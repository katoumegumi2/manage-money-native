# CLAUDE.md — Aplikasi Catatan Keuangan Voice

Sumber kebenaran lengkap ada di `docs/private/` (jangan dihapus, jangan commit ke remote publik):
- `brief-claude-code.md` — PRD + arsitektur + rencana fase (acuan utama).
- `desain-data-keuangan.md` — skema DB + query saldo + aturan simpan.
- `prompt-parser-keuangan.md` — system instruction + JSON schema parser (sudah teruji).
- `mockup-keuangan.html` — acuan visual UI/UX (terjemahkan ke Compose).

File ini adalah konteks tetap (ringkasan §2–§5 + §7 brief). Kalau ada konflik, brief yang menang.

---

## Bahasa
- **Identifier kode = Inggris.** **String UI = Indonesia** (di `strings.xml`).
- Domain & komentar boleh Indonesia bila lebih jelas; nama field DTO parser mengikuti kontrak Indonesia (`tipe`, `nominal`, `akun_dari`, dst — lihat kontrak AI).

## Stack
- Kotlin (JDK 17), Jetpack Compose + Material 3, Room (SQLite).
- Coroutines + Flow. Retrofit + OkHttp + kotlinx.serialization. Hilt (DI).
- STT: Android `SpeechRecognizer`. Key storage: `EncryptedSharedPreferences`.
- minSdk 26, targetSdk 35.

## Arsitektur
Clean-ish layering, MVVM:
```
data/   db (Room) · ai (parser impls + dto + prompt + validator) · stt · repo
domain/ model (Account, Transaction, ParsedTransaction, enums) · usecase
ui/     theme · home/account/confirm/manualentry/accounts/settings · components
di/     Hilt modules
```
Prinsip:
- **Local-first:** semua read dari Room via `Flow`; UI reaktif.
- **AI swappable:** domain & ui HANYA kenal interface `TransactionParser`. Tidak ada kelas yang menyebut "Gemini"/"DeepSeek" di luar paket `data/ai`.
- **Saldo derived, bukan stored** (lihat query di bawah).

## Kontrak AI (TETAP — provider hanya adapter)
Prompt + JSON schema = kontrak; provider = adapter yang bisa ditukar. Dua adapter:
- `GeminiParser` — endpoint native, `responseSchema`, default `gemini-2.5-flash-lite`.
- `OpenAiCompatibleParser` — semua provider format OpenAI Chat Completions (DeepSeek/OpenAI/OpenRouter/Groq/Ollama); beda hanya `baseUrl` + `model` + `apiKey`.

Interface domain:
```kotlin
interface TransactionParser {
    suspend fun parse(req: ParseRequest): Result<List<ParsedTransaction>>
}
```
`ParsedTransaction`: `tipe` (pengeluaran|pemasukan|transfer), `nominal: Long`, `kategori`,
`akunDari: String?`, `akunKe: String?`, `tanggal` (YYYY-MM-DD), `catatan`, `perluDicek: Boolean`.
Prompt builder isi placeholder `{{HARI_INI}}` (WIB) & `{{DAFTAR_AKUN}}`. Validator sama untuk semua provider; gagal → `Result.failure` → fallback manual, JANGAN crash.

## Data & saldo
- `AccountEntity(id, kode UNIQUE, nama, jenis, saldoAwal, urutan, aktif)`.
- `TransactionEntity(id, tipe, nominal, kategori, akunDariId FK?, akunKeId FK?, tanggal, catatan, batchId?, sumberInput, createdAt)`. **Simpan FK id akun, bukan kode string** (resolve kode→id saat simpan).
- Saldo DIHITUNG dari ledger:
```sql
saldoAwal
  + COALESCE(SUM(nominal WHERE akunKeId   = a.id), 0)
  - COALESCE(SUM(nominal WHERE akunDariId = a.id), 0)
```
  Total = SUM(saldo). Ekspos sebagai `Flow<List<AccountBalance>>` & `Flow<Long>`.
- Aturan simpan: pengeluaran→`akunDari` wajib; pemasukan→`akunKe` wajib; transfer→keduanya wajib & berbeda. Akun null tak boleh tersimpan. Satu batch konfirmasi = `batchId` sama.
- Seed akun: `bni, mandiri, seabank` (bank), `gopay, shopeepay` (ewallet), `tunai` (tunai).
- Kategori — pengeluaran: `makanan, belanja, transport, tagihan, hiburan, kesehatan, admin, lainnya`; pemasukan: `gaji, usaha, bonus, sampingan, transfer, lainnya`; transfer: `lainnya`.

## Design tokens (Material 3, light + dark)
- Warna: ink `#1E2A3A`, paper `#EDF0F2`, card `#FFFFFF`, gold (aksi/voice) `#D99A2B`,
  income `#2F7D5B`, expense `#C2513B`, transfer `#5C7390`, muted `#6B7785`, line `#E2E6EA`.
- Font: display **Space Grotesk**, body **Inter**, angka **JetBrains Mono** (tabular).
- Uang: `Rp 1.234.567` (pemisah titik, locale id-ID). Semua nominal pakai mono tabular.
- Tulisan UI: sentence case, kata kerja aktif, nama tombol konsisten ("Simpan semua" → toast "Tersimpan"). Empty/error state mengarahkan tindakan.

## Konvensi koding
- MVVM: ViewModel ekspos `StateFlow<UiState>`; UI murni dari state; side-effect via event.
- Semua I/O `suspend` + `Dispatchers.IO`; tidak ada blocking di main.
- Uang = `Long` rupiah (integer), tanpa floating point.
- Tanggal = `LocalDate`/ISO string; zona `Asia/Jakarta`.
- Unit test wajib untuk: hitung saldo, validator JSON parser, aturan simpan.
- **Jangan pernah log API key. Jangan commit key/secret** (`local.properties`, key user di EncryptedSharedPreferences).
- Offline: semua tampilan + input manual + simpan jalan tanpa internet. Hanya voice→parse butuh internet.

## Rencana fase (satu fase = satu PR; selesaikan acceptance sebelum lanjut)
0. Scaffold & design system. 1. Data layer. 2. Beranda read-only. 3. Manual + Konfirmasi + Simpan.
4. AI + Gemini adapter (input via kotak teks). 5. Voice. 6. Provider swappable + Pengaturan AI. 7. Polish & laporan.

## Build (lihat docs/private/setup-lxc.md)
- Gunakan `./gradlew` (wrapper). JDK 17. `ANDROID_HOME` ke Android SDK (cmdline-tools `latest`, platform android-35, build-tools 35.0.0).
- `local.properties` (gitignored) berisi `sdk.dir`. Deploy ke HP fisik via `adb` (tanpa emulator).
