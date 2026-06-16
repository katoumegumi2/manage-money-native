package com.taufik.managemoney.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Enkripsi backup: round-trip benar; passphrase salah → gagal anggun. */
class BackupCipherTest {

    private val plain = "money.db payload — saldo BNI 88000, Mandiri 1035000".toByteArray()

    @Test
    fun `enkripsi lalu dekripsi mengembalikan data asli`() {
        val blob = BackupCipher.encrypt(plain, "rahasia123".toCharArray())
        val out = BackupCipher.decrypt(blob, "rahasia123".toCharArray())
        assertArrayEquals(plain, out)
    }

    @Test
    fun `ciphertext berbeda dari plaintext`() {
        val blob = BackupCipher.encrypt(plain, "rahasia123".toCharArray())
        assertFalse(blob.copyOfRange(blob.size - plain.size, blob.size).contentEquals(plain))
    }

    @Test
    fun `salt-iv acak - dua enkripsi menghasilkan blob berbeda`() {
        val a = BackupCipher.encrypt(plain, "rahasia123".toCharArray())
        val b = BackupCipher.encrypt(plain, "rahasia123".toCharArray())
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun `passphrase salah gagal dengan WrongPassphraseException`() {
        val blob = BackupCipher.encrypt(plain, "benar".toCharArray())
        assertThrows(BackupCipher.WrongPassphraseException::class.java) {
            BackupCipher.decrypt(blob, "salah".toCharArray())
        }
    }

    @Test
    fun `blob bukan format kami ditolak`() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCipher.decrypt("bukan blob".toByteArray(), "apa saja".toCharArray())
        }
    }
}
