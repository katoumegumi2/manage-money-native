package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.AggregatedFinance
import com.taufik.managemoney.domain.model.AskContext

/** Prompt analisis — TERPISAH dari prompt parser (lihat docs/private/fitur-analisis-ai.md). */
internal const val ANALYSIS_SYSTEM_INSTRUCTION = """
Kamu adalah analis keuangan pribadi. Kamu diberi DATA agregat keuangan (JSON) untuk satu periode.
Buat insight ringkas, jujur, dan actionable dalam Bahasa Indonesia.

ATURAN:
- JANGAN mengarang atau menghitung angka di luar DATA. Pakai hanya angka yang ada.
- Mata uang Rupiah, format "Rp 1.234.567" (pemisah titik).
- Nada santai, suportif, tidak menggurui. Sapa dengan "kamu".
- Fokus ke yang penting: kategori terbesar, perubahan vs bulan lalu (naik/turun), net (surplus/defisit).
- Kalau ada defisit (keluar > masuk), sebutkan dengan jujur + 1 saran konkret.

FORMAT OUTPUT (teks biasa, tanpa markdown heading, tanpa JSON):
- Baris pertama: 1 kalimat ringkasan keseluruhan.
- Lalu 3-5 poin, tiap poin diawali "- " (tanda minus spasi).
- Maksimal ~120 kata total.
"""

/** Rakit konten user: agregat → JSON ringkas + perintah periode. */
internal fun buildAnalysisUserContent(data: AggregatedFinance): String {
    val cats = data.categories.joinToString(",") { """{"kategori":"${it.kategori}","total":${it.total}}""" }
    return """
        DATA (periode ${data.period}):
        {
          "periode":"${data.periodKey}",
          "pemasukan":${data.masuk},
          "pengeluaran":${data.keluar},
          "net":${data.net},
          "pengeluaran_bulan_lalu":${data.prevKeluar},
          "pemasukan_bulan_lalu":${data.prevMasuk},
          "pengeluaran_per_kategori":[$cats]
        }
        Buat insight untuk periode ini.
    """.trimIndent()
}

/** Prompt "Tanya AI" — jawab hanya dari DATA agregat. */
internal const val ASK_SYSTEM_INSTRUCTION = """
Kamu asisten keuangan pribadi. Jawab pertanyaan user HANYA berdasarkan DATA agregat (JSON) yang diberikan.

ATURAN:
- JANGAN mengarang angka. Kalau jawaban tidak ada di DATA, katakan jujur datanya belum cukup dan sebut data apa yang kurang (mis. "rincian per transaksi belum tersedia").
- DATA hanya agregat bulan ini (total, per kategori, saldo akun) — TIDAK ada rincian per transaksi/merchant. Jadi pertanyaan spesifik seperti "habis berapa buat kopi" tak bisa dijawab persis; arahkan ke kategori terdekat (mis. makanan).
- Mata uang Rupiah, format "Rp 1.234.567".
- Jawab singkat (1-3 kalimat), langsung ke poin, Bahasa Indonesia santai. Tanpa markdown.
"""

internal fun buildAskUserContent(question: String, ctx: AskContext): String {
    val cats = ctx.categories.joinToString(",") { """{"kategori":"${it.kategori}","total":${it.total}}""" }
    val accs = ctx.accounts.joinToString(",") { """{"akun":"${it.nama}","saldo":${it.saldo}}""" }
    return """
        DATA (hari ini ${ctx.today}, periode ${ctx.period}):
        {
          "pemasukan_bulan_ini":${ctx.masuk},
          "pengeluaran_bulan_ini":${ctx.keluar},
          "net_bulan_ini":${ctx.net},
          "pemasukan_bulan_lalu":${ctx.prevMasuk},
          "pengeluaran_bulan_lalu":${ctx.prevKeluar},
          "pengeluaran_per_kategori":[$cats],
          "saldo_akun":[$accs]
        }

        PERTANYAAN: $question
    """.trimIndent()
}
