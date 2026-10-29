package com.example.sharkystocks.data.network

import com.example.sharkystocks.data.model.StockResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.sharkystocks.BuildConfig

interface ApiService {
    @GET("eod/latest")
    suspend fun getStocks(
        @Query("access_key") accessKey: String = BuildConfig.MARKETSTACK_API_KEY,
        @Query("symbols") symbols: String = "AAPL,MSFT,GOOGL,AMZN,META,TSLA,JPM,IBM,ORCL,RBLX"
    ): StockResponse


    @GET("eod")
    suspend fun getStockHistory(
        @Query(value = "access_key") accessKey: String = BuildConfig.MARKETSTACK_API_KEY,
        @Query("symbols") symbols: String,
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("sort") sort: String = "ASC",
        @Query("limit") limit: Int = 100
    ): StockResponse

}

val apiService: ApiService = Retrofit.Builder()
    .baseUrl("https://api.marketstack.com/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)
