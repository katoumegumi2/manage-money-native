package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.ai.TransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `TransactionParser` aktif yang dipakai aplikasi: baca [AiConfig] terkini lalu delegasikan ke
 * adapter yang sesuai. Ganti provider/key di Pengaturan = langsung berlaku **tanpa rebuild**.
 */
@Singleton
internal class ConfigurableParser @Inject constructor(
    private val geminiApi: GeminiApi,
    private val openAiApi: OpenAiApi,
    private val configStore: AiConfigStore,
) : TransactionParser {

    override suspend fun parse(req: ParseRequest): Result<List<ParsedTransaction>> {
        // Failover: coba provider aktif → provider lain yang punya key. Sukses pertama menang.
        val candidates = configStore.parseCandidates()
        var last: Result<List<ParsedTransaction>>? = null
        for (cfg in candidates) {
            if (cfg.apiKey.isBlank()) continue
            val result = ParserFactory.create(cfg, geminiApi, openAiApi).parse(req)
            if (result.isSuccess) return result
            last = result
        }
        return last ?: Result.failure(ParseException("Belum ada provider AI dengan API key. Atur di Pengaturan."))
    }
}
