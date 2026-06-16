package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.TransactionParser

/** Bangun adapter parser sesuai [AiConfig]. Satu titik pemilihan provider (brief §3.4). */
internal object ParserFactory {
    fun create(cfg: AiConfig, geminiApi: GeminiApi, openAiApi: OpenAiApi): TransactionParser =
        if (cfg.provider.openAiCompatible) {
            OpenAiCompatibleParser(
                api = openAiApi,
                baseUrl = cfg.baseUrl,
                apiKey = cfg.apiKey,
                model = cfg.model,
            )
        } else {
            GeminiParser(api = geminiApi, apiKey = cfg.apiKey, model = cfg.model)
        }
}
