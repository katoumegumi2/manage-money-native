package com.taufik.managemoney.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Redesign v3 — palet "modern minimal terang, aksen emerald".
// Sumber: docs/private/design_handoff_catatan_keuangan/README.md (Design Tokens).
// ============================================================================

// -- Netral (light) --
val Bg = Color(0xFFFFFFFF)        // background semua layar
val CardLight = Color(0xFFFFFFFF) // permukaan kartu
val Surface1 = Color(0xFFF6F7F9)  // chip akun
val Surface2 = Color(0xFFF8F9FB)  // kartu detail / insight
val Field = Color(0xFFF4F5F7)     // search, chip filter, segmented
val Hairline = Color(0xFFF2F4F7)  // garis pemisah baris
val Ink = Color(0xFF101828)       // teks utama, tombol primer, nav aktif
val TextBody = Color(0xFF475467)  // teks sekunder / paragraf
val Muted = Color(0xFF98A2B3)     // caption, label, nav nonaktif, placeholder
val Line = Hairline               // alias lama

// -- Aksen emerald --
val Emerald = Color(0xFF12B76A)
val EmeraldBright = Color(0xFF32D583)
val EmeraldSoftBg = Color(0xFFEAFBF1)
val EmeraldSoftText = Color(0xFF0B7A48)

// -- Bahaya / pengeluaran --
val Danger = Color(0xFFF04438)
val DangerSoftBg = Color(0xFFFEF3F2)

// -- Peringatan / "perlu dicek" (amber) --
val Warn = Color(0xFFF79009)
val WarnSoftBg = Color(0xFFFFFAEB)

// -- Semantik jenis transaksi (v3) --
val Income = Emerald
val Expense = Danger
val Transfer = Color(0xFF667085)

// -- Aksen lama (masih dirujuk beberapa layar; diganti bertahap ke Emerald) --
val Gold = Emerald
val Paper = Bg

// -- Kartu "total" gelap (tetap gelap di light & dark) --
val BalanceCardBg = Color(0xFF0B0F19)
val MutedOnDark = Color(0xFF98A2B3)   // label & caption di kartu gelap
val IncomeOnDark = Color(0xFF32D583)  // angka positif di kartu gelap
val ExpenseOnDark = Color(0xFFFDA29B) // angka negatif di kartu gelap

// -- Turunan dark theme --
val DarkBg = Color(0xFF0E1420)
val InkDark = Color(0xFF0E1420)        // alias lama = background dark
val SurfaceDark = Color(0xFF1E2A3A)    // chip/surfaceVariant dark
val CardDark = Color(0xFF18222F)       // permukaan kartu dark
val DarkField = Color(0xFF232F3D)
val LineDark = Color(0xFF2A3848)
val OnDark = Color(0xFFEDF0F2)
val MutedDark = Color(0xFF98A2B3)
val EmeraldSoftBgDark = Color(0xFF10352A)
val EmeraldSoftTextDark = Color(0xFF6EE7B0)
val DangerSoftBgDark = Color(0xFF3A1D1B)

// -- Warna brand badge akun (fallback per jenis bila kode tak dikenal). --
val BadgeBni = Color(0xFFF2843B)
val BadgeMandiri = Color(0xFF1B66C9)
val BadgeSeabank = Color(0xFF12B0A0)
val BadgeGopay = Color(0xFF22B8E6)
val BadgeShopeepay = Color(0xFFF04438)
val BadgeTunai = Color(0xFF667085)
