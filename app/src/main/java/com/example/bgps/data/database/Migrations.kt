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

// Thêm bảng trip_points để lưu lại toàn bộ đường đi (trước đó chỉ lưu điểm
// đầu/cuối) — cần để vẽ Polyline ở màn chi tiết hành trình.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trip_points (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tripId TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                timestampMillis INTEGER NOT NULL,
                sequence INTEGER NOT NULL,
                FOREIGN KEY(tripId) REFERENCES trips(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_trip_points_tripId ON trip_points(tripId)")
    }
}
