package com.example.bgps.location

import android.content.Context
import com.example.bgps.domain.trip.FuelCostCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class FuelSettings(
    val motorbikeLitersPer100Km: Double,
    val carLitersPer100Km: Double,
    val pricePerLiterVnd: Double
)

// Lưu mức tiêu hao xăng (L/100km) từng loại xe + giá xăng người dùng tự
// chỉnh ở màn Cài đặt xăng — mặc định lấy từ FuelCostCalculator.DEFAULT_*
// cho tới khi người dùng lưu giá trị riêng. StateFlow để các màn đang hiện
// tiền xăng (danh sách/chi tiết hành trình, bản đồ trực tiếp) tự cập nhật
// ngay khi người dùng đổi cài đặt mà không cần khởi động lại app.
@Singleton
class FuelSettingsStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("fuel_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<FuelSettings> = _settings.asStateFlow()

    private fun load(): FuelSettings = FuelSettings(
        motorbikeLitersPer100Km = prefs.getFloat(
            KEY_MOTORBIKE,
            FuelCostCalculator.DEFAULT_MOTORBIKE_LITERS_PER_100KM.toFloat()
        ).toDouble(),
        carLitersPer100Km = prefs.getFloat(
            KEY_CAR,
            FuelCostCalculator.DEFAULT_CAR_LITERS_PER_100KM.toFloat()
        ).toDouble(),
        pricePerLiterVnd = prefs.getFloat(
            KEY_PRICE,
            FuelCostCalculator.DEFAULT_PRICE_PER_LITER_VND.toFloat()
        ).toDouble()
    )

    fun save(settings: FuelSettings) {
        prefs.edit()
            .putFloat(KEY_MOTORBIKE, settings.motorbikeLitersPer100Km.toFloat())
            .putFloat(KEY_CAR, settings.carLitersPer100Km.toFloat())
            .putFloat(KEY_PRICE, settings.pricePerLiterVnd.toFloat())
            .apply()
        _settings.value = settings
    }

    companion object {
        private const val KEY_MOTORBIKE = "motorbike_l_per_100km"
        private const val KEY_CAR = "car_l_per_100km"
        private const val KEY_PRICE = "price_per_liter_vnd"
    }
}
