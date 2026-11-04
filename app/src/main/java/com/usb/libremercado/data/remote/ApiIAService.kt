package com.usb.libremercado.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiIAService {
    @POST("generate")
    suspend fun getAIReponse(@Body request: AIRequest): AIResponse
}

data class AIRequest(val prompt: String)
data class AIResponse(val response: String)
