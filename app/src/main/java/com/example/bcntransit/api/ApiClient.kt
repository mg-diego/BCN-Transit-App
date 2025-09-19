package com.example.bcntransit.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://192.168.1.149:8000/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente por defecto para la mayoría de servicios
    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofitDefault = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val metroApiService: MetroApiService = retrofitDefault.create(MetroApiService::class.java)
    val rodaliesApiService: RodaliesApiService = retrofitDefault.create(RodaliesApiService::class.java)
    val fgcApiService: FgcApiService = retrofitDefault.create(FgcApiService::class.java)
    val busApiService: BusApiService = retrofitDefault.create(BusApiService::class.java)
    val nearApiService : NearApiService = retrofitDefault.create(NearApiService::class.java)

    // Cliente con timeout aumentado solo para TramApiService
    private val tramClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)  // tiempo de conexión
        .readTimeout(60, TimeUnit.SECONDS)     // tiempo de lectura
        .writeTimeout(60, TimeUnit.SECONDS)    // tiempo de escritura
        .build()

    private val retrofitTram = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(tramClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val tramApiService: TramApiService = retrofitTram.create(TramApiService::class.java)
}
