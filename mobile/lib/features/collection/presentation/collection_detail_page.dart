import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';
import '../data/collection_provider.dart';

class CollectionDetailPage extends ConsumerStatefulWidget {
  const CollectionDetailPage({super.key, required this.visitId});
  final String visitId;

  @override
  ConsumerState<CollectionDetailPage> createState() => _CollectionDetailPageState();
}

class _CollectionDetailPageState extends ConsumerState<CollectionDetailPage> {
  final Set<String> _updating = {};

  Future<void> _markCollected(CollectionTest test) async {
    if (_updating.contains(test.visitTestId)) return;
    setState(() => _updating.add(test.visitTestId));
    try {
      final dio = ref.read(dioProvider);
      await dio.patch('/visit-tests/${test.visitTestId}/sample-status', data: {'status': 'COLLECTED'});
      ref.invalidate(collectionDetailProvider(widget.visitId));
      ref.invalidate(pendingCollectionsProvider);
    } on Exception catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(apiError(e)), backgroundColor: AppColors.red),
      );
    } finally {
      if (mounted) setState(() => _updating.remove(test.visitTestId));
    }
  }

  Future<void> _markAllCollected(List<CollectionTest> tests) async {
    for (final t in tests.where((t) => t.status == 'PENDING')) {
      await _markCollected(t);
    }
  }

  @override
  Widget build(BuildContext context) {
    final async = ref.watch(collectionDetailProvider(widget.visitId));
    return Scaffold(
      appBar: labixAppBar('Collection Detail'),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorRetry(
          apiError(e),
          onRetry: () => ref.invalidate(collectionDetailProvider(widget.visitId)),
        ),
        data: (c) {
          final pending = c.tests.where((t) => t.status == 'PENDING').toList();
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _infoCard(c),
              const SizedBox(height: 16),
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
                Text('Samples (${c.tests.length})',
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
                if (pending.isNotEmpty)
                  FilledButton.icon(
                    onPressed: () => _markAllCollected(c.tests),
                    icon: const Icon(Icons.done_all, size: 18),
                    label: const Text('Mark All Collected'),
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.teal,
                      visualDensity: VisualDensity.compact,
                    ),
                  ),
              ]),
              const SizedBox(height: 10),
              ...c.tests.map((t) => _sampleRow(t)),
            ],
          );
        },
      ),
    );
  }

  Widget _infoCard(PendingCollection c) => Card(
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Expanded(child: Text(c.visitNumber,
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16, color: AppColors.navy))),
          if (c.scheduledAt.isNotEmpty)
            Text(_formatDate(c.scheduledAt), style: const TextStyle(fontSize: 12, color: AppColors.muted)),
        ]),
        const Divider(height: 20),
        LabeledField('Patient', c.patientName),
        if (c.patientPhone.isNotEmpty) ...[
          const SizedBox(height: 10),
          LabeledField('Phone', c.patientPhone),
        ],
        if (c.address.isNotEmpty) ...[
          const SizedBox(height: 10),
          LabeledField('Address', c.address),
        ],
      ]),
    ),
  );

  Widget _sampleRow(CollectionTest t) {
    final collected = t.status == 'COLLECTED';
    final loading = _updating.contains(t.visitTestId);
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        title: Text(t.testName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
        subtitle: t.sampleType.isNotEmpty
            ? Text(t.sampleType, style: const TextStyle(fontSize: 12, color: AppColors.muted))
            : null,
        trailing: loading
            ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2))
            : collected
                ? const Icon(Icons.check_circle, color: AppColors.teal, size: 28)
                : FilledButton(
                    onPressed: () => _markCollected(t),
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.navy,
                      visualDensity: VisualDensity.compact,
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                    ),
                    child: const Text('Collect', style: TextStyle(fontSize: 13)),
                  ),
      ),
    );
  }

  String _formatDate(String iso) {
    try {
      final d = DateTime.parse(iso).toLocal();
      return '${d.day}/${d.month} ${d.hour.toString().padLeft(2,'0')}:${d.minute.toString().padLeft(2,'0')}';
    } on FormatException {
      return iso;
    }
  }
}
