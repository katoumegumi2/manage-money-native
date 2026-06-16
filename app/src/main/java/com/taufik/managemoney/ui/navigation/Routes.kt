package com.taufik.managemoney.ui.navigation

/** Argumen & rute non-tab (detail). Tab utama ada di [TopDestination]. */
const val ARG_ACCOUNT_ID = "accountId"

const val ACCOUNT_DETAIL_ROUTE = "account/{$ARG_ACCOUNT_ID}"

fun accountDetailRoute(accountId: Long): String = "account/$accountId"

/** Input cepat: ketik kalimat → parse AI → Konfirmasi (Phase 5 diganti suara). */
const val PARSE_INPUT_ROUTE = "parse"

/** Form input manual transaksi. */
const val MANUAL_ENTRY_ROUTE = "manual"

/** Layar konfirmasi; argumen = payload Base64 `List<ParsedTransaction>` (lihat [ConfirmPayload]). */
const val ARG_CONFIRM_PAYLOAD = "payload"
const val CONFIRM_ROUTE = "confirm/{$ARG_CONFIRM_PAYLOAD}"

fun confirmRoute(payload: String): String = "confirm/$payload"
