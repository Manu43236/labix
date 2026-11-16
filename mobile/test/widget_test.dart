import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:labix_mobile/main.dart';

void main() {
  testWidgets('app launches without crashing', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LabixApp()));
    await tester.pump();
  });
}
