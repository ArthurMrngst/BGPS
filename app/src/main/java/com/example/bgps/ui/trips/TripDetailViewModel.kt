package com.example.bgps.ui.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.TripPointEntity
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.data.repository.TripRepository
import com.example.bgps.domain.DefaultCategories
import com.example.bgps.domain.trip.FuelCostCalculator
import com.example.bgps.location.FuelSettings
import com.example.bgps.location.FuelSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecordFuelExpenseState {
    data object Idle : RecordFuelExpenseState
    data object Recorded : RecordFuelExpenseState
    // Đã có một chi tiêu thật (bất kỳ danh mục nào — có thể là khoản chuyển
    // khoản đổ xăng bị xếp vào "Khác" do chưa nhận diện được tên cây xăng)
    // gần thời điểm này — hỏi người dùng có chắc muốn ghi thêm không, kèm
    // thông tin khoản đã có để họ tự nhận ra có phải cùng một lần đổ xăng.
    data class DuplicateWarning(val existing: TransactionEntity) : RecordFuelExpenseState
}

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repository: TripRepository,
    private val transactionRepository: TransactionRepository,
    fuelSettingsStore: FuelSettingsStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    val fuelSettings: StateFlow<FuelSettings> = fuelSettingsStore.settings

    private val _trip = MutableStateFlow<TripEntity?>(null)
    val trip: StateFlow<TripEntity?> = _trip.asStateFlow()

    private val _points = MutableStateFlow<List<TripPointEntity>>(emptyList())
    val points: StateFlow<List<TripPointEntity>> = _points.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _recordFuelExpenseState = MutableStateFlow<RecordFuelExpenseState>(RecordFuelExpenseState.Idle)
    val recordFuelExpenseState: StateFlow<RecordFuelExpenseState> = _recordFuelExpenseState.asStateFlow()

    init {
        viewModelScope.launch {
            _trip.value = repository.getTrip(tripId)
            _points.value = repository.getPoints(tripId)
            _loaded.value = true
        }
    }

    // Chỉ ghi khi người dùng chủ động bấm — KHÔNG tự động, để không trộn số
    // ước tính với số thực chi (xem ghi chú ở SummaryScreen). force=true khi
    // người dùng đã xác nhận bỏ qua cảnh báo trùng.
    fun recordFuelExpense(force: Boolean = false) {
        val currentTrip = _trip.value ?: return
        val vehicle = currentTrip.fuelVehicleType ?: return
        val settings = fuelSettings.value

        viewModelScope.launch {
            if (!force) {
                val existing = transactionRepository.findRecentFuelExpense(timestamp = currentTrip.endTime)
                if (existing != null) {
                    _recordFuelExpenseState.value = RecordFuelExpenseState.DuplicateWarning(existing)
                    return@launch
                }
            }

            val cost = FuelCostCalculator.estimateCostVnd(
                distanceMeters = currentTrip.distanceMeters,
                vehicleType = vehicle,
                motorbikeLitersPer100Km = settings.motorbikeLitersPer100Km,
                carLitersPer100Km = settings.carLitersPer100Km,
                pricePerLiterVnd = settings.pricePerLiterVnd
            )

            transactionRepository.save(
                TransactionEntity(
                    amount = cost,
                    type = TransactionType.EXPENSE,
                    category = DefaultCategories.FUEL_CATEGORY_NAME,
                    note = "Tiền xăng ước tính (${"%.1f".format(currentTrip.distanceMeters / 1000.0)} km)",
                    source = TransactionSource.GPS,
                    timestamp = currentTrip.endTime
                )
            )
            _recordFuelExpenseState.value = RecordFuelExpenseState.Recorded
        }
    }

    fun dismissRecordFuelExpenseState() {
        _recordFuelExpenseState.value = RecordFuelExpenseState.Idle
    }
}
