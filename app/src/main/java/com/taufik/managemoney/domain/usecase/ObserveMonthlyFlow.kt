package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Ringkasan uang masuk (pemasukan) & keluar (pengeluaran) bulan berjalan. Transfer dikecualikan (net-zero). */
data class MonthlyFlow(val masuk: Long, val keluar: Long)

class ObserveMonthlyFlow @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    private val zone = ZoneId.of("Asia/Jakarta")

    operator fun invoke(today: LocalDate = LocalDate.now(zone)): Flow<MonthlyFlow> {
        val sinceIso = today.withDayOfMonth(1).toString()
        return combine(
            transactionRepository.observeSumByTypeSince(TxType.PEMASUKAN, sinceIso),
            transactionRepository.observeSumByTypeSince(TxType.PENGELUARAN, sinceIso),
        ) { masuk, keluar -> MonthlyFlow(masuk, keluar) }
    }
}
