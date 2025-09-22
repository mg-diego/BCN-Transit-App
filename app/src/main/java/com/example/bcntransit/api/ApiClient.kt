package com.example.bcntransit.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://bcn-transit-app.duckdns.org:8000/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente con timeout aumentado solo para TramApiService
    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)  // tiempo de conexión
        .readTimeout(60, TimeUnit.SECONDS)     // tiempo de lectura
        .writeTimeout(60, TimeUnit.SECONDS)    // tiempo de escritura
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val metroApiService: MetroApiService = retrofit.create(MetroApiService::class.java)
    val rodaliesApiService: RodaliesApiService = retrofit.create(RodaliesApiService::class.java)
    val fgcApiService: FgcApiService = retrofit.create(FgcApiService::class.java)
    val busApiService: BusApiService = retrofit.create(BusApiService::class.java)
    val bicingApiService: BicingApiService = retrofit.create(BicingApiService::class.java)
    val userApiService : UserApiService = retrofit.create(UserApiService::class.java)
    val tramApiService: TramApiService = retrofit.create(TramApiService::class.java)
    val nearApiService : ResultsApiService = retrofit.create(ResultsApiService::class.java)
}