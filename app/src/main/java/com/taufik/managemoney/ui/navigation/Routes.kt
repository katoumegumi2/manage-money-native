package com.taufik.managemoney.ui.navigation

/** Argumen & rute non-tab (detail). Tab utama ada di [TopDestination]. */
const val ARG_ACCOUNT_ID = "accountId"

const val ACCOUNT_DETAIL_ROUTE = "account/{$ARG_ACCOUNT_ID}"

fun accountDetailRoute(accountId: String): String = "account/$accountId"

/** Edit/hapus satu transaksi tersimpan (dari Riwayat). */
const val ARG_TX_ID = "txId"
const val EDIT_TX_ROUTE = "edit/{$ARG_TX_ID}"

fun editTxRoute(txId: String): String = "edit/$txId"

/** Input suara: mic → STT → parse AI → Konfirmasi (jalur utama). */
const val VOICE_INPUT_ROUTE = "voice"

/** Input cepat: ketik kalimat → parse AI → Konfirmasi (fallback "ketik saja"). */
const val PARSE_INPUT_ROUTE = "parse"

/** Form input manual transaksi. */
const val MANUAL_ENTRY_ROUTE = "manual"

/** Pengaturan (menu) — dibuka dari ikon gear di Beranda. Berisi 2 kartu: Rekening & Sistem. */
const val SETTINGS_ROUTE = "settings"

/** Pengaturan Rekening — kelola akun (aktif/nonaktif, saldo awal, tambah). Sub-layar Pengaturan. */
const val ACCOUNTS_ROUTE = "settings/accounts"

/** Pengaturan Sistem — tema, AI, cadangan Drive, data. Sub-layar Pengaturan. */
const val SYSTEM_SETTINGS_ROUTE = "settings/system"

/** Chat "Tanya AI" — tanya bebas tentang keuangan (dibuka dari Laporan). */
const val ASK_AI_ROUTE = "ask"

/** Layar konfirmasi; argumen = payload Base64 `List<ParsedTransaction>` (lihat [ConfirmPayload]). */
const val ARG_CONFIRM_PAYLOAD = "payload"
const val CONFIRM_ROUTE = "confirm/{$ARG_CONFIRM_PAYLOAD}"

fun confirmRoute(payload: String): String = "confirm/$payload"
