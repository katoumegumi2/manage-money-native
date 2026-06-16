package com.taufik.managemoney.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SeedTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        // pakai file DB (bukan in-memory murni) agar callback onCreate jalan saat dibuat
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).addCallback(AppDatabase.seedCallback).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `seed enam akun saat pertama dibuat`() = runTest {
        val accounts = db.accountDao().observeActive().first()
        assertEquals(6, accounts.size)
        assertEquals(
            listOf("bni", "mandiri", "seabank", "gopay", "shopeepay", "tunai"),
            accounts.map { it.kode },
        )
    }

    @Test
    fun `saldo awal seed nol`() = runTest {
        assertEquals(0L, db.accountDao().observeTotal().first())
    }
}
