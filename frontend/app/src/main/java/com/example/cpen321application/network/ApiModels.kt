package com.example.cpen321application.network

import com.squareup.moshi.JsonClass

/**
 * Response models from backend APIs
 */

@JsonClass(generateAdapter = true)
data class ServerIpResponse(
    val ip: String
)

@JsonClass(generateAdapter = true)
data class ServerTimeResponse(
    val time: String
)

@JsonClass(generateAdapter = true)
data class MyNameResponse(
    val firstName: String,
    val lastName: String
)

@JsonClass(generateAdapter = true)
data class TriviaResponse(
    val category: String,
    val difficulty: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>
)
