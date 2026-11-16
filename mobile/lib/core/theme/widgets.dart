import 'package:flutter/material.dart';
import 'app_theme.dart';

class StatusChip extends StatelessWidget {
  const StatusChip(this.status, {super.key, this.small = true});
  final String status;
  final bool small;

  static const _map = {
    'PENDING':    (Color(0xFFFEF3C7), Color(0xFF92400E)),
    'COLLECTED':  (Color(0xFFDBEAFE), Color(0xFF1E40AF)),
    'RECEIVED':   (Color(0xFFE0E7FF), Color(0xFF3730A3)),
    'ENTERED':    (Color(0xFFF3E8FF), Color(0xFF6B21A8)),
    'VERIFIED':   (Color(0xFFD1FAE5), Color(0xFF065F46)),
    'RELEASED':   (Color(0xFFDCFCE7), Color(0xFF15803D)),
    'PAID':       (Color(0xFFD1FAE5), Color(0xFF065F46)),
    'DUE':        (Color(0xFFFEE2E2), Color(0xFF991B1B)),
    'PARTIAL':    (Color(0xFFFEF3C7), Color(0xFF92400E)),
    'DRAFT':      (Color(0xFFF1F5F9), Color(0xFF475569)),
    'ACTIVE':     (Color(0xFFD1FAE5), Color(0xFF065F46)),
    'SUSPENDED':  (Color(0xFFFEE2E2), Color(0xFF991B1B)),
    'REJECTED':   (Color(0xFFFEE2E2), Color(0xFF991B1B)),
    'REGISTERED': (Color(0xFFDBEAFE), Color(0xFF1E40AF)),
  };

  @override
  Widget build(BuildContext context) {
    final (bg, fg) = _map[status] ?? (const Color(0xFFF1F5F9), AppColors.muted);
    return Container(
      padding: EdgeInsets.symmetric(horizontal: small ? 8 : 12, vertical: small ? 3 : 5),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(100)),
      child: Text(status, style: TextStyle(color: fg, fontSize: small ? 11 : 13, fontWeight: FontWeight.w600)),
    );
  }
}

class FlagChip extends StatelessWidget {
  const FlagChip(this.flag, {super.key});
  final String? flag;

  @override
  Widget build(BuildContext context) {
    if (flag == null || flag!.isEmpty) return const Text('—', style: TextStyle(color: AppColors.muted, fontSize: 12));
    final (bg, fg, icon) = switch (flag!) {
      'CRITICAL' => (const Color(0xFFFEE2E2), const Color(0xFF991B1B), '⚠ '),
      'HIGH'     => (const Color(0xFFFEF3C7), const Color(0xFF92400E), '↑ '),
      'LOW'      => (const Color(0xFFDBEAFE), const Color(0xFF1E40AF), '↓ '),
      'NORMAL'   => (Colors.transparent, AppColors.green, ''),
      _          => (Colors.transparent, AppColors.muted, ''),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(6)),
      child: Text('$icon${flag!}',
        style: TextStyle(color: fg, fontSize: 12, fontWeight: FontWeight.w600)),
    );
  }
}

class LabeledField extends StatelessWidget {
  const LabeledField(this.label, this.value, {super.key});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Text(label, style: const TextStyle(fontSize: 11, color: AppColors.muted, fontWeight: FontWeight.w500)),
      const SizedBox(height: 2),
      Text(value, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
    ],
  );
}

class ErrorRetry extends StatelessWidget {
  const ErrorRetry(this.message, {super.key, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        const Icon(Icons.error_outline, size: 48, color: AppColors.red),
        const SizedBox(height: 12),
        Text(message, textAlign: TextAlign.center, style: const TextStyle(color: AppColors.muted)),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: onRetry,
          icon: const Icon(Icons.refresh),
          label: const Text('Retry'),
          style: FilledButton.styleFrom(backgroundColor: AppColors.navy),
        ),
      ]),
    ),
  );
}

String paise(int p) => '₹${(p / 100).toStringAsFixed(2)}';
