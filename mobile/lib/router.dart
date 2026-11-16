import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/auth/auth_model.dart';
import 'features/auth/presentation/login_page.dart';
import 'features/auth/presentation/no_access_page.dart';
import 'features/collection/presentation/collection_home_page.dart';
import 'features/collection/presentation/collection_detail_page.dart';
import 'features/dashboard/presentation/dashboard_page.dart';
import 'features/patients/presentation/patients_page.dart';
import 'features/visits/presentation/visits_page.dart';
import 'features/reports/presentation/reports_page.dart';
import 'shell_page.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final auth = ref.watch(authProvider);

  return GoRouter(
    initialLocation: '/login',
    redirect: (context, state) {
      final loggedIn = auth != null;
      final onLogin = state.matchedLocation == '/login';

      if (!loggedIn) return onLogin ? null : '/login';
      if (onLogin) {
        if (!auth.canAccessMobile) return '/no-access';
        return auth.hasFullAccess ? '/dashboard' : '/collections';
      }
      if (state.matchedLocation == '/no-access' && auth.canAccessMobile) {
        return auth.hasFullAccess ? '/dashboard' : '/collections';
      }
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (_, _) => const LoginPage()),
      GoRoute(path: '/no-access', builder: (_, _) => const NoAccessPage()),
      ShellRoute(
        builder: (context, state, child) => ShellPage(child: child),
        routes: [
          GoRoute(path: '/dashboard',   builder: (_, _) => const DashboardPage()),
          GoRoute(path: '/collections', builder: (_, _) => const CollectionHomePage()),
          GoRoute(
            path: '/collections/:id',
            builder: (_, s) => CollectionDetailPage(visitId: s.pathParameters['id']!),
          ),
          GoRoute(path: '/patients',    builder: (_, _) => const PatientsPage()),
          GoRoute(path: '/visits',      builder: (_, _) => const VisitsPage()),
          GoRoute(path: '/reports',     builder: (_, _) => const ReportsPage()),
        ],
      ),
    ],
  );
});
