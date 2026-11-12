package com.example.bgps.di

import com.example.bgps.auth.AuthTokenStore
import com.example.bgps.data.network.AuthApi
import com.example.bgps.data.network.SyncApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Backend chạy local trên máy tính — điện thoại gọi được qua
    // `adb reverse tcp:3000 tcp:3000` (xem README của repo backend).
    private const val BASE_URL = "http://localhost:3000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenStore: AuthTokenStore): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = authTokenStore.getTokenOrNull()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)
}
