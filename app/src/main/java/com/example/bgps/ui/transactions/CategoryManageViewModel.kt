package com.example.bgps.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.CategoryEntity
import com.example.bgps.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        repository.observeCategories()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun add(displayName: String, icon: String?, colorHex: String?) {
        viewModelScope.launch { repository.add(displayName, icon, colorHex) }
    }

    fun rename(category: CategoryEntity, newDisplayName: String) {
        viewModelScope.launch { repository.rename(category, newDisplayName) }
    }

    fun updateIconColor(category: CategoryEntity, icon: String?, colorHex: String?) {
        viewModelScope.launch { repository.updateIconColor(category, icon, colorHex) }
    }

    fun delete(category: CategoryEntity, reassignTo: String) {
        viewModelScope.launch { repository.delete(category, reassignTo) }
    }
}
