import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/theme/widgets.dart';
import '../../../shell_page.dart';

class _Patient {
  const _Patient({required this.id, required this.name, required this.phone, required this.gender, required this.dob});
  final String id, name, phone, gender, dob;
  factory _Patient.fromJson(Map<String, dynamic> j) => _Patient(
    id: j['id'] as String, name: j['name'] as String,
    phone: j['phone'] as String? ?? '', gender: j['gender'] as String? ?? '',
    dob: j['dob'] as String? ?? '',
  );
}

final _searchProvider = StateProvider<String>((ref) => '');

final _patientsProvider = FutureProvider.family<List<_Patient>, String>((ref, q) async {
  final dio = ref.watch(dioProvider);
  final params = q.isNotEmpty ? {'q': q, 'limit': '30'} : {'limit': '30'};
  final res = await dio.get('/patients', queryParameters: params);
  final list = (res.data['data'] ?? res.data) as List;
  return list.map((e) => _Patient.fromJson(e as Map<String, dynamic>)).toList();
});

class PatientsPage extends ConsumerWidget {
  const PatientsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(_searchProvider);
    final async = ref.watch(_patientsProvider(q));
    return Scaffold(
      appBar: labixAppBar('Patients'),
      body: Column(children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
          child: TextField(
            decoration: const InputDecoration(
              prefixIcon: Icon(Icons.search, size: 20),
              hintText: 'Search patients…',
              contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            ),
            onChanged: (v) => ref.read(_searchProvider.notifier).state = v,
          ),
        ),
        Expanded(child: async.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => ErrorRetry(apiError(e), onRetry: () => ref.invalidate(_patientsProvider(q))),
          data: (patients) => patients.isEmpty
              ? const Center(child: Text('No patients found', style: TextStyle(color: AppColors.muted)))
              : ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: patients.length,
                  separatorBuilder: (_, _) => const Divider(height: 1),
                  itemBuilder: (_, i) {
                    final p = patients[i];
                    return ListTile(
                      contentPadding: const EdgeInsets.symmetric(vertical: 6),
                      leading: CircleAvatar(
                        backgroundColor: AppColors.navy.withAlpha(25),
                        child: Text(p.name.isNotEmpty ? p.name[0].toUpperCase() : '?',
                            style: const TextStyle(color: AppColors.navy, fontWeight: FontWeight.w700)),
                      ),
                      title: Text(p.name, style: const TextStyle(fontWeight: FontWeight.w600)),
                      subtitle: Text([p.phone, p.gender].where((s) => s.isNotEmpty).join(' · ')),
                    );
                  },
                ),
        )),
      ]),
    );
  }
}
