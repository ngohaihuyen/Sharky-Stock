package com.example.sharkystocks.data.model

data class ChartPoint(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class ChartRange(val label: String, val days: Int) {
    ONE_WEEK("1W", 5),
    ONE_MONTH("1M", 22),
    THREE_MONTHS("3M", 66),
    SIX_MONTHS("6M", 130);

    companion object {
        // Fetch the longest range once, then slice it for shorter ranges (saves API calls)
        val maxDays: Int = entries.maxOf { it.days }
    }
}
