import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';

final _dashboardProvider = FutureProvider<Map<String, dynamic>>((ref) async {
  final dio = ref.watch(dioProvider);
  final res = await dio.get('/dashboard');
  return res.data as Map<String, dynamic>;
});

class DashboardPage extends ConsumerWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(_dashboardProvider);
    return Scaffold(
      appBar: labixAppBar(
        'Dashboard',
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => ref.invalidate(_dashboardProvider),
          ),
        ],
      ),
      body: async.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => ErrorRetry(apiError(e), onRetry: () => ref.invalidate(_dashboardProvider)),
        data: (d) => RefreshIndicator(
          onRefresh: () async => ref.invalidate(_dashboardProvider),
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _StatsRow(d),
              const SizedBox(height: 16),
              _RecentVisitsCard(d),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatsRow extends StatelessWidget {
  const _StatsRow(this.d);
  final Map<String, dynamic> d;

  @override
  Widget build(BuildContext context) {
    final stats = [
      _Stat('Today\'s Visits',    '${d['todayVisits'] ?? 0}',    AppColors.navy),
      _Stat('Pending Reports',    '${d['pendingReports'] ?? 0}', AppColors.amber),
      _Stat('Released Today',     '${d['releasedToday'] ?? 0}',  AppColors.teal),
      _Stat('Pending Collection', '${d['pendingCollection'] ?? 0}', AppColors.green),
    ];
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 10,
      mainAxisSpacing: 10,
      childAspectRatio: 1.5,
      children: stats.map(_statCard).toList(),
    );
  }

  Widget _statCard(_Stat s) => Container(
    padding: const EdgeInsets.all(16),
    decoration: BoxDecoration(
      color: s.color.withAlpha(25),
      borderRadius: BorderRadius.circular(12),
      border: Border.all(color: s.color.withAlpha(80)),
    ),
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
      Text(s.label, style: TextStyle(fontSize: 11, color: s.color, fontWeight: FontWeight.w600)),
      Text(s.value, style: TextStyle(fontSize: 28, fontWeight: FontWeight.w800, color: s.color)),
    ]),
  );
}

class _Stat { const _Stat(this.label, this.value, this.color); final String label, value; final Color color; }

class _RecentVisitsCard extends StatelessWidget {
  const _RecentVisitsCard(this.d);
  final Map<String, dynamic> d;

  @override
  Widget build(BuildContext context) {
    final visits = (d['recentVisits'] as List? ?? [])
        .map((v) => v as Map<String, dynamic>)
        .toList();
    if (visits.isEmpty) return const SizedBox.shrink();
    return Card(
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(16, 14, 16, 8),
          child: Text('Recent Visits', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
        ),
        const Divider(height: 1),
        ...visits.map((v) => ListTile(
          dense: true,
          title: Text(v['patientName'] as String? ?? '', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
          subtitle: Text(v['visitNumber'] as String? ?? ''),
          trailing: StatusChip(v['status'] as String? ?? ''),
        )),
      ]),
    );
  }
}
