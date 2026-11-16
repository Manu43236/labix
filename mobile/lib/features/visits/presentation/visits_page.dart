import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';

class _Visit {
  const _Visit({required this.id, required this.number, required this.patientName, required this.status, required this.createdAt, required this.total});
  final String id, number, patientName, status, createdAt;
  final int total;
  factory _Visit.fromJson(Map<String, dynamic> j) => _Visit(
    id: j['id'] as String, number: j['visitNumber'] as String,
    patientName: j['patientName'] as String? ?? '',
    status: j['status'] as String? ?? '', createdAt: j['createdAt'] as String? ?? '',
    total: j['totalAmount'] as int? ?? 0,
  );
}

final _visitsProvider = FutureProvider<List<_Visit>>((ref) async {
  final dio = ref.watch(dioProvider);
  final res = await dio.get('/visits', queryParameters: {'limit': '50', 'sort': 'createdAt,desc'});
  final list = (res.data['data'] ?? res.data) as List;
  return list.map((e) => _Visit.fromJson(e as Map<String, dynamic>)).toList();
});

class VisitsPage extends ConsumerWidget {
  const VisitsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(_visitsProvider);
    return Scaffold(
      appBar: labixAppBar(
        'Visits',
        actions: [
          IconButton(icon: const Icon(Icons.refresh), onPressed: () => ref.invalidate(_visitsProvider)),
        ],
      ),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorRetry(apiError(e), onRetry: () => ref.invalidate(_visitsProvider)),
        data: (visits) => visits.isEmpty
            ? const Center(child: Text('No visits', style: TextStyle(color: AppColors.muted)))
            : RefreshIndicator(
                onRefresh: () async => ref.invalidate(_visitsProvider),
                child: ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: visits.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 8),
                  itemBuilder: (_, i) => _VisitCard(visits[i]),
                ),
              ),
      ),
    );
  }
}

class _VisitCard extends StatelessWidget {
  const _VisitCard(this.v);
  final _Visit v;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(14),
      child: Row(children: [
        Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(v.number, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: AppColors.navy)),
          const SizedBox(height: 4),
          Text(v.patientName, style: const TextStyle(fontSize: 14)),
          const SizedBox(height: 2),
          Text(_formatDate(v.createdAt), style: const TextStyle(fontSize: 12, color: AppColors.muted)),
        ])),
        Column(crossAxisAlignment: CrossAxisAlignment.end, mainAxisSize: MainAxisSize.min, children: [
          StatusChip(v.status),
          const SizedBox(height: 6),
          Text(paise(v.total), style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
        ]),
      ]),
    ),
  );

  String _formatDate(String iso) {
    try {
      final d = DateTime.parse(iso).toLocal();
      return '${d.day}/${d.month}/${d.year}';
    } on FormatException {
      return iso;
    }
  }
}
