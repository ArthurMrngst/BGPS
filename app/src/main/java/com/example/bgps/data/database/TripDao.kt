package com.example.bgps.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert
    suspend fun insert(trip: TripEntity)

    @Insert
    suspend fun insertPoints(points: List<TripPointEntity>)

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun observeAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getById(tripId: String): TripEntity?

    @Query("SELECT * FROM trip_points WHERE tripId = :tripId ORDER BY sequence ASC")
    suspend fun getPoints(tripId: String): List<TripPointEntity>
}
