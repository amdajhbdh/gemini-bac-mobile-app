package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// ============================================
// COBALT WHATSAPP BACKEND API SCHEMA
// ============================================

@JsonClass(generateAdapter = true)
data class CobaltSessionRequest(
    val sessionId: String,
    val useMobileProtocol: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CobaltSessionResponse(
    val sessionId: String,
    val status: String, // "PAIRING_QR", "CONNECTED", "DISCONNECTED"
    val qrCodeBase64: String? = null,
    val pairingCode: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class CobaltSendMessageRequest(
    val sessionId: String,
    val recipient: String,
    val messageText: String
)

@JsonClass(generateAdapter = true)
data class CobaltSendMessageResponse(
    val success: Boolean,
    val messageId: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class CobaltMessageSyncResponse(
    val success: Boolean,
    val messages: List<CobaltIncomingMessage>? = null
)

@JsonClass(generateAdapter = true)
data class CobaltIncomingMessage(
    val senderName: String,
    val senderPhone: String,
    val messageText: String,
    val timestampMs: Long,
    val isFromMe: Boolean,
    val isMedia: Boolean = false,
    val mediaUrl: String? = null
)

// ============================================
// COBALT RETROFIT API SERVICE
// ============================================

interface CobaltApiService {

    @POST
    suspend fun createCobaltSession(
        @Url url: String,
        @Body body: CobaltSessionRequest
    ): CobaltSessionResponse

    @GET
    suspend fun getCobaltSessionStatus(
        @Url url: String,
        @Query("sessionId") sessionId: String
    ): CobaltSessionResponse

    @POST
    suspend fun sendCobaltMessage(
        @Url url: String,
        @Body body: CobaltSendMessageRequest
    ): CobaltSendMessageResponse

    @GET
    suspend fun syncCobaltMessages(
        @Url url: String,
        @Query("sessionId") sessionId: String,
        @Query("sinceTimestamp") sinceTimestamp: Long
    ): CobaltMessageSyncResponse
}

// ============================================
// COBALT CLIENT COMPANION
// ============================================

object CobaltClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost/") // Dynamic URLs passed via @Url parameters
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: CobaltApiService = retrofit.create(CobaltApiService::class.java)

    /**
     * Start a Cobalt WhatsApp session connection
     */
    suspend fun startSession(serverUrl: String, phone: String, useMobile: Boolean): Result<CobaltSessionResponse> {
        return try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/cobalt/session/start" else "$serverUrl/api/cobalt/session/start"
            val response = apiService.createCobaltSession(endpoint, CobaltSessionRequest(phone, useMobile))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Poll / Get status of Cobalt session pairing
     */
    suspend fun getStatus(serverUrl: String, phone: String): Result<CobaltSessionResponse> {
        return try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/cobalt/session/status" else "$serverUrl/api/cobalt/session/status"
            val response = apiService.getCobaltSessionStatus(endpoint, phone)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send structured WhatsApp message via the Cobalt instance
     */
    suspend fun sendMessage(serverUrl: String, phone: String, to: String, text: String): Result<CobaltSendMessageResponse> {
        return try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/cobalt/session/send" else "$serverUrl/api/cobalt/session/send"
            val response = apiService.sendCobaltMessage(endpoint, CobaltSendMessageRequest(phone, to, text))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync incoming messages from Cobalt server memory into local Room cache
     */
    suspend fun syncMessages(serverUrl: String, phone: String, since: Long): Result<CobaltMessageSyncResponse> {
        return try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/cobalt/session/sync" else "$serverUrl/api/cobalt/session/sync"
            val response = apiService.syncCobaltMessages(endpoint, phone, since)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
