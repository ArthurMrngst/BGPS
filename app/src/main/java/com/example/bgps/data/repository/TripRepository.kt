package com.example.bgps.data.repository

import com.example.bgps.data.database.TripDao
import com.example.bgps.data.database.TripEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val dao: TripDao
) {
    fun observeTrips(): Flow<List<TripEntity>> = dao.observeAll()

    suspend fun save(trip: TripEntity) = dao.insert(trip)
}
