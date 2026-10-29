package com.example.bgps.di

import android.content.Context
import androidx.room.Room
import com.example.bgps.data.database.AppDatabase
import com.example.bgps.data.database.MIGRATION_1_2
import com.example.bgps.data.database.TransactionDao
import com.example.bgps.data.database.TripDao
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
        Room.databaseBuilder(context, AppDatabase::class.java, "bgps.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()
}
