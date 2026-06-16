package com.taufik.managemoney.data.export

import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CsvExporterTest {

    private val kodeById = mapOf(1L to "bni", 2L to "tunai")

    private fun tx(
        tipe: TxType,
        nominal: Long,
        dari: Long? = null,
        ke: Long? = null,
        kategori: String = "lainnya",
        catatan: String = "",
    ) = Transaction(
        id = 1, tipe = tipe, nominal = nominal, kategori = kategori,
        akunDariId = dari, akunKeId = ke, tanggal = LocalDate.of(2026, 6, 16),
        catatan = catatan, sumberInput = SourceInput.MANUAL, createdAt = 0L,
    )

    @Test
    fun `header dan baris benar`() {
        val csv = CsvExporter.export(
            listOf(tx(TxType.PENGELUARAN, 25_000, dari = 1, kategori = "makanan", catatan = "kopi")),
            kodeById,
        )
        val lines = csv.trim().split("\r\n")
        assertEquals("tanggal,tipe,nominal,kategori,akun_dari,akun_ke,catatan", lines[0])
        assertEquals("2026-06-16,pengeluaran,25000,makanan,bni,,kopi", lines[1])
    }

    @Test
    fun `catatan dengan koma dan kutip di-escape`() {
        val csv = CsvExporter.export(
            listOf(tx(TxType.PEMASUKAN, 1000, ke = 1, kategori = "gaji", catatan = """beli "kopi", roti""")),
            kodeById,
        )
        val line = csv.trim().split("\r\n")[1]
        assertTrue(line.endsWith(""","beli ""kopi"", roti""""))
    }

    @Test
    fun `transfer punya akun dari dan ke`() {
        val csv = CsvExporter.export(
            listOf(tx(TxType.TRANSFER, 300_000, dari = 1, ke = 2)),
            kodeById,
        )
        val line = csv.trim().split("\r\n")[1]
        assertEquals("2026-06-16,transfer,300000,lainnya,bni,tunai,", line)
    }
}
