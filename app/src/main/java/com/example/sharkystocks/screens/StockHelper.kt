package com.example.sharkystocks.screens

internal fun getCompanyName(symbol: String): String {
    return when (symbol) {
        "AAPL" -> "Apple Inc."
        "MSFT" -> "Microsoft"
        "JPM" -> "JPMorgan Chase"
        "GS" -> "Goldman Sachs"
        "AMZN" -> "Amazon"
        "TSLA" -> "Tesla"
        "NFLX" -> "Netflix"
        "GOOGL" -> "Alphabet"
        else -> symbol
    }
}

internal fun getSector(symbol: String): String {
    return when (symbol) {
        "AAPL", "MSFT", "GOOGL" -> "Technology"
        "JPM", "GS" -> "Financial Services"
        "AMZN" -> "E-commerce"
        "TSLA" -> "Automotive"
        "NFLX" -> "Entertainment"
        else -> "Other"
    }
}

internal fun getMarketCap(symbol: String): String {
    return when (symbol) {
        "AAPL" -> "$2.8T"
        "MSFT" -> "$2.5T"
        "GOOGL" -> "$1.7T"
        "AMZN" -> "$1.6T"
        "TSLA" -> "$600B"
        "JPM" -> "$450B"
        "GS" -> "$120B"
        "NFLX" -> "$200B"
        else -> "N/A"
    }
}

internal fun getVolume(symbol: String): String {
    return when (symbol) {
        "AAPL" -> "55.2M"
        "MSFT" -> "28.4M"
        "GOOGL" -> "31.7M"
        "AMZN" -> "42.1M"
        "TSLA" -> "112.5M"
        "JPM" -> "18.3M"
        "GS" -> "4.2M"
        "NFLX" -> "8.7M"
        else -> "N/A"
    }
}

internal fun get52WeekHigh(symbol: String): String {
    return when (symbol) {
        "AAPL" -> "$198.23"
        "MSFT" -> "$376.17"
        "GOOGL" -> "$142.65"
        "AMZN" -> "$145.86"
        "TSLA" -> "$299.29"
        "JPM" -> "$155.93"
        "GS" -> "$386.71"
        "NFLX" -> "$485.00"
        else -> "N/A"
    }
}

internal fun get52WeekLow(symbol: String): String {
    return when (symbol) {
        "AAPL" -> "$124.17"
        "MSFT" -> "$219.35"
        "GOOGL" -> "$83.34"
        "AMZN" -> "$81.43"
        "TSLA" -> "$101.81"
        "JPM" -> "$112.21"
        "GS" -> "$289.53"
        "NFLX" -> "$232.51"
        else -> "N/A"
    }
}