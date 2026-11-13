package com.example.bgps.di

import android.os.Build
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

    // Backend chạy local trên máy tính.
    //   - Máy thật: gọi qua `adb reverse tcp:3000 tcp:3000` tới localhost.
    //   - Emulator: 10.0.2.2 là địa chỉ đặc biệt trỏ thẳng về localhost của
    //     máy tính, KHÔNG cần adb reverse — adb reverse trên emulator hay
    //     bị lỗi "unexpected end of stream" (đã gặp thật khi test), nên emulator
    //     luôn ưu tiên 10.0.2.2.
    private val BASE_URL = if (isEmulator()) "http://10.0.2.2:3000/" else "http://localhost:3000/"

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT.contains("sdk")

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
