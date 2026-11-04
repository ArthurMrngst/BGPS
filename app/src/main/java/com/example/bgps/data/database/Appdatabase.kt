package com.example.bgps.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, TripEntity::class, TripPointEntity::class, BudgetEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun tripDao(): TripDao
    abstract fun budgetDao(): BudgetDao
}
