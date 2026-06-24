package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.util.Locale

/**
 * Eksekusi tool Tanya AI — MURNI (tanpa I/O) agar mudah dites. [ChatTools] yang mengambil snapshot
 * transaksi + saldo dari DB lalu memanggil [dispatch]. Hasil = [JsonObject] untuk dibalas ke model.
 */
internal object ChatToolLogic {

    private val locale = Locale.forLanguageTag("id-ID")

    fun dispatch(
        name: String,
        args: JsonObject,
        all: List<Transaction>,
        balances: List<AccountBalance>,
    ): JsonObject = when (name) {
        "saldo_akun" -> saldoAkun(balances)
        "ringkas_keuangan" -> ringkasKeuangan(all, args)
        "cari_transaksi" -> cariTransaksi(all, balances, args)
        else -> buildJsonObject { put("error", "tool tidak dikenal: $name") }
    }

    private fun saldoAkun(balances: List<AccountBalance>): JsonObject = buildJsonObject {
        put(
            "akun",
            buildJsonArray {
                balances.forEach { b ->
                    addJsonObject {
                        put("nama", b.nama)
                        put("saldo", b.saldo)
                    }
                }
            },
        )
        put("total", balances.sumOf { it.saldo })
    }

    private fun ringkasKeuangan(all: List<Transaction>, args: JsonObject): JsonObject {
        val dari = args.dateOrNull("dari_tanggal")
        val sampai = args.dateOrNull("sampai_tanggal")
        val inRange = all.filter { it.tanggal.inRange(dari, sampai) }
        val masuk = inRange.filter { it.tipe == TxType.PEMASUKAN }.sumOf { it.nominal }
        val keluar = inRange.filter { it.tipe == TxType.PENGELUARAN }.sumOf { it.nominal }
        val perKategori = inRange.filter { it.tipe == TxType.PENGELUARAN }
            .groupBy { it.kategori }
            .map { (k, list) -> k to list.sumOf { it.nominal } }
            .sortedByDescending { it.second }
        return buildJsonObject {
            put("dari_tanggal", dari?.toString() ?: "awal")
            put("sampai_tanggal", sampai?.toString() ?: "kini")
            put("pemasukan", masuk)
            put("pengeluaran", keluar)
            put("net", masuk - keluar)
            put(
                "pengeluaran_per_kategori",
                buildJsonArray {
                    perKategori.forEach { (k, total) ->
                        addJsonObject {
                            put("kategori", k)
                            put("total", total)
                        }
                    }
                },
            )
        }
    }

    private fun cariTransaksi(
        all: List<Transaction>,
        balances: List<AccountBalance>,
        args: JsonObject,
    ): JsonObject {
        val nameById = balances.associate { it.accountId to it.nama }
        val kata = args.stringOrNull("kata_kunci")?.lowercase(locale)
        val dari = args.dateOrNull("dari_tanggal")
        val sampai = args.dateOrNull("sampai_tanggal")
        val tipe = args.stringOrNull("tipe")?.lowercase(locale)
        val kategori = args.stringOrNull("kategori")?.lowercase(locale)
        val akun = args.stringOrNull("akun")?.lowercase(locale)
        val limit = (args["limit"]?.jsonPrimitive?.intOrNull ?: 50).coerceIn(1, 100)

        // Resolusi argumen "akun" → himpunan id akun yang nama/kode-nya cocok.
        val akunIds: Set<String>? = akun?.let { needle ->
            balances.filter {
                it.nama.lowercase(locale).contains(needle) || it.kode.lowercase(locale).contains(needle)
            }.map { it.accountId }.toSet()
        }

        val matched = all.filter { tx ->
            val dariNama = tx.akunDariId?.let { nameById[it] }.orEmpty().lowercase(locale)
            val keNama = tx.akunKeId?.let { nameById[it] }.orEmpty().lowercase(locale)
            (kata == null ||
                tx.catatan.lowercase(locale).contains(kata) ||
                tx.kategori.lowercase(locale).contains(kata) ||
                dariNama.contains(kata) || keNama.contains(kata)) &&
                tx.tanggal.inRange(dari, sampai) &&
                (tipe == null || tx.tipe.wire == tipe) &&
                (kategori == null || tx.kategori.lowercase(locale) == kategori) &&
                (akunIds == null || tx.akunDariId in akunIds || tx.akunKeId in akunIds)
        }

        val shown = matched.take(limit)
        return buildJsonObject {
            put("jumlah_cocok", matched.size)
            put("ditampilkan", shown.size)
            put(
                "transaksi",
                buildJsonArray {
                    shown.forEach { tx ->
                        addJsonObject {
                            put("tgl", tx.tanggal.toString())
                            put("tipe", tx.tipe.wire)
                            put("kat", tx.kategori)
                            put("nom", tx.nominal)
                            addStringOrNull("dari", tx.akunDariId?.let { nameById[it] })
                            addStringOrNull("ke", tx.akunKeId?.let { nameById[it] })
                            put("catatan", tx.catatan)
                        }
                    }
                },
            )
        }
    }

    // --- util pembacaan argumen & tanggal ---

    private fun JsonObject.stringOrNull(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull()?.takeIf { it.isNotBlank() }

    private fun JsonObject.dateOrNull(key: String): LocalDate? =
        stringOrNull(key)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun LocalDate.inRange(dari: LocalDate?, sampai: LocalDate?): Boolean =
        (dari == null || !this.isBefore(dari)) && (sampai == null || !this.isAfter(sampai))
}

/** content JsonPrimitive yang aman untuk null literal. */
private fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? =
    if (isString || content != "null") content else null

private fun kotlinx.serialization.json.JsonObjectBuilder.addStringOrNull(key: String, value: String?) {
    if (value == null) put(key, kotlinx.serialization.json.JsonNull) else put(key, value)
}
