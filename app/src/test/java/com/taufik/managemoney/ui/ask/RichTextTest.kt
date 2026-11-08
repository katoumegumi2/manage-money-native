package com.taufik.managemoney.ui.ask

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test

class RichTextTest {
    @Test
    fun `tanda bintang ganda jadi tebal dan tidak tampil`() {
        val r = richText("Pengeluaran **Rp 961.000**, net **−Rp 746.000**.")
        assertEquals("Pengeluaran Rp 961.000, net −Rp 746.000.", r.text)
        assertEquals(2, r.spanStyles.size)
        assertEquals(FontWeight.Bold, r.spanStyles[0].item.fontWeight)
        assertEquals("Rp 961.000", r.text.substring(r.spanStyles[0].start, r.spanStyles[0].end))
    }

    @Test
    fun `bintang tanpa pasangan dibiarkan apa adanya`() {
        assertEquals("harga **naik", richText("harga **naik").text)
    }
}
