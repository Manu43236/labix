import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/auth/auth_model.dart';
import 'core/theme/app_theme.dart';

class ShellPage extends ConsumerWidget {
  const ShellPage({super.key, required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authProvider);
    if (user == null) return child;

    final isFullAccess = user.hasFullAccess;
    final location = GoRouterState.of(context).matchedLocation;

    final tabs = isFullAccess
        ? const [
            _Tab('/dashboard',   Icons.dashboard_outlined,   'Dashboard'),
            _Tab('/patients',    Icons.people_outline,        'Patients'),
            _Tab('/visits',      Icons.science_outlined,      'Visits'),
            _Tab('/reports',     Icons.description_outlined,  'Reports'),
            _Tab('/collections', Icons.local_shipping_outlined,'Collect'),
          ]
        : const [
            _Tab('/collections', Icons.local_shipping_outlined, 'Collections'),
          ];

    final currentIndex = tabs.indexWhere((t) => location.startsWith(t.path));

    return Scaffold(
      body: child,
      bottomNavigationBar: tabs.length == 1
          ? null
          : BottomNavigationBar(
              currentIndex: currentIndex < 0 ? 0 : currentIndex,
              onTap: (i) => context.go(tabs[i].path),
              items: tabs
                  .map((t) => BottomNavigationBarItem(icon: Icon(t.icon), label: t.label))
                  .toList(),
            ),
    );
  }
}

class _Tab {
  const _Tab(this.path, this.icon, this.label);
  final String path;
  final IconData icon;
  final String label;
}

AppBar labixAppBar(String title, {List<Widget>? actions, bool showLogo = false}) => AppBar(
      backgroundColor: AppColors.navy,
      foregroundColor: Colors.white,
      elevation: 0,
      title: showLogo
          ? Image.asset('assets/images/labix_logo.png', height: 32, fit: BoxFit.contain)
          : Text(title),
      actions: actions,
    );
