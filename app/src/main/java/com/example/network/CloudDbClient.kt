package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// ============================================
// TURSO HTTP HANDLERS & SCHEMA TYPES
// ============================================

@JsonClass(generateAdapter = true)
data class TursoStatement(
    val sql: String,
    val args: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TursoQueryRequest(
    val statements: List<String>
)

@JsonClass(generateAdapter = true)
data class TursoExecuteResponse(
    val results: List<TursoResult>? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class TursoResult(
    val columns: List<String>? = null,
    val rows: List<List<Any>>? = null
)

// ============================================
// NEON DB HTTP HANDLERS & SCHEMA TYPES
// ============================================

@JsonClass(generateAdapter = true)
data class NeonSqlRequest(
    val query: String
)

@JsonClass(generateAdapter = true)
data class NeonSqlResponse(
    val result: List<Map<String, Any>>? = null,
    val error: String? = null,
    val rowCount: Int? = null
)

// ============================================
// RETROFIT INTERFACE DEFINITION
// ============================================

interface CloudDbApiService {
    
    // Execute multiple SQLite/libSQL requests in Turso
    @POST
    suspend fun executeTurso(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: TursoQueryRequest
    ): TursoExecuteResponse

    // Execute PostgreSQL queries on Neon
    @POST
    suspend fun executeNeon(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: NeonSqlRequest
    ): NeonSqlResponse
}

object CloudDbClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost/") // Dynamic URLs are passed in @Url parameters
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: CloudDbApiService = retrofit.create(CloudDbApiService::class.java)

    /**
     * Executes queries on Turso in a safe, fail-graceful manner
     */
    suspend fun executeTursoSql(
        baseUrl: String,
        token: String,
        statements: List<String>
    ): Result<TursoExecuteResponse> {
        return try {
            val endpoint = if (baseUrl.endsWith("/")) "${baseUrl}v1/execute" else "$baseUrl/v1/execute"
            val auth = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = apiService.executeTurso(
                url = endpoint,
                authHeader = auth,
                body = TursoQueryRequest(statements)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes queries on Neon in a safe, fail-graceful manner
     */
    suspend fun executeNeonSql(
        baseUrl: String,
        token: String,
        query: String
    ): Result<NeonSqlResponse> {
        return try {
            val endpoint = if (baseUrl.contains("/sql")) baseUrl else {
                if (baseUrl.endsWith("/")) "${baseUrl}sql" else "$baseUrl/sql"
            }
            val auth = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = apiService.executeNeon(
                url = endpoint,
                authHeader = auth,
                body = NeonSqlRequest(query)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
