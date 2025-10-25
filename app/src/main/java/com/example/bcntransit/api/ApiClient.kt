package com.example.bcntransit.api


import com.example.bcntransit.data.enums.TransportType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://bcn-transit-bot-production.up.railway.app/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
    val resultsApiService : ResultsApiService = retrofit.create(ResultsApiService::class.java)

    fun from(transportType: TransportType): ApiService {
        return when (transportType) {
            TransportType.METRO -> metroApiService
            TransportType.BUS -> busApiService
            TransportType.RODALIES -> rodaliesApiService
            TransportType.FGC -> fgcApiService
            TransportType.TRAM -> tramApiService
            TransportType.BICING -> bicingApiService
        }
    }
}