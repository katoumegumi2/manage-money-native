package com.taufik.managemoney.di

import android.content.Context
import androidx.room.Room
import com.taufik.managemoney.data.db.AppDatabase
import com.taufik.managemoney.data.db.Migrations
import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addCallback(AppDatabase.seedCallback)
            .addMigrations(*Migrations.ALL)
            .build()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
}
