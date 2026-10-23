package com.industri.fleettrack.data.remote.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class DeliveryDto(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("tracking_number")
    val trackingNumber: String,

    @SerializedName("recipient_name")
    val recipientName: String,

    @SerializedName("recipient_phone")
    val recipientPhone: String,

    @SerializedName("destination_address")
    val destinationAddress: String,

    @SerializedName("cod_amount")
    val codAmount: Double,

    @SerializedName("status")
    val status: String
)

data class StatusUpdateRequest(
    @SerializedName("status")
    val status: String,

    @SerializedName("failure_reason")
    val failureReason: String? = null
)

data class StatusUpdateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null
)

interface CourierApiService {

    @GET("api/v1/courier/manifest/today")
    suspend fun getTodayManifest(): Response<List<DeliveryDto>>

    @POST("api/v1/courier/deliveries/{order_id}/status")
    suspend fun updateDeliveryStatus(
        @Path("order_id") orderId: String,
        @Body request: StatusUpdateRequest
    ): Response<StatusUpdateResponse>

    companion object {
        private const val BASE_URL = "https://api.fleettrack.id/"

        fun create(baseUrl: String = BASE_URL): CourierApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CourierApiService::class.java)
        }
    }
}
