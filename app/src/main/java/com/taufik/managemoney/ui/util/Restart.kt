package com.taufik.managemoney.ui.util

import android.content.Context
import android.content.Intent

/** Restart aplikasi (dipakai setelah restore DB — koneksi Room sudah ditutup). */
fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
