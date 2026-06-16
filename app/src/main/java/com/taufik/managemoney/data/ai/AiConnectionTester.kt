package com.taufik.managemoney.data.ai

import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "Tes parse" di layar Pengaturan: jalankan satu kalimat uji lewat [config] yang sedang diisi
 * (belum tentu tersimpan), untuk memastikan key/baseUrl/model valid sebelum disimpan.
 */
@Singleton
class AiConnectionTester @Inject internal constructor(
    private val geminiApi: GeminiApi,
    private val openAiApi: OpenAiApi,
    private val accountRepository: AccountRepository,
) {
    suspend fun test(
        config: AiConfig,
        sample: String = "kopi 25 ribu dari tunai",
    ): Result<List<ParsedTransaction>> {
        val codes = accountRepository.observeActiveAccounts().first().map { it.kode }
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        return ParserFactory.create(config, geminiApi, openAiApi)
            .parse(ParseRequest(text = sample, today = today, accountCodes = codes))
    }
}
