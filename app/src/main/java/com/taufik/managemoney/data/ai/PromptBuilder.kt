package com.taufik.managemoney.data.ai

import java.time.LocalDate

/** Isi placeholder system instruction parser (brief §3.6). Murni → mudah dites. */
object PromptBuilder {
    fun build(today: LocalDate, accountCodes: List<String>): String =
        PARSER_SYSTEM_INSTRUCTION
            .replace("{{HARI_INI}}", today.toString())
            .replace("{{DAFTAR_AKUN}}", accountCodes.joinToString(", "))
            .trim()
}
