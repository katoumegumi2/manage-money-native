package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Eksekusi tool Tanya AI (function calling) — logika query/filter murni. */
class ChatToolLogicTest {

    private val balances = listOf(
        AccountBalance("a1", "bni", "BNI", AccountType.BANK, saldo = 1_000_000),
        AccountBalance("a2", "gopay", "GoPay", AccountType.EWALLET, saldo = 250_000),
    )

    private fun tx(
        tipe: TxType,
        nominal: Long,
        kategori: String,
        dari: String?,
        ke: String?,
        tanggal: String,
        catatan: String,
    ) = Transaction(
        id = catatan,
        tipe = tipe,
        nominal = nominal,
        kategori = kategori,
        akunDariId = dari,
        akunKeId = ke,
        tanggal = LocalDate.parse(tanggal),
        catatan = catatan,
        sumberInput = SourceInput.MANUAL,
    )

    private val all = listOf(
        tx(TxType.PENGELUARAN, 25_000, "makanan", "a1", null, "2026-06-10", "kopi di Tuku"),
        tx(TxType.PENGELUARAN, 500_000, "lainnya", "a1", null, "2026-01-15", "pinjamin Budi"),
        tx(TxType.PEMASUKAN, 100_000, "lainnya", null, "a1", "2026-03-01", "Budi bayar utang"),
        tx(TxType.TRANSFER, 50_000, "lainnya", "a1", "a2", "2026-06-12", "topup gopay"),
    )

    private fun call(name: String, args: JsonObject = JsonObject(emptyMap())) =
        ChatToolLogic.dispatch(name, args, all, balances)

    private fun JsonObject.int(key: String) = this[key]!!.jsonPrimitive.int
    private fun JsonObject.long(key: String) = this[key]!!.jsonPrimitive.long

    @Test
    fun `saldo_akun = total semua akun`() {
        val r = call("saldo_akun")
        assertEquals(1_250_000L, r.long("total"))
    }

    @Test
    fun `cari kata kunci budi cocok dua transaksi (utang & pelunasan)`() {
        val r = call("cari_transaksi", buildJsonObject { put("kata_kunci", "budi") })
        assertEquals(2, r.int("jumlah_cocok"))
    }

    @Test
    fun `cari filter tipe pengeluaran`() {
        val r = call("cari_transaksi", buildJsonObject { put("tipe", "pengeluaran") })
        assertEquals(2, r.int("jumlah_cocok"))
    }

    @Test
    fun `cari filter akun gopay termasuk transfer`() {
        val r = call("cari_transaksi", buildJsonObject { put("akun", "gopay") })
        assertEquals(1, r.int("jumlah_cocok"))
    }

    @Test
    fun `cari rentang tanggal menjangkau hanya yang sesuai`() {
        val r = call(
            "cari_transaksi",
            buildJsonObject { put("dari_tanggal", "2026-06-01"); put("sampai_tanggal", "2026-06-30") },
        )
        assertEquals(2, r.int("jumlah_cocok")) // kopi + topup gopay
    }

    @Test
    fun `cari filter kategori makanan`() {
        val r = call("cari_transaksi", buildJsonObject { put("kategori", "makanan") })
        assertEquals(1, r.int("jumlah_cocok"))
    }

    @Test
    fun `ringkas keuangan hitung masuk keluar net`() {
        val r = call(
            "ringkas_keuangan",
            buildJsonObject { put("dari_tanggal", "2026-01-01"); put("sampai_tanggal", "2026-12-31") },
        )
        assertEquals(100_000L, r.long("pemasukan"))
        assertEquals(525_000L, r.long("pengeluaran")) // 25.000 + 500.000 (transfer tak dihitung)
        assertEquals(-425_000L, r.long("net"))
    }

    @Test
    fun `tool tak dikenal balas error`() {
        val r = call("ngawur")
        assertEquals(true, r.containsKey("error"))
    }
}
