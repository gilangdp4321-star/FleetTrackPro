package com.industri.fleettrack.data.local.dao

import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import kotlinx.coroutines.flow.Flow

interface DeliveryDao {
    fun getAllDeliveryOrders(): Flow<List<DeliveryOrderEntity>>
    fun getOrdersByStatus(status: String): Flow<List<DeliveryOrderEntity>>
    suspend fun insertAll(orders: List<DeliveryOrderEntity>)
    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        failureReason: String? = null,
        isSynced: Boolean = false
    )
    suspend fun clearAll()
}
