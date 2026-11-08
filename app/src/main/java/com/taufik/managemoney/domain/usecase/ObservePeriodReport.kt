package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/** Periode Laporan. */
enum class ReportPeriod { WEEK, MONTH, SIX_MONTHS, YEAR }

/** Satu titik grafik arus kas. [label] ringkas (mis. "Sen", "8–14", "Jan"). [current] = bucket berjalan. */
data class FlowBucket(val label: String, val masuk: Long, val keluar: Long, val current: Boolean)

data class PeriodReport(
    val period: ReportPeriod,
    val start: LocalDate,
    val end: LocalDate,
    val masuk: Long,
    val keluar: Long,
    val buckets: List<FlowBucket>,
    /** Pengeluaran per kategori dalam periode, urut terbesar. */
    val categories: List<CategorySum>,
) {
    val net: Long get() = masuk - keluar
    val isEmpty: Boolean get() = masuk == 0L && keluar == 0L
}

private val localeId = Locale.forLanguageTag("id-ID")

private fun shortDay(d: DayOfWeek) = d.getDisplayName(TextStyle.SHORT, localeId).take(3).replaceFirstChar { it.titlecase(localeId) }
private fun shortMonth(ym: YearMonth) = ym.month.getDisplayName(TextStyle.SHORT, localeId).take(3).replaceFirstChar { it.titlecase(localeId) }

/**
 * Rangkum transaksi untuk [period] relatif terhadap [today]. Murni (tanpa I/O) agar mudah diuji.
 * - WEEK: 7 hari terakhir (per hari). MONTH: bulan berjalan (per pekan 1–7, 8–14, …).
 * - SIX_MONTHS / YEAR: 6 / 12 bulan terakhir termasuk bulan berjalan (per bulan).
 * Transfer tidak dihitung sebagai masuk/keluar.
 */
fun buildPeriodReport(txs: List<Transaction>, period: ReportPeriod, today: LocalDate): PeriodReport {
    data class Range(val label: String, val from: LocalDate, val to: LocalDate, val current: Boolean)
    val ranges: List<Range> = when (period) {
        ReportPeriod.WEEK -> (6 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            Range(shortDay(d.dayOfWeek), d, d, back == 0)
        }
        ReportPeriod.MONTH -> {
            val ym = YearMonth.from(today)
            (1..ym.lengthOfMonth() step 7).map { startDay ->
                val endDay = minOf(startDay + 6, ym.lengthOfMonth())
                val from = ym.atDay(startDay); val to = ym.atDay(endDay)
                Range("$startDay–$endDay", from, to, !today.isBefore(from) && !today.isAfter(to))
            }
        }
        ReportPeriod.SIX_MONTHS, ReportPeriod.YEAR -> {
            val n = if (period == ReportPeriod.YEAR) 12 else 6
            val first = YearMonth.from(today).minusMonths((n - 1).toLong())
            (0 until n).map { i ->
                val ym = first.plusMonths(i.toLong())
                Range(shortMonth(ym), ym.atDay(1), ym.atEndOfMonth(), i == n - 1)
            }
        }
    }
    val start = ranges.first().from
    val end = ranges.last().to
    val inRange = txs.filter { !it.tanggal.isBefore(start) && !it.tanggal.isAfter(end) }
    val buckets = ranges.map { r ->
        val sub = inRange.filter { !it.tanggal.isBefore(r.from) && !it.tanggal.isAfter(r.to) }
        FlowBucket(
            label = r.label,
            masuk = sub.filter { it.tipe == TxType.PEMASUKAN }.sumOf { it.nominal },
            keluar = sub.filter { it.tipe == TxType.PENGELUARAN }.sumOf { it.nominal },
            current = r.current,
        )
    }
    val categories = inRange.filter { it.tipe == TxType.PENGELUARAN }
        .groupBy { it.kategori }
        .map { (k, list) -> CategorySum(k, list.sumOf { it.nominal }) }
        .sortedByDescending { it.total }
    return PeriodReport(
        period = period,
        start = start,
        end = end,
        masuk = buckets.sumOf { it.masuk },
        keluar = buckets.sumOf { it.keluar },
        buckets = buckets,
        categories = categories,
    )
}

/** Laporan per periode dari seluruh transaksi (reaktif). */
class ObservePeriodReport @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(period: ReportPeriod, today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))): Flow<PeriodReport> {
        // Hanya baca transaksi dalam rentang periode (bukan seluruh riwayat), hitung di luar thread UI.
        val probe = buildPeriodReport(emptyList(), period, today)
        return transactionRepository.observeBetween(probe.start, probe.end)
            .map { buildPeriodReport(it, period, today) }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
}
