package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/** Satu titik tren: bulan + total masuk & keluar. [label] ringkas mis. "Jan". */
data class MonthlyPoint(val ym: String, val label: String, val masuk: Long, val keluar: Long)

/**
 * Tren [months] bulan terakhir (termasuk bulan berjalan), masuk vs keluar.
 * Bulan tanpa transaksi tetap muncul sebagai 0 agar grafik berkesinambungan.
 */
class ObserveMonthlyTrend @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    private val zone = ZoneId.of("Asia/Jakarta")
    private val localeId = Locale.forLanguageTag("id-ID")

    operator fun invoke(today: LocalDate = LocalDate.now(zone), months: Int = 6): Flow<List<MonthlyPoint>> {
        val firstMonth = YearMonth.from(today).minusMonths((months - 1).toLong())
        val sinceIso = firstMonth.atDay(1).toString()
        return transactionRepository.observeMonthlyTotals(sinceIso).map { totals ->
            val masukByMonth = totals.filter { it.tipe == TxType.PEMASUKAN }.associate { it.ym to it.total }
            val keluarByMonth = totals.filter { it.tipe == TxType.PENGELUARAN }.associate { it.ym to it.total }
            (0 until months).map { offset ->
                val ym = firstMonth.plusMonths(offset.toLong())
                val key = ym.toString() // "YYYY-MM"
                MonthlyPoint(
                    ym = key,
                    label = ym.month.getDisplayName(TextStyle.SHORT, localeId),
                    masuk = masukByMonth[key] ?: 0L,
                    keluar = keluarByMonth[key] ?: 0L,
                )
            }
        }
    }
}
