package com.taufik.managemoney.data.ai

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.usecase.ObserveBalances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Eksekutor tool Tanya AI: ambil snapshot seluruh transaksi + saldo dari DB, lalu jalankan tool
 * via [ChatToolLogic]. Hanya dipanggil saat model meminta data (function calling) → tidak ada
 * data dikirim ke AI bila tak diperlukan.
 */
@Singleton
internal class ChatTools @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val observeBalances: ObserveBalances,
) {
    suspend fun dispatch(name: String, args: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        val all = transactionRepository.observeAll().first()
        val balances = observeBalances().first()
        ChatToolLogic.dispatch(name, args, all, balances)
    }
}
