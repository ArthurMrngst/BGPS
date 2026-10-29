package com.example.bgps.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Thêm bảng trips (Sprint 5, A6). Viết migration thật thay vì
// fallbackToDestructiveMigration để không xoá dữ liệu giao dịch đã có khi
// người dùng cập nhật app — bài học từ lần quên tăng version ở Sprint 1.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trips (
                id TEXT NOT NULL PRIMARY KEY,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                distanceMeters REAL NOT NULL,
                vehicleType TEXT NOT NULL,
                startLatitude REAL,
                startLongitude REAL,
                endLatitude REAL,
                endLongitude REAL
            )
            """.trimIndent()
        )
    }
}
