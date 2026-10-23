package com.industri.fleettrack.data.local.dao

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class DeliveryDaoImpl(context: Context) : DeliveryDao {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Any()

    private val _ordersStateFlow = MutableStateFlow<List<DeliveryOrderEntity>>(emptyList())

    init {
        loadDataOrSeed()
    }

    private fun loadDataOrSeed() {
        synchronized(lock) {
            val json = prefs.getString(KEY_ORDERS, null)
            if (json.isNullOrEmpty()) {
                val seedData = getInitialSeedData()
                saveToPrefsLocked(seedData)
                _ordersStateFlow.value = seedData
            } else {
                val type = object : TypeToken<List<DeliveryOrderEntity>>() {}.type
                val loadedList: List<DeliveryOrderEntity> = try {
                    gson.fromJson(json, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                if (loadedList.isEmpty()) {
                    val seedData = getInitialSeedData()
                    saveToPrefsLocked(seedData)
                    _ordersStateFlow.value = seedData
                } else {
                    _ordersStateFlow.value = loadedList
                }
            }
        }
    }

    private fun saveToPrefsLocked(orders: List<DeliveryOrderEntity>) {
        val json = gson.toJson(orders)
        prefs.edit().putString(KEY_ORDERS, json).apply()
    }

    private fun getInitialSeedData(): List<DeliveryOrderEntity> {
        val currentTime = System.currentTimeMillis()
        return listOf(
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
    }

    override fun getAllDeliveryOrders(): Flow<List<DeliveryOrderEntity>> {
        return _ordersStateFlow.asStateFlow()
    }

    override fun getOrdersByStatus(status: String): Flow<List<DeliveryOrderEntity>> {
        return _ordersStateFlow.map { list ->
            list.filter { it.deliveryStatus.equals(status, ignoreCase = true) }
        }
    }

    override suspend fun insertAll(orders: List<DeliveryOrderEntity>) {
        synchronized(lock) {
            val currentList = _ordersStateFlow.value.toMutableList()
            orders.forEach { newOrder ->
                val index = currentList.indexOfFirst { it.orderId == newOrder.orderId }
                if (index != -1) {
                    currentList[index] = newOrder
                } else {
                    currentList.add(newOrder)
                }
            }
            saveToPrefsLocked(currentList)
            _ordersStateFlow.value = currentList
        }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        failureReason: String?,
        isSynced: Boolean
    ) {
        synchronized(lock) {
            val currentList = _ordersStateFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.orderId == orderId }
            if (index != -1) {
                val old = currentList[index]
                val updated = old.copy(
                    deliveryStatus = status,
                    failureReason = failureReason,
                    isSynced = isSynced,
                    updatedAt = System.currentTimeMillis()
                )
                currentList[index] = updated
                saveToPrefsLocked(currentList)
                _ordersStateFlow.value = currentList
            }
        }
    }

    override suspend fun clearAll() {
        synchronized(lock) {
            prefs.edit().remove(KEY_ORDERS).apply()
            _ordersStateFlow.value = emptyList()
        }
    }

    companion object {
        private const val PREF_NAME = "fleettrack_orders_pref"
        private const val KEY_ORDERS = "delivery_orders_json"
    }
}
