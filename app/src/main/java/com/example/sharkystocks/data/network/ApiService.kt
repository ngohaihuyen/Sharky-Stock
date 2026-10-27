package com.example.sharkystocks.data.network

import com.example.sharkystocks.data.model.StockResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("eod/latest")
    suspend fun getStocks(
        @Query("access_key") accessKey: String = "dbb36fe99f32cfe0364d5076f4f1ddb3",
        @Query("symbols") symbols: String = "AAPL,MSFT,GOOGL,AMZN,META,TSLA,NVDA,NFLX,JPM,MA,JNJ,UNH,XOM,CVX,PG,HD,BAC,DIS,PFE,INTC,AMD,QCOM,IBM,ADBE,CRM,CSCO,WMT,KO,NAB,MC,BLK,BX,ORCL,RBLX"
    ): StockResponse

    @GET("eod")
    suspend fun getHistoricalStocks(
        @Query("access_key") accessKey: String,
        @Query("symbols") symbols: String,
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("sort") sort: String = "ASC",
        @Query("limit") limit: Int = 1000
    ): StockResponse

    // End-of-day price history for ONE symbol, newest first

    // End-of-day history for one stock symbol
    @GET("eod")
    suspend fun getStockHistory(
        @Query("access_key") accessKey: String = "dbb36fe99f32cfe0364d5076f4f1ddb3",
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
