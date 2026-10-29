package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Từng điểm GPS ghi được trong một hành trình — dùng để vẽ lại đường đi trên
// bản đồ ở màn chi tiết hành trình. Xoá trip thì các điểm của nó cũng tự xoá
// theo (CASCADE) để không rác dữ liệu mồ côi.
@Entity(
    tableName = "trip_points",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class TripPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long,
    val sequence: Int
)
