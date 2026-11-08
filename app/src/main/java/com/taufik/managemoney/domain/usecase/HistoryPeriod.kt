package com.taufik.managemoney.domain.usecase

import java.time.LocalDate
import java.time.YearMonth

/** Periode Riwayat. Default [MONTH] agar tak memuat seluruh riwayat sekaligus. */
enum class HistoryPeriod { WEEK, MONTH, THREE_MONTHS, YEAR, ALL }

/** Rentang tanggal inklusif untuk [period] relatif [today]; `first` null = tanpa batas bawah (semua). */
fun historyRange(period: HistoryPeriod, today: LocalDate): Pair<LocalDate?, LocalDate> = when (period) {
    HistoryPeriod.WEEK -> today.minusDays(6) to today
    HistoryPeriod.MONTH -> YearMonth.from(today).atDay(1) to today
    HistoryPeriod.THREE_MONTHS -> YearMonth.from(today).minusMonths(2).atDay(1) to today
    HistoryPeriod.YEAR -> today.withDayOfYear(1) to today
    HistoryPeriod.ALL -> null to today
}
