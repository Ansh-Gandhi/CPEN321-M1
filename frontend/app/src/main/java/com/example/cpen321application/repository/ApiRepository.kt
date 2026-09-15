package com.example.cpen321application.repository

import com.example.cpen321application.network.ApiService
import com.example.cpen321application.network.ServerIpResponse
import com.example.cpen321application.network.ServerTimeResponse
import com.example.cpen321application.network.MyNameResponse
import com.example.cpen321application.network.TriviaResponse

/**
 * Repository for handling API calls and error handling
 */
class ApiRepository(private val apiService: ApiService) {

    suspend fun getServerIp(): Result<ServerIpResponse> = runCatching {
        apiService.getServerIp()
    }

    suspend fun getServerTime(): Result<ServerTimeResponse> = runCatching {
        apiService.getServerTime()
    }

    suspend fun getMyName(): Result<MyNameResponse> = runCatching {
        apiService.getMyName()
    }

    suspend fun getTriviaQuestion(): Result<TriviaResponse> = runCatching {
        apiService.getTriviaQuestion()
    }
}
