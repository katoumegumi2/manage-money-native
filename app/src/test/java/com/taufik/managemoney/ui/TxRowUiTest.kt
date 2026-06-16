package com.taufik.managemoney.ui

import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.model.AmountStyle
import com.taufik.managemoney.ui.model.toRowUi
import com.taufik.managemoney.ui.util.relativeDateLabel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Logika presentasi murni untuk baris transaksi (tanpa Android/Compose). */
class TxRowUiTest {

    private val today = LocalDate.of(2026, 6, 15)
    private val names = mapOf("1" to "BNI", "2" to "Tunai", "3" to "GoPay")

    private fun tx(
        tipe: TxType,
        nominal: Long,
        akunDari: String? = null,
        akunKe: String? = null,
        kategori: String = "lainnya",
        catatan: String = "",
        tanggal: LocalDate = today,
    ) = Transaction(
        id = "7",
        tipe = tipe,
        nominal = nominal,
        kategori = kategori,
        akunDariId = akunDari,
        akunKeId = akunKe,
        tanggal = tanggal,
        catatan = catatan,
        sumberInput = SourceInput.MANUAL,
        createdAt = 0L,
    )

    @Test
    fun `pengeluaran bertanda minus dan gaya expense`() {
        val row = tx(TxType.PENGELUARAN, 25_000, akunDari = "2", kategori = "makanan", catatan = "Kopi")
            .toRowUi(names, today)
        assertEquals(AmountStyle.EXPENSE, row.style)
        assertEquals("−25.000", row.amountText)
        assertEquals("Kopi", row.title)
        assertEquals("Makanan · Tunai · hari ini", row.subtitle)
    }

    @Test
    fun `pemasukan bertanda plus dan gaya income`() {
        val row = tx(TxType.PEMASUKAN, 8_000_000, akunKe = "1", kategori = "gaji", catatan = "Gaji")
            .toRowUi(names, today.plusDays(1)) // kemarin relatif hari ini tx
        assertEquals(AmountStyle.INCOME, row.style)
        assertEquals("+8.000.000", row.amountText)
        assertEquals("Gaji · BNI · kemarin", row.subtitle)
    }

    @Test
    fun `transfer netral di Beranda tanpa perspektif`() {
        val row = tx(TxType.TRANSFER, 300_000, akunDari = "1", akunKe = "2", catatan = "Tarik tunai")
            .toRowUi(names, today)
        assertEquals(AmountStyle.TRANSFER, row.style)
        assertEquals("300.000", row.amountText) // tanpa tanda
        assertEquals("BNI → Tunai · hari ini", row.subtitle)
    }

    @Test
    fun `transfer keluar dari sudut pandang akun sumber bertanda minus`() {
        val row = tx(TxType.TRANSFER, 300_000, akunDari = "1", akunKe = "2")
            .toRowUi(names, today, perspectiveId = "1")
        assertEquals(AmountStyle.EXPENSE, row.style)
        assertEquals("−300.000", row.amountText)
    }

    @Test
    fun `transfer masuk dari sudut pandang akun tujuan bertanda plus`() {
        val row = tx(TxType.TRANSFER, 300_000, akunDari = "1", akunKe = "2")
            .toRowUi(names, today, perspectiveId = "2")
        assertEquals(AmountStyle.INCOME, row.style)
        assertEquals("+300.000", row.amountText)
    }

    @Test
    fun `judul default memakai kategori bila catatan kosong`() {
        val row = tx(TxType.PENGELUARAN, 5_000, akunDari = "2", kategori = "transport")
            .toRowUi(names, today)
        assertEquals("Transport", row.title)
    }

    @Test
    fun `label tanggal relatif`() {
        assertEquals("hari ini", relativeDateLabel(today, today))
        assertEquals("kemarin", relativeDateLabel(today.minusDays(1), today))
        assertEquals("3 hari lalu", relativeDateLabel(today.minusDays(3), today))
        assertEquals("8 Jun 2026", relativeDateLabel(LocalDate.of(2026, 6, 8), today))
    }
}
