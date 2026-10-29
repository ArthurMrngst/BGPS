package com.example.bgps

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class BGpsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // osmdroid yêu cầu đặt User-Agent (quy định dùng chung của OSM, không
        // có thì tile có thể bị chặn) và dùng cache dir riêng của app để
        // không cần xin quyền lưu trữ ngoài.
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir.resolve("osmdroid")
            osmdroidTileCache = osmdroidBasePath.resolve("tiles")
        }
    }
}
