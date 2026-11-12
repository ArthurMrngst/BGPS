package com.example.bgps.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Khớp field-by-field với ClientTransactionDto phía backend — type/source
// gửi dạng tên enum (EXPENSE/INCOME, MANUAL/NOTIFICATION/OCR/GPS).
data class ClientTransactionDto(
    val id: String,
    val amount: Long,
    val type: String,
    val category: String,
    val merchant: String?,
    val note: String?,
    val source: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val transactionRef: String?,
    val updatedAt: Long,
    val isDeleted: Boolean
)

data class SyncPushRequest(val transactions: List<ClientTransactionDto>)
data class SyncPushResponse(val accepted: Int, val skipped: Int)

interface SyncApi {
    @POST("transactions/sync")
    suspend fun push(@Body body: SyncPushRequest): SyncPushResponse

    @GET("transactions/sync")
    suspend fun pull(@Query("since") since: Long): List<ClientTransactionDto>
}
