package com.example.sharkystocks.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.sharkystocks.ui.theme.ChartGreen
import com.example.sharkystocks.ui.theme.ChartRed
import com.example.sharkystocks.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val GRID_LINES = 3

private val SharkyNavy = Color(0xFF071A23)
private val SharkyNavySoft = Color(0xFF071A23)
private val SharkyTeal = Color(0xFF55E6B1)
private val SharkyMuted = Color(0xFF8FA8AF)
private val SharkyLine = Color(0xFF24414A)
private val SharkyWhite = Color(0xFFF4FAF8)

@Composable
fun ChartScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val selectedStock = state.value.selectedStock

    val pageBackground = if (ThemeManager.isDarkMode) SharkyNavy else Color(0xFFF2F7F6)
    val textPrimary = if (ThemeManager.isDarkMode) SharkyWhite else Color(0xFF102B35)
    val textSecondary = if (ThemeManager.isDarkMode) SharkyMuted else Color(0xFF60777D)
    val cardBackground = if (ThemeManager.isDarkMode) SharkyNavySoft else Color.White
    val borderColor = if (ThemeManager.isDarkMode) SharkyLine else Color(0xFFDDE9E6)

    LaunchedEffect(key1 = selectedStock?.symbol) {
        selectedStock?.symbol?.let { symbol ->
            viewModel.refreshChartScreen(symbol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SharkyNavy, Color(0xFF0B3037))
                    )
                )
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.09f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SharkyTeal
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Price Chart",
                        color = SharkyWhite,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedStock?.let { "${it.symbol} • ${getCompanyName(it.symbol)}" }
                            ?: "Historical price performance",
                        color = SharkyMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (selectedStock == null) {
                ChartMessage(
                    iconRes = android.R.drawable.ic_menu_search,
                    title = "No Stock Selected",
                    message = "Please select a stock from the home screen",
                    buttonText = "Back to Home",
                    onButtonClick = { navController.popBackStack() },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            } else {
                when (val chartState = state.value.chartState) {
                    StockChartState.Idle, StockChartState.Loading -> {
                        ChartLoading(textSecondary = textSecondary)
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
                            },
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
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
                            },
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    is StockChartState.Success -> {
                        if (chartState.symbol != selectedStock.symbol) {
                            ChartLoading(textSecondary = textSecondary)
                        } else {
                            ChartContent(
                                stock = selectedStock,
                                points = chartState.points,
                                selectedRange = state.value.chartRange,
                                onRangeSelected = { viewModel.selectChartRange(it) },
                                cardBackground = cardBackground,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                borderColor = borderColor
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
    onRangeSelected: (ChartRange) -> Unit,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color
) {
    val visiblePoints = remember(points, selectedRange) { points.takeLast(selectedRange.days) }
    var selectedIndex by remember(visiblePoints) { mutableStateOf<Int?>(null) }
    val cardElevation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        cardElevation.animateTo(
            targetValue = 0f,
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
            containerColor = cardBackground
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF174B50), Color(0xFF102C37))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stock.symbol,
                        color = SharkyTeal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = getCompanyName(stock.symbol),
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getSector(stock.symbol),
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format(Locale.US, "$%.2f", displayedPoint.close),
                color = textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(changeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPositive) "▲" else "▼",
                    color = changeColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%+.2f (%+.2f%%)", change, changePercent),
                    color = changeColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedIndex != null) formatChartDate(displayedPoint.date, withYear = true)
                    else "Past ${selectedRange.label}",
                    color = changeColor.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            StockLineChart(
                points = visiblePoints,
                lineColor = lineColor,
                selectedIndex = selectedIndex,
                onSelectedIndexChange = { selectedIndex = it },
                textSecondary = textSecondary,
                cardBackground = cardBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChartRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
                textSecondary = textSecondary
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Range Stats",
        color = textPrimary,
        fontSize = 20.sp,
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
                color = textPrimary,
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Close",
                value = String.format(Locale.US, "$%.2f", lastPoint.close),
                iconRes = android.R.drawable.ic_media_pause,
                color = textPrimary,
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
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
                color = ChartGreen,
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "${selectedRange.label} Low",
                value = String.format(Locale.US, "$%.2f", visiblePoints.minOf { it.low }),
                iconRes = android.R.drawable.arrow_down_float,
                color = ChartRed,
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
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
                color = if (ThemeManager.isDarkMode) SharkyTeal else Color(0xFF167A59),
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            InfoCard(
                title = "Trading Days",
                value = visiblePoints.size.toString(),
                iconRes = android.R.drawable.ic_menu_my_calendar,
                color = textSecondary,
                cardBackground = cardBackground,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                borderColor = borderColor
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "End-of-day prices from Marketstack • " +
                "${formatChartDate(firstPoint.date, withYear = true)} – ${formatChartDate(lastPoint.date, withYear = true)}",
        modifier = Modifier.fillMaxWidth(),
        color = textSecondary.copy(alpha = 0.7f),
        fontSize = 11.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun StockLineChart(
    points: List<ChartPoint>,
    lineColor: Color,
    selectedIndex: Int?,
    onSelectedIndexChange: (Int?) -> Unit,
    textSecondary: Color,
    cardBackground: Color,
    modifier: Modifier = Modifier
) {
    val drawProgress = remember { Animatable(0f) }
    val currentOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)

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

    val gridColor = if (ThemeManager.isDarkMode) SharkyWhite.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    val guideColor = if (ThemeManager.isDarkMode) SharkyWhite.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f)
    val labelColor = textSecondary
    val dotBorderColor = cardBackground

    Row(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
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

                // Lines
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


//sd Canvas của Compose: đổi giá thành toạ độ (x, y), nối bằng Path, tô gradient bên dưới
//Ánh xạ tọa độ (x, y): dựa vào biên độ giá min/max để quy đổi time & price thành tđộ pixel t/ứng
// path & gradient dùng đối tượng Path để nối các điểm thành đường gấp khúc
// pointerInput: lắng nghe thao tác user để hiển thị đường dọc cùng điểm tròn chi tiết tại mốc giá
//pointerInput để bắt tọa độ Offset (trục X, Y) khi user chạm hoặc kéo ngón tay trên biểu đồ

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

                selectedIndex?.coerceIn(0, points.lastIndex)?.let { index ->
                    drawLine(
                        color = guideColor,
                        start = Offset(xAt(index), topPadding),
                        end = Offset(xAt(index), size.height - bottomPadding),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val dotIndex = selectedIndex?.coerceIn(0, points.lastIndex)
                    ?: if (drawProgress.value >= 1f) points.lastIndex else null

                dotIndex?.let { index ->
                    val center = Offset(xAt(index), yAt(points[index].close))
                    drawCircle(color = dotBorderColor, radius = 6.dp.toPx(), center = center)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (points.isNotEmpty()) {
                    Text(
                        text = formatChartDate(points.first().date),
                        color = labelColor,
                        fontSize = 11.sp
                    )
                    if (points.size > 2) {
                        Text(
                            text = formatChartDate(points[points.size / 2].date),
                            color = labelColor,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = formatChartDate(points.last().date),
                        color = labelColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

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
                    color = labelColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ChartRangeSelector(
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit,
    textSecondary: Color
) {
    val containerBg = if (ThemeManager.isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFE8F1EF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
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
                        if (isSelected) SolidColor(SharkyTeal) else SolidColor(Color.Transparent)
                    )
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    color = if (isSelected) SharkyNavy else textSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChartLoading(textSecondary: Color) {
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
                color = SharkyTeal,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading price history...",
                color = textSecondary,
                fontSize = 13.sp
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
    textPrimary: Color,
    textSecondary: Color,
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
                color = if (isError) ChartRed else textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharkyTeal,
                    contentColor = SharkyNavy
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
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