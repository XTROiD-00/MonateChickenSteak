package com.example.monatechickensteak.data

import com.example.monatechickensteak.BuildConfig
import com.example.monatechickensteak.model.FoodItem
import com.example.monatechickensteak.model.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

data class AuthRequest(val name: String? = null, val email: String, val password: String? = null)
data class GoogleAuthRequest(@SerializedName("id_token") val idToken: String)
data class AuthResponse(val success: Boolean, val message: String? = null, val token: String? = null, val user: ApiUser? = null)
data class ApiUser(val id: Int, val name: String, val email: String, @SerializedName("loyalty_points") val loyaltyPoints: Int = 0) {
    fun toUser() = User(id, name, email, loyaltyPoints)
}
data class ApiMenuItem(val id: Int, val name: String, val category: String, val description: String, val price: Double, val emoji: String = "🍽️", @SerializedName("image_url") val imageUrl: String? = null) {
    fun toFoodItem() = FoodItem(id, name, category, description, price, emoji, imageUrl)
}
data class MenuResponse(val success: Boolean, val items: List<ApiMenuItem> = emptyList(), val message: String? = null)
data class ProfileUpdateRequest(val name: String)
data class ChangePasswordRequest(@SerializedName("current_password") val currentPassword: String, @SerializedName("new_password") val newPassword: String)
data class ApiMessage(val success: Boolean, val message: String? = null, val user: ApiUser? = null)
data class PlaceOrderItem(@SerializedName("menu_item_id") val menuItemId: Int, val quantity: Int)
data class PlaceOrderRequest(val items: List<PlaceOrderItem>, @SerializedName("collection_time") val collectionTime: String, @SerializedName("payment_method") val paymentMethod: String)
data class PlacedOrder(val id: Int, val status: String, val total: Double, @SerializedName("loyalty_points_earned") val loyaltyPointsEarned: Int = 0)
data class PlaceOrderResponse(val success: Boolean, val message: String? = null, val order: PlacedOrder? = null)

interface MonateApi {
    @POST("auth/register") suspend fun register(@Body body: AuthRequest): AuthResponse
    @POST("auth/login") suspend fun login(@Body body: AuthRequest): AuthResponse
    @POST("auth/google") suspend fun googleLogin(@Body body: GoogleAuthRequest): AuthResponse
    @GET("menu") suspend fun menu(): MenuResponse
    @GET("profile") suspend fun profile(@Header("Authorization") bearer: String): ApiMessage
    @PATCH("profile") suspend fun updateProfile(@Header("Authorization") bearer: String, @Body body: ProfileUpdateRequest): ApiMessage
    @POST("auth/change-password") suspend fun changePassword(@Header("Authorization") bearer: String, @Body body: ChangePasswordRequest): ApiMessage
    @POST("orders") suspend fun placeOrder(@Header("Authorization") bearer: String, @Body body: PlaceOrderRequest): PlaceOrderResponse
}

object ApiClient {
    private val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(logger)
        .build()

    val service: MonateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MonateApi::class.java)
    }
}
