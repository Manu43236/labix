import 'package:flutter_test/flutter_test.dart';
import 'package:labix_mobile/core/auth/auth_model.dart';

void main() {
  final base = AuthUser(
    userId: 'u1', tenantId: 't1',
    roles: ['COLLECTOR'], planCode: 'MOB_COLLECT',
  );

  test('MOB_COLLECT: canAccessMobile true, hasFullAccess false', () {
    expect(base.canAccessMobile, isTrue);
    expect(base.hasFullAccess, isFalse);
  });

  test('MOB_FULL: canAccessMobile true, hasFullAccess true', () {
    final u = AuthUser(userId: 'u2', tenantId: 't1', roles: ['LAB_ADMIN'], planCode: 'MOB_FULL');
    expect(u.canAccessMobile, isTrue);
    expect(u.hasFullAccess, isTrue);
  });

  test('WEB plan: canAccessMobile false', () {
    final u = AuthUser(userId: 'u3', tenantId: 't1', roles: ['RECEPTIONIST'], planCode: 'WEB');
    expect(u.canAccessMobile, isFalse);
  });

  test('hasRole matches correctly', () {
    expect(base.hasRole('COLLECTOR'), isTrue);
    expect(base.hasRole('PATHOLOGIST'), isFalse);
  });

  test('round-trip JSON serialisation', () {
    final json = base.toJson();
    final restored = AuthUser.fromJson(json);
    expect(restored.userId, base.userId);
    expect(restored.planCode, base.planCode);
    expect(restored.roles, base.roles);
  });

  test('fromJson defaults planCode to WEB when absent', () {
    final u = AuthUser.fromJson({'userId': 'x', 'tenantId': 't', 'roles': []});
    expect(u.planCode, 'WEB');
  });
}
