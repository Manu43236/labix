import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'dart:convert';

class AuthUser {
  const AuthUser({
    required this.userId,
    required this.tenantId,
    required this.roles,
    required this.planCode,
    this.email,
  });

  final String userId;
  final String tenantId;
  final List<String> roles;
  final String planCode;
  final String? email;

  bool get canAccessMobile =>
      planCode == 'MOB_COLLECT' || planCode == 'MOB_FULL';
  bool get hasFullAccess => planCode == 'MOB_FULL';
  bool get isCollector => roles.contains('COLLECTOR');
  bool get isAdmin => roles.contains('LAB_ADMIN');
  bool get isPathologist => roles.contains('PATHOLOGIST');
  bool get isTechnician => roles.contains('TECHNICIAN');

  bool hasRole(String role) => roles.contains(role);

  Map<String, dynamic> toJson() => {
    'userId': userId, 'tenantId': tenantId,
    'roles': roles, 'planCode': planCode, 'email': email,
  };

  factory AuthUser.fromJson(Map<String, dynamic> j) => AuthUser(
    userId: j['userId'] as String,
    tenantId: j['tenantId'] as String,
    roles: List<String>.from(j['roles'] as List),
    planCode: j['planCode'] as String? ?? 'WEB',
    email: j['email'] as String?,
  );
}

final _storage = FlutterSecureStorage();

final authProvider = StateNotifierProvider<AuthNotifier, AuthUser?>(
  (ref) => AuthNotifier(),
);

class AuthNotifier extends StateNotifier<AuthUser?> {
  AuthNotifier() : super(null) { _restore(); }

  Future<void> _restore() async {
    final raw = await _storage.read(key: 'user');
    if (raw != null) {
      try { state = AuthUser.fromJson(jsonDecode(raw) as Map<String, dynamic>); }
      catch (_) { await _storage.deleteAll(); }
    }
  }

  Future<void> setSession(String token, AuthUser user) async {
    await _storage.write(key: 'token', value: token);
    await _storage.write(key: 'user', value: jsonEncode(user.toJson()));
    state = user;
  }

  Future<void> logout() async {
    await _storage.deleteAll();
    state = null;
  }
}
