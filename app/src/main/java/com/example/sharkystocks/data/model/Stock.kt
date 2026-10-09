package com.example.sharkystocks.data.model

data class Stock(
    val symbol: String,
    val price: String,
    val change: String,
    val changePercent: String
)