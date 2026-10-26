package com.example.sharkystocks.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sharkystocks.StockChartState
import com.example.sharkystocks.StockViewModel
import com.example.sharkystocks.data.model.ChartPoint
import com.example.sharkystocks.data.model.ChartRange
import com.example.sharkystocks.data.model.Stock
import com.example.sharkystocks.ui.theme.BlueGradientEnd
import com.example.sharkystocks.ui.theme.BlueGradientStart
import com.example.sharkystocks.ui.theme.ChartGreen
import com.example.sharkystocks.ui.theme.ChartRed
import com.example.sharkystocks.ui.theme.MintGreen
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val GRID_LINES = 3

@Composable
fun ChartScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val selectedStock = state.value.selectedStock

    LaunchedEffect(key1 = selectedStock?.symbol) {
        selectedStock?.symbol?.let { symbol ->
            viewModel.refreshChartScreen(symbol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {

        // Header (same style as DetailsScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(BlueGradientStart, BlueGradientEnd)
                    )
                )
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Price Chart",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = selectedStock?.let { "${it.symbol} • ${getCompanyName(it.symbol)}" }
                            ?: "Historical price performance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (selectedStock == null) {
                ChartMessage(
                    iconRes = android.R.drawable.ic_menu_search,
                    title = "No Stock Selected",
                    message = "Please select a stock from the home screen",
                    buttonText = "Back to Home",
                    onButtonClick = { navController.popBackStack() }
                )
            } else {
                when (val chartState = state.value.chartState) {
                    StockChartState.Idle, StockChartState.Loading -> {
                        ChartLoading()
                    }

                    is StockChartState.Error -> {
                        ChartMessage(
                            iconRes = android.R.drawable.stat_notify_error,
                            title = "Network Error",
                            message = chartState.message,
                            buttonText = "Retry",
                            isError = true,
                            onButtonClick = {
                                viewModel.refreshChartScreen(selectedStock.symbol, forceRefresh = true)
                            }
                        )
                    }

                    StockChartState.Empty -> {
                        ChartMessage(
                            iconRes = android.R.drawable.ic_menu_info_details,
                            title = "No Chart Data",
                            message = "No price history available for ${selectedStock.symbol}",
                            buttonText = "Retry",
                            onButtonClick = {
                                viewModel.refreshChartScreen(selectedStock.symbol, forceRefresh = true)
                            }
                        )
                    }

                    is StockChartState.Success -> {
                        if (chartState.symbol != selectedStock.symbol) {
                            // Old chart of another stock is still in state -> wait for the new one
                            ChartLoading()
                        } else {
                            ChartContent(
                                stock = selectedStock,
                                points = chartState.points,
                                selectedRange = state.value.chartRange,
                                onRangeSelected = { viewModel.selectChartRange(it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChartContent(
    stock: Stock,
    points: List<ChartPoint>,
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit
) {
    // We fetch 6 months once, then just slice it for 1W / 1M / 3M
    val visiblePoints = remember(points, selectedRange) { points.takeLast(selectedRange.days) }
    var selectedIndex by remember(visiblePoints) { mutableStateOf<Int?>(null) }
    val cardElevation = remember { Animatable(4f) }

    LaunchedEffect(Unit) {
        cardElevation.animateTo(
            targetValue = 8f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
    }

    val firstPoint = visiblePoints.first()
    val lastPoint = visiblePoints.last()
    val displayedPoint = selectedIndex?.let { visiblePoints.getOrNull(it) } ?: lastPoint

    val change = displayedPoint.close - firstPoint.close
    val changePercent = if (firstPoint.close != 0.0) (change / firstPoint.close) * 100 else 0.0
    val isPositive = change >= 0
    val changeColor = if (isPositive) ChartGreen else ChartRed
    val lineColor = if (lastPoint.close >= firstPoint.close) ChartGreen else ChartRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = cardElevation.value.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(BlueGradientStart, BlueGradientEnd)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = getCompanyName(stock.symbol),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getSector(stock.symbol),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format(Locale.US, "$%.2f", displayedPoint.close),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(changeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPositive) "▲" else "▼",
                    style = MaterialTheme.typography.titleMedium,
                    color = changeColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%+.2f (%+.2f%%)", change, changePercent),
                    style = MaterialTheme.typography.titleMedium,
                    color = changeColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedIndex != null) formatChartDate(displayedPoint.date, withYear = true)
                    else "Past ${selectedRange.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = changeColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            StockLineChart(
                points = visiblePoints,
                lineColor = lineColor,
                selectedIndex = selectedIndex,
                onSelectedIndexChange = { selectedIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChartRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Range Stats",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Open",
                value = String.format(Locale.US, "$%.2f", firstPoint.open),
                iconRes = android.R.drawable.ic_media_play,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Close",
                value = String.format(Locale.US, "$%.2f", lastPoint.close),
                iconRes = android.R.drawable.ic_media_pause,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "${selectedRange.label} High",
                value = String.format(Locale.US, "$%.2f", visiblePoints.maxOf { it.high }),
                iconRes = android.R.drawable.arrow_up_float,
                color = ChartGreen
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "${selectedRange.label} Low",
                value = String.format(Locale.US, "$%.2f", visiblePoints.minOf { it.low }),
                iconRes = android.R.drawable.arrow_down_float,
                color = ChartRed
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Avg Volume",
                value = formatVolume(visiblePoints.map { it.volume }.average()),
                iconRes = android.R.drawable.ic_menu_sort_by_size,
                color = MintGreen
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Trading Days",
                value = visiblePoints.size.toString(),
                iconRes = android.R.drawable.ic_menu_my_calendar,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "End-of-day prices from Marketstack • " +
                "${formatChartDate(firstPoint.date, withYear = true)} – ${formatChartDate(lastPoint.date, withYear = true)}",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
    )
}

@Composable
fun StockLineChart(
    points: List<ChartPoint>,
    lineColor: Color,
    selectedIndex: Int?,
    onSelectedIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawProgress = remember { Animatable(0f) }
    val currentOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)

    // Re-play the "drawing" animation whenever the data / range changes
    LaunchedEffect(points) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = LinearEasing)
        )
    }

    val maxPrice = points.maxOfOrNull { it.close } ?: 0.0
    val minPrice = points.minOfOrNull { it.close } ?: 0.0
    val priceSpan = if (maxPrice - minPrice > 0.0) maxPrice - minPrice else 1.0

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val guideColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val dotBorderColor = MaterialTheme.colorScheme.surface

    Row(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    // Touch / drag on the chart to inspect a specific day
                    .pointerInput(points) {
                        fun indexAt(x: Float): Int {
                            if (points.size <= 1 || size.width == 0) return 0
                            return ((x / size.width) * (points.size - 1))
                                .roundToInt()
                                .coerceIn(0, points.lastIndex)
                        }

                        awaitEachGesture {
                            val down = awaitFirstDown()
                            currentOnSelectedIndexChange(indexAt(down.position.x))

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                val delta = change.positionChange()
                                // Horizontal drag belongs to the chart, vertical drag still scrolls the screen
                                if (abs(delta.x) > abs(delta.y)) change.consume()

                                currentOnSelectedIndexChange(indexAt(change.position.x))
                            }

                            currentOnSelectedIndexChange(null)
                        }
                    }
            ) {
                val topPadding = 8.dp.toPx()
                val bottomPadding = 8.dp.toPx()
                val chartHeight = size.height - topPadding - bottomPadding
                val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f

                fun xAt(index: Int): Float = index * stepX
                fun yAt(price: Double): Float =
                    topPadding + ((maxPrice - price) / priceSpan * chartHeight).toFloat()

                // Grid lines
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 0..GRID_LINES) {
                    val y = topPadding + chartHeight * i / GRID_LINES
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                if (points.isEmpty()) return@Canvas

                if (points.size == 1) {
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(size.width / 2, yAt(points[0].close))
                    )
                    return@Canvas
                }

                // Line + gradient area, drawn progressively for the intro animation
                val visibleCount = (points.size * drawProgress.value).toInt().coerceIn(2, points.size)
                val linePath = Path()
                val fillPath = Path()

                for (i in 0 until visibleCount) {
                    val x = xAt(i)
                    val y = yAt(points[i].close)
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, size.height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(xAt(visibleCount - 1), size.height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = topPadding,
                        endY = size.height
                    )
                )

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Vertical guide line for the touched day
                selectedIndex?.coerceIn(0, points.lastIndex)?.let { index ->
                    drawLine(
                        color = guideColor,
                        start = Offset(xAt(index), topPadding),
                        end = Offset(xAt(index), size.height - bottomPadding),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Dot on the touched day, or on the latest day once the animation is done
                val dotIndex = selectedIndex?.coerceIn(0, points.lastIndex)
                    ?: if (drawProgress.value >= 1f) points.lastIndex else null

                dotIndex?.let { index ->
                    val center = Offset(xAt(index), yAt(points[index].close))
                    drawCircle(color = dotBorderColor, radius = 6.dp.toPx(), center = center)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date labels under the chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (points.isNotEmpty()) {
                    Text(
                        text = formatChartDate(points.first().date),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                    if (points.size > 2) {
                        Text(
                            text = formatChartDate(points[points.size / 2].date),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor
                        )
                    }
                    Text(
                        text = formatChartDate(points.last().date),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        }

        // Price labels on the right, aligned with the grid lines
        Column(
            modifier = Modifier
                .height(220.dp)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            for (i in 0..GRID_LINES) {
                val price = maxPrice - (maxPrice - minPrice) * i / GRID_LINES
                Text(
                    text = formatAxisPrice(price),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
private fun ChartRangeSelector(
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChartRange.entries.forEach { range ->
            val isSelected = range == selectedRange

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(
                            colors = listOf(BlueGradientStart, BlueGradientEnd)
                        ) else SolidColor(Color.Transparent)
                    )
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ChartLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading price history...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ChartMessage(
    iconRes: Int,
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun formatChartDate(date: String, withYear: Boolean = false): String {
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: return date
        SimpleDateFormat(if (withYear) "MMM d, yyyy" else "MMM d", Locale.US).format(parsed)
    } catch (e: Exception) {
        date
    }
}

private fun formatAxisPrice(price: Double): String {
    return if (price >= 1000) String.format(Locale.US, "%.0f", price)
    else String.format(Locale.US, "%.2f", price)
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", volume / 1_000_000_000)
        volume >= 1_000_000 -> String.format(Locale.US, "%.1fM", volume / 1_000_000)
        volume >= 1_000 -> String.format(Locale.US, "%.1fK", volume / 1_000)
        else -> String.format(Locale.US, "%.0f", volume)
    }
}
