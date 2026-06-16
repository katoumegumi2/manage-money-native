package com.taufik.managemoney.data.repo

import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.ai.TransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Jembatan UI → parser: lengkapi konteks (hari ini WIB + kode akun aktif) lalu panggil [TransactionParser].
 * UI hanya tahu teks masuk → `Result<List<ParsedTransaction>>`; provider tetap tersembunyi.
 */
@Singleton
class ParseRepository @Inject constructor(
    private val parser: TransactionParser,
    private val accountRepository: AccountRepository,
) {
    suspend fun parse(text: String): Result<List<ParsedTransaction>> {
        val codes = accountRepository.observeActiveAccounts().first().map { it.kode }
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        return parser.parse(ParseRequest(text = text, today = today, accountCodes = codes))
    }
}
