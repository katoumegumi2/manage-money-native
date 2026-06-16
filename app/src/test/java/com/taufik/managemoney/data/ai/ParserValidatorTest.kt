package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validator §3.5 — bersihkan output model → parse JSON → validasi per aturan. */
class ParserValidatorTest {

    private val accounts = setOf("bni", "mandiri", "tunai", "gopay", "shopeepay", "seabank")

    private fun el(
        tipe: String = "pengeluaran",
        nominal: Long = 25000,
        kategori: String = "makanan",
        akunDari: String? = null,
        akunKe: String? = null,
        tanggal: String = "2026-06-16",
        perluDicek: Boolean = false,
    ): String = """
        {"tipe":"$tipe","nominal":$nominal,"kategori":"$kategori",
         "akun_dari":${akunDari?.let { "\"$it\"" } ?: "null"},
         "akun_ke":${akunKe?.let { "\"$it\"" } ?: "null"},
         "tanggal":"$tanggal","catatan":"x","perlu_dicek":$perluDicek}
    """.trimIndent()

    private fun parse(raw: String) = ParserValidator.parse(raw, accounts)

    @Test
    fun `array json bersih ter-parse`() {
        val result = parse("[${el()}]")
        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals(TxType.PENGELUARAN, list[0].tipe)
        assertEquals(25000L, list[0].nominal)
    }

    @Test
    fun `blok markdown json di-strip`() {
        val raw = "```json\n[${el()}]\n```"
        assertTrue(parse(raw).isSuccess)
    }

    @Test
    fun `blok markdown polos di-strip`() {
        val raw = "```\n[${el()}]\n```"
        assertTrue(parse(raw).isSuccess)
    }

    @Test
    fun `array kosong sukses dengan list kosong`() {
        val result = parse("[]")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `multi elemen ter-parse semua`() {
        val raw = "[${el(nominal = 30000)},${el(kategori = "transport", nominal = 5000)}]"
        assertEquals(2, parse(raw).getOrThrow().size)
    }

    @Test
    fun `kategori tidak sesuai tipe ditolak`() {
        // pengeluaran tak boleh kategori "gaji"
        assertTrue(parse("[${el(kategori = "gaji")}]").isFailure)
    }

    @Test
    fun `akun di luar daftar ditolak`() {
        assertTrue(parse("[${el(akunDari = "bca")}]").isFailure)
    }

    @Test
    fun `akun dalam daftar diterima`() {
        assertTrue(parse("[${el(akunDari = "bni")}]").isSuccess)
    }

    @Test
    fun `nominal negatif ditolak`() {
        assertTrue(parse("[${el(nominal = -1000)}]").isFailure)
    }

    @Test
    fun `nominal nol diterima (perlu dicek)`() {
        // §3.5 nominal >= 0 valid; 0 dipakai saat nominal tak terdeteksi
        assertTrue(parse("[${el(nominal = 0, perluDicek = true)}]").isSuccess)
    }

    @Test
    fun `tanggal format salah ditolak`() {
        assertTrue(parse("[${el(tanggal = "16-06-2026")}]").isFailure)
    }

    @Test
    fun `teks bukan json ditolak`() {
        assertTrue(parse("halo aku bukan json").isFailure)
    }

    @Test
    fun `transfer dengan kategori lainnya diterima`() {
        val raw = "[${el(tipe = "transfer", kategori = "lainnya", akunDari = "bni", akunKe = "tunai")}]"
        assertTrue(parse(raw).isSuccess)
    }

    @Test
    fun `transfer kategori selain lainnya ditolak`() {
        val raw = "[${el(tipe = "transfer", kategori = "makanan", akunDari = "bni", akunKe = "tunai")}]"
        assertTrue(parse(raw).isFailure)
    }
}
