package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService: GeminiApiService by lazy {
        RetrofitClient().apiService
    }

    private class RetrofitClient {
         val apiService: GeminiApiService

         init {
              val retrofit = Retrofit.Builder()
                  .baseUrl(BASE_URL)
                  .client(okHttpClient)
                  .addConverterFactory(MoshiConverterFactory.create(moshi))
                  .build()
              apiService = retrofit.create(GeminiApiService::class.java)
         }
    }

    suspend fun askGemini(prompt: String, systemPrompt: String = ""): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your GEMINI_API_KEY in the AI Studio Secrets panel. (Offline response: Mountain roads require careful attention. Keep speeds under 30 km/h and give way to ascending vehicles!)"
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = if (systemPrompt.isNotEmpty()) {
                GeminiInstruction(parts = listOf(GeminiPart(text = systemPrompt)))
            } else null
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No suggestions. Drive safely!"
        } catch (e: Exception) {
            "Safety Tip: For mountain climbs like Nainital, ensure you are in low gears (1st/2nd) and sound horn on blind turns. (API Error: ${e.localizedMessage ?: "Network issue"})"
        }
    }
}
