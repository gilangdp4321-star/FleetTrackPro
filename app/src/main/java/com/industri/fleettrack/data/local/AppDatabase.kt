package com.industri.fleettrack.data.local

import android.content.Context
import com.industri.fleettrack.data.local.dao.DeliveryDao
import com.industri.fleettrack.data.local.dao.DeliveryDaoImpl

object AppDatabase {
    @Volatile
    private var instance: DeliveryDao? = null

    fun getInstance(context: Context): DeliveryDao {
        return instance ?: synchronized(this) {
            instance ?: DeliveryDaoImpl(context.applicationContext).also { instance = it }
        }
    }
}
