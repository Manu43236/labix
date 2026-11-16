import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';
import '../data/collection_provider.dart';

class CollectionHomePage extends ConsumerWidget {
  const CollectionHomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(pendingCollectionsProvider);
    return Scaffold(
      appBar: labixAppBar(
        'Collections',
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(pendingCollectionsProvider),
          ),
        ],
      ),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorRetry(
          apiError(e),
          onRetry: () => ref.invalidate(pendingCollectionsProvider),
        ),
        data: (items) => items.isEmpty
            ? const Center(
                child: Column(mainAxisSize: MainAxisSize.min, children: [
                  Icon(Icons.check_circle_outline, size: 56, color: AppColors.teal),
                  SizedBox(height: 12),
                  Text('No pending collections', style: TextStyle(color: AppColors.muted)),
                ]),
              )
            : RefreshIndicator(
                onRefresh: () async => ref.invalidate(pendingCollectionsProvider),
                child: ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 10),
                  itemBuilder: (_, i) => _CollectionCard(items[i]),
                ),
              ),
      ),
    );
  }
}

class _CollectionCard extends StatelessWidget {
  const _CollectionCard(this.c);
  final PendingCollection c;

  @override
  Widget build(BuildContext context) => Card(
    child: InkWell(
      onTap: () => context.go('/collections/${c.visitId}'),
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            Expanded(
              child: Text(c.visitNumber,
                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15, color: AppColors.navy)),
            ),
            StatusChip('PENDING'),
          ]),
          const SizedBox(height: 8),
          Text(c.patientName, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
          if (c.patientPhone.isNotEmpty) ...[
            const SizedBox(height: 2),
            Text(c.patientPhone, style: const TextStyle(fontSize: 13, color: AppColors.muted)),
          ],
          if (c.address.isNotEmpty) ...[
            const SizedBox(height: 4),
            Row(children: [
              const Icon(Icons.location_on_outlined, size: 14, color: AppColors.muted),
              const SizedBox(width: 4),
              Expanded(child: Text(c.address, style: const TextStyle(fontSize: 12, color: AppColors.muted), maxLines: 1, overflow: TextOverflow.ellipsis)),
            ]),
          ],
          const SizedBox(height: 8),
          Wrap(
            spacing: 6,
            children: c.tests
                .map((t) => Chip(
                      label: Text(t.testName, style: const TextStyle(fontSize: 11)),
                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      padding: EdgeInsets.zero,
                      visualDensity: VisualDensity.compact,
                    ))
                .toList(),
          ),
        ]),
      ),
    ),
  );
}
