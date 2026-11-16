import 'dart:io';
import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';

class _Report {
  const _Report({required this.id, required this.visitNumber, required this.patientName, required this.status, required this.createdAt});
  final String id, visitNumber, patientName, status, createdAt;
  factory _Report.fromJson(Map<String, dynamic> j) => _Report(
    id: j['id'] as String, visitNumber: j['visitNumber'] as String,
    patientName: j['patientName'] as String? ?? '',
    status: j['status'] as String, createdAt: j['createdAt'] as String? ?? '',
  );
}

final _reportsProvider = FutureProvider<List<_Report>>((ref) async {
  final dio = ref.watch(dioProvider);
  final res = await dio.get('/reports', queryParameters: {'limit': '50', 'sort': 'createdAt,desc'});
  final list = (res.data['data'] ?? res.data) as List;
  return list.map((e) => _Report.fromJson(e as Map<String, dynamic>)).toList();
});

class ReportsPage extends ConsumerWidget {
  const ReportsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(_reportsProvider);
    return Scaffold(
      appBar: labixAppBar(
        'Reports',
        actions: [
          IconButton(icon: const Icon(Icons.refresh), onPressed: () => ref.invalidate(_reportsProvider)),
        ],
      ),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorRetry(apiError(e), onRetry: () => ref.invalidate(_reportsProvider)),
        data: (reports) => reports.isEmpty
            ? const Center(child: Text('No reports', style: TextStyle(color: AppColors.muted)))
            : RefreshIndicator(
                onRefresh: () async => ref.invalidate(_reportsProvider),
                child: ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: reports.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 8),
                  itemBuilder: (_, i) => _ReportCard(reports[i]),
                ),
              ),
      ),
    );
  }
}

class _ReportCard extends ConsumerStatefulWidget {
  const _ReportCard(this.r);
  final _Report r;

  @override
  ConsumerState<_ReportCard> createState() => _ReportCardState();
}

class _ReportCardState extends ConsumerState<_ReportCard> {
  bool _sharing = false;

  Future<void> _share() async {
    if (widget.r.status != 'RELEASED') return;
    setState(() => _sharing = true);
    try {
      final dio = ref.read(dioProvider);
      final res = await dio.get<Uint8List>(
        '/reports/${widget.r.id}/pdf',
        options: Options(responseType: ResponseType.bytes),
      );
      final bytes = res.data;
      if (bytes == null) return;

      final dir = await getTemporaryDirectory();
      final file = File('${dir.path}/${widget.r.visitNumber}.pdf');
      await file.writeAsBytes(bytes);

      await Share.shareXFiles(
        [XFile(file.path, mimeType: 'application/pdf')],
        subject: 'Report - ${widget.r.patientName}',
      );
    } on Exception catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(apiError(e)), backgroundColor: AppColors.red),
      );
    } finally {
      if (mounted) setState(() => _sharing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final r = widget.r;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(children: [
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(r.visitNumber, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: AppColors.navy)),
            const SizedBox(height: 4),
            Text(r.patientName, style: const TextStyle(fontSize: 14)),
            const SizedBox(height: 2),
            Text(_formatDate(r.createdAt), style: const TextStyle(fontSize: 12, color: AppColors.muted)),
          ])),
          Column(crossAxisAlignment: CrossAxisAlignment.end, mainAxisSize: MainAxisSize.min, children: [
            StatusChip(r.status),
            if (r.status == 'RELEASED') ...[
              const SizedBox(height: 8),
              _sharing
                  ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2))
                  : IconButton.filled(
                      onPressed: _share,
                      icon: const Icon(Icons.share, size: 18),
                      style: IconButton.styleFrom(
                        backgroundColor: AppColors.teal,
                        foregroundColor: Colors.white,
                        minimumSize: const Size(36, 36),
                      ),
                      tooltip: 'Share via WhatsApp / OS',
                    ),
            ],
          ]),
        ]),
      ),
    );
  }

  String _formatDate(String iso) {
    try {
      final d = DateTime.parse(iso).toLocal();
      return '${d.day}/${d.month}/${d.year}';
    } on FormatException {
      return iso;
    }
  }
}
