package com.taufik.managemoney.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun `format ribuan pakai titik`() {
        assertEquals("Rp 1.234.567", formatRupiah(1_234_567))
    }

    @Test
    fun `format nol`() {
        assertEquals("Rp 0", formatRupiah(0))
    }

    @Test
    fun `format ratusan tanpa pemisah`() {
        assertEquals("Rp 500", formatRupiah(500))
    }

    @Test
    fun `format negatif`() {
        assertEquals("-Rp 25.000", formatRupiah(-25_000))
    }

    @Test
    fun `format jutaan`() {
        assertEquals("Rp 8.000.000", formatRupiah(8_000_000))
    }

    @Test
    fun `format ringkas jutaan ribuan dan satuan`() {
        assertEquals("9,3 jt", formatShort(9_278_000))
        assertEquals("4 jt", formatShort(4_034_000))
        assertEquals("28 jt", formatShort(27_981_000))
        assertEquals("513 rb", formatShort(513_000))
        assertEquals("999", formatShort(999))
        assertEquals("2 rb", formatShort(1_500))
        assertEquals("800", formatShort(800))
        assertEquals("−746 rb", formatShort(-746_000))
    }
}
