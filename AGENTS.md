# AGENTS.md

Panduan untuk semua coding agent (Claude Code, dll) di repo ini.

**Baca `CLAUDE.md` lebih dulu — itu sumber konteks tetap (stack, arsitektur, kontrak AI, data model, design tokens, konvensi, rencana fase).** File ini sengaja tidak menduplikasi isinya agar tidak melenceng (single source of truth).

Detail lengkap ada di `docs/private/` (brief, desain data, prompt parser, mockup, setup LXC).

## Aturan kerja
- **Bangun bertahap, satu fase satu PR.** Jangan loncat fase. Tiap fase punya *acceptance criteria* di `docs/private/brief-claude-code.md` §6 — fase selesai hanya jika semua terpenuhi.
- Identifier kode = Inggris; string UI = Indonesia.
- Uang = `Long` rupiah (tanpa float). Tanggal = ISO/`LocalDate`, zona `Asia/Jakarta`.
- Jangan pernah commit/log API key atau secret.
- Tulis unit test untuk logika saldo, validator parser, dan aturan simpan.

## Build & test
- `./gradlew assembleDebug` — build APK debug.
- `./gradlew test` — unit test JVM.
- `./gradlew installDebug` — pasang ke HP fisik via adb.
- Butuh JDK 17 + Android SDK (`ANDROID_HOME`). Lihat `docs/private/setup-lxc.md`.
