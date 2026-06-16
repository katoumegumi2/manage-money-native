package com.taufik.managemoney.ui.navigation

/** Argumen & rute non-tab (detail). Tab utama ada di [TopDestination]. */
const val ARG_ACCOUNT_ID = "accountId"

const val ACCOUNT_DETAIL_ROUTE = "account/{$ARG_ACCOUNT_ID}"

fun accountDetailRoute(accountId: Long): String = "account/$accountId"
