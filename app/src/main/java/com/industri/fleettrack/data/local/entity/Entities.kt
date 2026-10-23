package com.industri.fleettrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_orders")
data class DeliveryOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val orderId: String,

    @ColumnInfo(name = "tracking_number")
    val trackingNumber: String,

    @ColumnInfo(name = "recipient_name")
    val recipientName: String,

    @ColumnInfo(name = "recipient_phone")
    val recipientPhone: String,

    @ColumnInfo(name = "destination_address")
    val destinationAddress: String,

    @ColumnInfo(name = "cod_amount")
    val codAmount: Double,

    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String,

    @ColumnInfo(name = "failure_reason")
    val failureReason: String? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "queue_id")
    val queueId: Long = 0,

    @ColumnInfo(name = "order_id")
    val orderId: String,

    @ColumnInfo(name = "target_status")
    val targetStatus: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
