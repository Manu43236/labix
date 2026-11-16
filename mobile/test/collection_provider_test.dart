import 'package:flutter_test/flutter_test.dart';
import 'package:labix_mobile/features/collection/data/collection_provider.dart';

void main() {
  group('PendingCollection.fromJson', () {
    final json = {
      'visitId': 'v1',
      'visitNumber': 'VN-001',
      'patientName': 'Ravi Kumar',
      'patientPhone': '9876543210',
      'collectionAddress': '12 Main St',
      'scheduledAt': '2026-11-15T09:00:00',
      'tests': [
        {'visitTestId': 'vt1', 'testName': 'CBC', 'sampleType': 'Blood', 'status': 'PENDING'},
        {'visitTestId': 'vt2', 'testName': 'LFT', 'sampleType': 'Blood', 'status': 'COLLECTED'},
      ],
    };

    test('parses all fields correctly', () {
      final c = PendingCollection.fromJson(json);
      expect(c.visitId, 'v1');
      expect(c.visitNumber, 'VN-001');
      expect(c.patientName, 'Ravi Kumar');
      expect(c.tests.length, 2);
    });

    test('tests status preserved', () {
      final c = PendingCollection.fromJson(json);
      expect(c.tests.first.status, 'PENDING');
      expect(c.tests.last.status, 'COLLECTED');
    });

    test('defaults to empty string for optional fields', () {
      final minimal = {'visitId': 'v2', 'visitNumber': 'VN-002', 'patientName': 'P'};
      final c = PendingCollection.fromJson(minimal);
      expect(c.patientPhone, '');
      expect(c.address, '');
      expect(c.tests, isEmpty);
    });
  });

  group('CollectionTest.fromJson', () {
    test('parses correctly', () {
      final t = CollectionTest.fromJson({
        'visitTestId': 'vt1', 'testName': 'CBC', 'sampleType': 'Blood', 'status': 'PENDING',
      });
      expect(t.visitTestId, 'vt1');
      expect(t.sampleType, 'Blood');
    });
  });
}
