package com.example.bcntransit.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://<BACKEND>/api/" // <- pon tu URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val metroApiService: MetroApiService = retrofit.create(MetroApiService::class.java)
    val tramApiService: TramApiService = retrofit.create(TramApiService::class.java)
    val rodaliesApiService: RodaliesApiService = retrofit.create(RodaliesApiService::class.java)
    val fgcApiService: FgcApiService = retrofit.create(FgcApiService::class.java)
}