package com.taufik.managemoney.domain

import com.taufik.managemoney.domain.model.DraftIssue
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.TransactionDraft
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.model.issues
import com.taufik.managemoney.domain.model.toTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Aturan simpan §4.3 — validator murni + builder draft→Transaction. */
class DraftValidationTest {

    private val today = LocalDate.of(2026, 6, 16)

    private fun draft(
        tipe: TxType,
        nominal: Long = 10_000,
        akunDariId: String? = null,
        akunKeId: String? = null,
        kategori: String = "lainnya",
    ) = TransactionDraft(
        tipe = tipe,
        nominal = nominal,
        kategori = kategori,
        akunDariId = akunDariId,
        akunKeId = akunKeId,
        tanggal = today,
        catatan = "",
    )

    // --- validasi ---

    @Test
    fun `pengeluaran dengan akun dari dan nominal positif valid`() {
        val issues = draft(TxType.PENGELUARAN, akunDariId = "1").issues()
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `pengeluaran tanpa akun dari ditolak`() {
        val issues = draft(TxType.PENGELUARAN, akunDariId = null).issues()
        assertEquals(setOf(DraftIssue.AKUN_DARI_WAJIB), issues)
    }

    @Test
    fun `pemasukan tanpa akun ke ditolak`() {
        val issues = draft(TxType.PEMASUKAN, akunKeId = null).issues()
        assertEquals(setOf(DraftIssue.AKUN_KE_WAJIB), issues)
    }

    @Test
    fun `pemasukan dengan akun ke valid`() {
        val issues = draft(TxType.PEMASUKAN, akunKeId = "2").issues()
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `transfer dengan dua akun berbeda valid`() {
        val issues = draft(TxType.TRANSFER, akunDariId = "1", akunKeId = "2").issues()
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `transfer dengan akun dari sama dengan akun ke ditolak`() {
        val issues = draft(TxType.TRANSFER, akunDariId = "1", akunKeId = "1").issues()
        assertEquals(setOf(DraftIssue.TRANSFER_AKUN_SAMA), issues)
    }

    @Test
    fun `transfer tanpa salah satu akun ditolak`() {
        val issues = draft(TxType.TRANSFER, akunDariId = "1", akunKeId = null).issues()
        assertEquals(setOf(DraftIssue.AKUN_KE_WAJIB), issues)
    }

    @Test
    fun `nominal nol ditolak`() {
        val issues = draft(TxType.PENGELUARAN, nominal = 0, akunDariId = "1").issues()
        assertEquals(setOf(DraftIssue.NOMINAL_TIDAK_VALID), issues)
    }

    @Test
    fun `nominal negatif ditolak`() {
        val issues = draft(TxType.PEMASUKAN, nominal = -5_000, akunKeId = "2").issues()
        assertEquals(setOf(DraftIssue.NOMINAL_TIDAK_VALID), issues)
    }

    // --- builder draft→Transaction (normalisasi akun per tipe) ---

    @Test
    fun `build pengeluaran menormalkan akun ke jadi null`() {
        // draft "kotor": pengeluaran tapi punya akunKe nyasar — harus dibuang
        val d = draft(TxType.PENGELUARAN, akunDariId = "1", akunKeId = "9")
        val tx = d.toTransaction(batchId = "b1", sumber = SourceInput.MANUAL, createdAt = 0L)
        assertEquals("1", tx.akunDariId)
        assertNull(tx.akunKeId)
        assertEquals("b1", tx.batchId)
        assertEquals(SourceInput.MANUAL, tx.sumberInput)
    }

    @Test
    fun `build pemasukan menormalkan akun dari jadi null`() {
        val d = draft(TxType.PEMASUKAN, akunDariId = "9", akunKeId = "2")
        val tx = d.toTransaction(batchId = null, sumber = SourceInput.MANUAL, createdAt = 0L)
        assertNull(tx.akunDariId)
        assertEquals("2", tx.akunKeId)
    }

    @Test
    fun `build transfer mempertahankan kedua akun`() {
        val d = draft(TxType.TRANSFER, akunDariId = "1", akunKeId = "2")
        val tx = d.toTransaction(batchId = "b1", sumber = SourceInput.MANUAL, createdAt = 0L)
        assertEquals("1", tx.akunDariId)
        assertEquals("2", tx.akunKeId)
    }

    @Test
    fun `draft valid tidak punya issue`() {
        assertFalse(draft(TxType.TRANSFER, akunDariId = "1", akunKeId = "2").issues().isNotEmpty())
    }
}
