package com.example.sharkystocks.data.network

import com.example.sharkystocks.data.model.StockResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Put your own Marketstack key here (used by every request below)
const val MARKETSTACK_ACCESS_KEY = "e79c039983faf91d490f1a0d08b3cc1e"

interface ApiService {
    @GET("eod/latest")
    suspend fun getStocks(
        @Query("access_key") accessKey: String = MARKETSTACK_ACCESS_KEY,
        @Query("symbols") symbols: String = "AAPL,MSFT,GOOGL,AMZN,META,TSLA,NVDA,NFLX,JPM,MA,JNJ,UNH,XOM,CVX,PG,HD,BAC,DIS,PFE,INTC,AMD,QCOM,IBM,ADBE,CRM,CSCO,NFLX,WMT,KO, NAB, MC,BLK,BX,ORCL,RBLX"
    ): StockResponse

    // End-of-day price history for ONE symbol, newest first
    @GET("eod")
    suspend fun getStockHistory(
        @Query("access_key") accessKey: String = MARKETSTACK_ACCESS_KEY,
        @Query("symbols") symbols: String,
        @Query("limit") limit: Int = 130,
        @Query("sort") sort: String = "DESC"
    ): StockResponse
}

val apiService: ApiService = Retrofit.Builder()
    .baseUrl("https://api.marketstack.com/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)
