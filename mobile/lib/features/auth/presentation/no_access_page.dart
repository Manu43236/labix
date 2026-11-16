import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_strings.dart';
import '../../../core/auth/auth_model.dart';
import '../../../core/theme/app_theme.dart';

class NoAccessPage extends ConsumerWidget {
  const NoAccessPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    backgroundColor: AppColors.bg,
    appBar: AppBar(
      title: const Text(S.appName),
      actions: [
        TextButton(
          onPressed: () => ref.read(authProvider.notifier).logout(),
          child: Text(S.logout, style: const TextStyle(color: Colors.white)),
        ),
      ],
    ),
    body: Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.phone_android_outlined, size: 72, color: AppColors.muted.withAlpha(100)),
          const SizedBox(height: 24),
          const Text(
            'Mobile Access Not Available',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: AppColors.navy),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),
          const Text(
            S.noMobileAccess,
            style: TextStyle(color: AppColors.muted, fontSize: 14, height: 1.6),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    ),
  );
}
