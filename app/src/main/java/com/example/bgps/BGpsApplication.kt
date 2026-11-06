package com.example.bgps

import android.app.Application
import com.example.bgps.data.repository.CategoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import javax.inject.Inject

@HiltAndroidApp
class BGpsApplication : Application() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

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

        // Chèn 11 danh mục mặc định nếu bảng categories đang trống (cài mới
        // hoặc vừa nâng cấp qua MIGRATION_6_7) — xem B3 phần 2.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            categoryRepository.ensureSeeded()
        }
    }
}
