import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';

// A pending collection: one visit with its sample items to collect
class PendingCollection {
  const PendingCollection({
    required this.visitId,
    required this.visitNumber,
    required this.patientName,
    required this.patientPhone,
    required this.address,
    required this.scheduledAt,
    required this.tests,
  });
  final String visitId;
  final String visitNumber;
  final String patientName;
  final String patientPhone;
  final String address;
  final String scheduledAt;
  final List<CollectionTest> tests;

  factory PendingCollection.fromJson(Map<String, dynamic> j) => PendingCollection(
    visitId: j['visitId'] as String,
    visitNumber: j['visitNumber'] as String,
    patientName: j['patientName'] as String,
    patientPhone: j['patientPhone'] as String? ?? '',
    address: j['collectionAddress'] as String? ?? '',
    scheduledAt: j['scheduledAt'] as String? ?? '',
    tests: (j['tests'] as List? ?? [])
        .map((t) => CollectionTest.fromJson(t as Map<String, dynamic>))
        .toList(),
  );
}

class CollectionTest {
  const CollectionTest({required this.visitTestId, required this.testName, required this.sampleType, required this.status});
  final String visitTestId;
  final String testName;
  final String sampleType;
  final String status;

  factory CollectionTest.fromJson(Map<String, dynamic> j) => CollectionTest(
    visitTestId: j['visitTestId'] as String,
    testName: j['testName'] as String,
    sampleType: j['sampleType'] as String? ?? '',
    status: j['status'] as String,
  );
}

final pendingCollectionsProvider = FutureProvider<List<PendingCollection>>((ref) async {
  final dio = ref.watch(dioProvider);
  final res = await dio.get('/collections/mine');
  final list = res.data as List;
  return list.map((e) => PendingCollection.fromJson(e as Map<String, dynamic>)).toList();
});

final collectionDetailProvider = FutureProvider.family<PendingCollection, String>((ref, visitId) async {
  final dio = ref.watch(dioProvider);
  final res = await dio.get('/collections/mine/$visitId');
  return PendingCollection.fromJson(res.data as Map<String, dynamic>);
});
