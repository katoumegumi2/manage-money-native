package com.taufik.managemoney.data.backup

import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Enkripsi blob backup gaya E2E: AES-256-GCM dengan kunci diturunkan dari passphrase (PBKDF2).
 * Google hanya memegang ciphertext; tanpa passphrase backup tak bisa dibuka.
 *
 * Format blob: MAGIC(4) + VERSION(1) + salt(16) + iv(12) + ciphertext(+tag 16).
 */
object BackupCipher {
    private val MAGIC = byteArrayOf('M'.code.toByte(), 'M'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte())
    private const val VERSION = 1.toByte()
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val TAG_BITS = 128
    private const val PBKDF2_ITERATIONS = 210_000
    private const val KEY_BITS = 256

    /** Passphrase salah / blob rusak saat dekripsi. */
    class WrongPassphraseException : Exception("Passphrase salah atau cadangan rusak")

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plain: ByteArray, passphrase: CharArray): ByteArray {
        val rng = SecureRandom()
        val salt = ByteArray(SALT_LEN).also { rng.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(TAG_BITS, iv))
        val ct = cipher.doFinal(plain)
        return MAGIC + byteArrayOf(VERSION) + salt + iv + ct
    }

    fun decrypt(blob: ByteArray, passphrase: CharArray): ByteArray {
        val headerLen = MAGIC.size + 1 + SALT_LEN + IV_LEN
        require(blob.size > headerLen) { "Blob backup tidak valid" }
        require(blob.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) { "Format backup tidak dikenal" }
        val salt = blob.copyOfRange(MAGIC.size + 1, MAGIC.size + 1 + SALT_LEN)
        val iv = blob.copyOfRange(MAGIC.size + 1 + SALT_LEN, headerLen)
        val ct = blob.copyOfRange(headerLen, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt), GCMParameterSpec(TAG_BITS, iv))
        return try {
            cipher.doFinal(ct)
        } catch (e: AEADBadTagException) {
            throw WrongPassphraseException()
        }
    }
}
