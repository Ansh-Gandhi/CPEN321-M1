package com.example.cpen321application.network

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Retrofit API service for backend endpoints
 */
interface ApiService {
    @GET("/api/server-ip")
    suspend fun getServerIp(): ServerIpResponse

    @GET("/api/server-time")
    suspend fun getServerTime(): ServerTimeResponse

    @GET("/api/my-name")
    suspend fun getMyName(): MyNameResponse

    @GET("/api/trivia")
    suspend fun getTriviaQuestion(): TriviaResponse
}

/**
 * Singleton Retrofit client
 */
object NetworkClient {
    fun create(baseUrl: String): ApiService {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
