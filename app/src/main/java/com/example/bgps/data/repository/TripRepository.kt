package com.example.bgps.data.repository

import com.example.bgps.data.database.TripDao
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.TripPointEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val dao: TripDao
) {
    fun observeTrips(): Flow<List<TripEntity>> = dao.observeAll()

    suspend fun saveWithPoints(trip: TripEntity, points: List<TripPointEntity>) {
        dao.insert(trip)
        if (points.isNotEmpty()) {
            dao.insertPoints(points)
        }
    }

    suspend fun getTrip(tripId: String): TripEntity? = dao.getById(tripId)

    suspend fun getPoints(tripId: String): List<TripPointEntity> = dao.getPoints(tripId)
}
