package com.taufik.managemoney.data.ai

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.time.LocalDate

/** Adapter OpenAI-compatible (DeepSeek dsb) via MockWebServer — tanpa device/jaringan nyata. */
class OpenAiCompatibleParserTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenAiApi
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val codes = listOf("tunai", "gopay", "bni")
    private val today = LocalDate.of(2026, 6, 16)

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    /** Balasan chat completions di mana `message.content` = string [content]. Escaping ditangani serializer. */
    private fun responseWith(content: String) = MockResponse().setBody(
        json.encodeToString(ChatResponse(listOf(ChatChoice(ChatMessage("assistant", content))))),
    )

    private fun parser(apiKey: String = "test-key", baseUrl: String = server.url("/v1/chat/completions").toString()) =
        OpenAiCompatibleParser(api = api, baseUrl = baseUrl, apiKey = apiKey, model = "deepseek-chat")

    private fun req(text: String = "kopi 25 ribu dari tunai") =
        ParseRequest(text = text, today = today, accountCodes = codes)

    @Test
    fun `balasan chat-completions terparse jadi transaksi`() = runTest {
        val content = """[{"tipe":"pengeluaran","nominal":25000,"kategori":"makanan",""" +
            """"akun_dari":"tunai","akun_ke":null,"tanggal":"2026-06-16","catatan":"kopi","perlu_dicek":false}]"""
        server.enqueue(responseWith(content))

        val result = parser().parse(req())

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals(TxType.PENGELUARAN, list[0].tipe)
        assertEquals(25_000L, list[0].nominal)
        assertEquals("tunai", list[0].akunDari)

        // request benar: POST ke path, Authorization Bearer, body bawa model + pesan system & user
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/v1/chat/completions", recorded.path)
        assertEquals("Bearer test-key", recorded.getHeader("Authorization"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("deepseek-chat"))
        assertTrue(body.contains("\"system\""))
        assertTrue(body.contains("\"user\""))
    }

    @Test
    fun `fence json di-strip lalu terparse`() = runTest {
        server.enqueue(responseWith("```json\n[]\n```"))
        val result = parser().parse(req())
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `key kosong gagal tanpa memanggil server`() = runTest {
        val result = parser(apiKey = "").parse(req())
        assertTrue(result.isFailure)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `balasan bukan transaksi valid jadi failure`() = runTest {
        // kategori 'ngawur' tak sesuai tipe → validator menolak
        val content = """[{"tipe":"pengeluaran","nominal":1000,"kategori":"ngawur",""" +
            """"akun_dari":"tunai","akun_ke":null,"tanggal":"2026-06-16","catatan":"x","perlu_dicek":false}]"""
        server.enqueue(responseWith(content))
        val result = parser().parse(req())
        assertTrue(result.isFailure)
    }

    @Test
    fun `error http jadi failure bukan crash`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val result = parser().parse(req())
        assertTrue(result.isFailure)
    }

    @Test
    fun `preset provider konsisten`() {
        assertFalse(AiProvider.GEMINI.openAiCompatible)
        assertTrue(AiProvider.DEEPSEEK.openAiCompatible)
        assertTrue(AiProvider.DEEPSEEK.defaultBaseUrl.isNotBlank())
        assertEquals("deepseek-chat", AiProvider.DEEPSEEK.defaultModel)
    }
}
