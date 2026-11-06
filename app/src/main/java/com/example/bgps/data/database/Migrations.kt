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
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
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

// Thêm cột fuelVehicleType để người dùng chọn thủ công xe máy/ô tô lúc bắt
// đầu hành trình, phục vụ tính tiền xăng ước tính — cột nullable nên không
// cần giá trị mặc định, các hành trình cũ giữ nguyên NULL (không tính xăng).
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE trips ADD COLUMN fuelVehicleType TEXT")
    }
}

// Thêm cột transactionRef để chống lưu trùng giao dịch từ thông báo ngân
// hàng (xem TransactionEntity.transactionRef) — cột nullable nên giao dịch
// cũ giữ nguyên NULL, không ảnh hưởng dữ liệu đã có.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN transactionRef TEXT")
    }
}

// Thêm bảng budgets (D1) — ngân sách hàng tháng theo danh mục.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS budgets (
                category TEXT NOT NULL PRIMARY KEY,
                monthlyLimit INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Thêm bảng categories (B3 phần 2) — danh mục giờ là dữ liệu người dùng tự
// quản lý thay vì enum cố định. Bảng để trống sau migration; việc chèn 11
// danh mục khởi tạo nằm ở CategoryRepository.ensureSeeded() (gọi từ
// BGpsApplication.onCreate) để dùng chung một chỗ cho cả cài mới lẫn nâng
// cấp, không lặp lại danh sách này ở đây.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT NOT NULL PRIMARY KEY,
                displayName TEXT NOT NULL,
                icon TEXT,
                colorHex TEXT,
                isBuiltIn INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
