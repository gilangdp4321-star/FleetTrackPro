package com.industri.fleettrack.data.repository

import com.industri.fleettrack.data.local.dao.DeliveryDao
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.data.remote.api.CourierApiService
import com.industri.fleettrack.data.remote.api.DeliveryDto
import com.industri.fleettrack.data.remote.api.StatusUpdateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

class DeliveryRepository(
    private val deliveryDao: DeliveryDao,
    private val apiService: CourierApiService = CourierApiService.create()
) {

    val manifestOrdersFlow: Flow<List<DeliveryOrderEntity>> = deliveryDao.getAllDeliveryOrders()

    suspend fun refreshManifest(): Result<List<DeliveryOrderEntity>> {
        seedInitialDataIfEmpty()

        return try {
            val response = withTimeoutOrNull(3000L) {
                apiService.getTodayManifest()
            }
            if (response != null && response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val entities = dtos.map { dtoToEntity(it) }
                deliveryDao.insertAll(entities)
                Timber.d("Manifest refreshed from remote API with %d items.", entities.size)
                Result.success(entities)
            } else {
                Timber.w("Remote API failed or timed out. Falling back to local data.")
                seedInitialDataIfEmpty()
                val localOrders = deliveryDao.getAllDeliveryOrders().first()
                Result.success(localOrders)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching manifest from remote. Falling back to local seed data.")
            seedInitialDataIfEmpty()
            val localOrders = deliveryDao.getAllDeliveryOrders().first()
            Result.success(localOrders)
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentList = deliveryDao.getAllDeliveryOrders().first()
        if (currentList.isEmpty()) {
            val currentTime = System.currentTimeMillis()
            val initialSeed = listOf(
                DeliveryOrderEntity(
                    orderId = "ORD-2026-8801",
                    trackingNumber = "TRK-CRB-001",
                    recipientName = "Budi Santoso",
                    recipientPhone = "081234567890",
                    destinationAddress = "Jl. Siliwangi No. 12, Cirebon",
                    codAmount = 150000.0,
                    deliveryStatus = "PENDING",
                    failureReason = null,
                    isSynced = false,
                    updatedAt = currentTime
                ),
                DeliveryOrderEntity(
                    orderId = "ORD-2026-8802",
                    trackingNumber = "TRK-CRB-002",
                    recipientName = "Siti Aminah",
                    recipientPhone = "082198765432",
                    destinationAddress = "Jl. Kartini No. 45, Cirebon",
                    codAmount = 75000.0,
                    deliveryStatus = "PENDING",
                    failureReason = null,
                    isSynced = false,
                    updatedAt = currentTime
                ),
                DeliveryOrderEntity(
                    orderId = "ORD-2026-8803",
                    trackingNumber = "TRK-CRB-003",
                    recipientName = "Ahmad Hidayat",
                    recipientPhone = "085712344321",
                    destinationAddress = "Jl. Cipto Mangunkusumo No. 88, Cirebon",
                    codAmount = 0.0,
                    deliveryStatus = "PENDING",
                    failureReason = null,
                    isSynced = false,
                    updatedAt = currentTime
                )
            )
            deliveryDao.insertAll(initialSeed)
            Timber.d("Initial seed data inserted into local database.")
        }
    }

    suspend fun updateDeliveryStatus(
        orderId: String,
        status: String,
        failureReason: String? = null
    ) {
        deliveryDao.updateOrderStatus(
            orderId = orderId,
            status = status,
            failureReason = failureReason,
            isSynced = false
        )

        try {
            val response = withTimeoutOrNull(3000L) {
                apiService.updateDeliveryStatus(
                    orderId = orderId,
                    request = StatusUpdateRequest(status = status, failureReason = failureReason)
                )
            }
            if (response != null && response.isSuccessful && response.body()?.success == true) {
                deliveryDao.updateOrderStatus(
                    orderId = orderId,
                    status = status,
                    failureReason = failureReason,
                    isSynced = true
                )
                Timber.d("Status for order %s successfully synced to remote API.", orderId)
            } else {
                Timber.w("Failed or timed out syncing status for order %s to remote API.", orderId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while syncing status for order %s to remote API.", orderId)
        }
    }

    private fun dtoToEntity(dto: DeliveryDto): DeliveryOrderEntity {
        return DeliveryOrderEntity(
            orderId = dto.orderId,
            trackingNumber = dto.trackingNumber,
            recipientName = dto.recipientName,
            recipientPhone = dto.recipientPhone,
            destinationAddress = dto.destinationAddress,
            codAmount = dto.codAmount,
            deliveryStatus = dto.status,
            failureReason = null,
            isSynced = true,
            updatedAt = System.currentTimeMillis()
        )
    }
}
