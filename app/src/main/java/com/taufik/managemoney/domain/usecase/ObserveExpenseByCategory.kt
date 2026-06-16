package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.CategorySum
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/** Rekap pengeluaran per kategori bulan berjalan (laporan di Riwayat). */
class ObserveExpenseByCategory @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    private val zone = ZoneId.of("Asia/Jakarta")

    operator fun invoke(today: LocalDate = LocalDate.now(zone)): Flow<List<CategorySum>> {
        val sinceIso = today.withDayOfMonth(1).toString()
        return transactionRepository.observeExpenseByCategorySince(sinceIso)
    }
}
