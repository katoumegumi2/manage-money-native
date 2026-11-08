package com.taufik.managemoney.domain

import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.ReportPeriod
import com.taufik.managemoney.domain.usecase.buildPeriodReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PeriodReportTest {
    private val today = LocalDate.of(2026, 11, 8)
    private fun tx(tipe: TxType, n: Long, d: LocalDate, kat: String = "makanan") =
        Transaction(tipe = tipe, nominal = n, kategori = kat, akunDariId = "a", akunKeId = "b", tanggal = d, sumberInput = SourceInput.MANUAL)

    private val data = listOf(
        tx(TxType.PENGELUARAN, 38_000, today),
        tx(TxType.PENGELUARAN, 122_000, today.minusDays(1), "belanja"),
        tx(TxType.PEMASUKAN, 50_000, today.minusDays(1), "refund"),
        tx(TxType.PENGELUARAN, 500_000, LocalDate.of(2026, 10, 15)),
        tx(TxType.PEMASUKAN, 4_000_000, LocalDate.of(2026, 7, 1), "gaji"),
        tx(TxType.TRANSFER, 999_000, today, "lainnya"),
        tx(TxType.PENGELUARAN, 1_000, LocalDate.of(2025, 11, 30)), // di luar 12 bulan terakhir
    )

    @Test
    fun `minggu = 7 hari terakhir per hari, transfer tak dihitung`() {
        val r = buildPeriodReport(data, ReportPeriod.WEEK, today)
        assertEquals(7, r.buckets.size)
        assertEquals(160_000L, r.keluar)
        assertEquals(50_000L, r.masuk)
        assertTrue(r.buckets.last().current)
        assertEquals(38_000L, r.buckets.last().keluar)
        assertEquals(today.minusDays(6), r.start)
    }

    @Test
    fun `bulan = bulan berjalan per pekan`() {
        val r = buildPeriodReport(data, ReportPeriod.MONTH, today)
        assertEquals(listOf("1–7", "8–14", "15–21", "22–28", "29–30"), r.buckets.map { it.label })
        assertTrue(r.buckets[1].current)
        assertEquals(38_000L, r.buckets[1].keluar)   // 8 Nov
        assertEquals(122_000L, r.buckets[0].keluar)  // 7 Nov masuk pekan 1–7
        assertEquals(160_000L, r.keluar)
    }

    @Test
    fun `enam bulan dan setahun per bulan, kategori urut terbesar`() {
        val six = buildPeriodReport(data, ReportPeriod.SIX_MONTHS, today)
        assertEquals(6, six.buckets.size)
        assertEquals(4_050_000L, six.masuk)
        assertEquals(660_000L, six.keluar)
        assertEquals(listOf("makanan", "belanja"), six.categories.map { it.kategori })
        assertEquals(538_000L, six.categories.first().total)
        val year = buildPeriodReport(data, ReportPeriod.YEAR, today)
        assertEquals(12, year.buckets.size)
        assertEquals(660_000L, year.keluar) // 30 Nov 2025 di luar rentang (mulai Des 2025)
    }
}
