package com.taufik.managemoney.domain

import com.taufik.managemoney.domain.usecase.HistoryPeriod
import com.taufik.managemoney.domain.usecase.historyRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HistoryPeriodTest {
    private val today = LocalDate.of(2026, 11, 8)

    @Test
    fun `rentang tiap periode`() {
        assertEquals(LocalDate.of(2026, 11, 2) to today, historyRange(HistoryPeriod.WEEK, today))
        assertEquals(LocalDate.of(2026, 11, 1) to today, historyRange(HistoryPeriod.MONTH, today))
        assertEquals(LocalDate.of(2026, 9, 1) to today, historyRange(HistoryPeriod.THREE_MONTHS, today))
        assertEquals(LocalDate.of(2026, 1, 1) to today, historyRange(HistoryPeriod.YEAR, today))
        assertNull(historyRange(HistoryPeriod.ALL, today).first)
    }
}
