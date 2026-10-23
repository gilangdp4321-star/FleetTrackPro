package com.industri.fleettrack.ui.manifest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class ManifestUiState {
    object Loading : ManifestUiState()
    data class Success(val orders: List<DeliveryOrderEntity>) : ManifestUiState()
    data class Error(val message: String) : ManifestUiState()
}

class ManifestViewModel(
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManifestUiState>(ManifestUiState.Loading)
    val uiState: StateFlow<ManifestUiState> = _uiState.asStateFlow()

    init {
        observeManifestOrders()
        refreshData()
    }

    private fun observeManifestOrders() {
        viewModelScope.launch {
            repository.manifestOrdersFlow
                .catch {
                    repository.seedInitialDataIfEmpty()
                    val localOrders = repository.manifestOrdersFlow.first()
                    _uiState.value = ManifestUiState.Success(localOrders)
                }
                .collect { orders ->
                    _uiState.value = ManifestUiState.Success(orders)
                }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()

            val localOrders = repository.manifestOrdersFlow.first()
            if (localOrders.isNotEmpty()) {
                _uiState.value = ManifestUiState.Success(localOrders)
            } else {
                _uiState.value = ManifestUiState.Loading
            }

            try {
                repository.refreshManifest()
            } catch (_: Exception) {
                val currentLocalOrders = repository.manifestOrdersFlow.first()
                _uiState.value = ManifestUiState.Success(currentLocalOrders)
            }
        }
    }

    fun refreshManifest() {
        refreshData()
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateDeliveryStatus(orderId, status)
        }
    }
}

class ManifestViewModelFactory(
    private val repository: DeliveryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManifestViewModel::class.java)) {
            return ManifestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
