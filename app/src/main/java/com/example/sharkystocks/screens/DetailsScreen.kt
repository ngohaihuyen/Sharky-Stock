package com.example.sharkystocks.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CurrencyExchange
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sharkystocks.StockListState
import com.example.sharkystocks.StockViewModel
import com.example.sharkystocks.ui.theme.ChartGreen
import com.example.sharkystocks.ui.theme.ChartRed
import com.example.sharkystocks.ui.theme.ThemeManager

private val SharkyNavy = Color(0xFF071A23)
private val SharkyNavySoft = Color(0xFF071A23)
private val SharkyTeal = Color(0xFF55E6B1)
private val SharkyMuted = Color(0xFF8FA8AF)
private val SharkyLine = Color(0xFF24414A)
private val SharkyWhite = Color(0xFFF4FAF8)

@Composable
fun DetailsScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stock = state.selectedStock

    val elevation by animateDpAsState(
        targetValue = 0.dp,
        label = "details_card_elevation"
    )


    val pageBackground = if (ThemeManager.isDarkMode) SharkyNavy else Color(0xFFF2F7F6)
    val textPrimary = if (ThemeManager.isDarkMode) SharkyWhite else Color(0xFF102B35)
    val textSecondary = if (ThemeManager.isDarkMode) SharkyMuted else Color(0xFF60777D)
    val cardBackground = if (ThemeManager.isDarkMode) SharkyNavySoft else Color.White
    val borderColor = if (ThemeManager.isDarkMode) SharkyLine else Color(0xFFDDE9E6)

    LaunchedEffect(stock?.symbol) {
        stock?.symbol?.let { symbol ->
            viewModel.refreshDetailsScreen(symbol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SharkyNavy, Color(0xFF0B3037))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
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

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = "Stock Details",
                    color = SharkyWhite,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your market at a glance",
                    color = SharkyMuted,
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when {
                stock != null -> {
                    val isPositive = !stock.change.trim().startsWith("-")
                    val changeColor = if (isPositive) ChartGreen else ChartRed

                    // Main stock overview
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = elevation,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackground
                        ),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF174B50), Color(0xFF102C37))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stock.symbol,
                                        color = SharkyTeal,
                                        fontSize = if (stock.symbol.length > 4) 14.sp else 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = getCompanyName(stock.symbol),
                                        color = textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = getSector(stock.symbol),
                                        color = textSecondary,
                                        fontSize = 13.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = SharkyTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.height(28.dp))

                            Text(
                                text = "CURRENT PRICE",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = stock.price,
                                color = textPrimary,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(changeColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPositive) "↗" else "↘",
                                    color = changeColor,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "${stock.change} (${stock.changePercent})",
                                        color = changeColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPositive) "Positive movement" else "Negative movement",
                                        color = changeColor.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SharkyTeal)
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = "Latest available market data",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Market statistics
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(23.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SharkyTeal)
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "Market Statistics",
                                color = textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.weight(1f)) {
                                InfoCard(
                                    title = "Market Cap",
                                    value = getMarketCap(stock.symbol),
                                    iconRes = android.R.drawable.ic_menu_my_calendar,
                                    color = textPrimary,
                                    cardBackground = cardBackground,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    borderColor = borderColor
                                )
                            }

                            Box(Modifier.weight(1f)) {
                                InfoCard(
                                    title = "Volume",
                                    value = getVolume(stock.symbol),
                                    iconRes = android.R.drawable.ic_menu_sort_by_size,
                                    color = if (ThemeManager.isDarkMode) SharkyTeal else Color(0xFF167A59),
                                    cardBackground = cardBackground,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    borderColor = borderColor
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.weight(1f)) {
                                InfoCard(
                                    title = "52W High",
                                    value = get52WeekHigh(stock.symbol),
                                    iconRes = android.R.drawable.arrow_up_float,
                                    color = ChartGreen,
                                    cardBackground = cardBackground,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    borderColor = borderColor
                                )
                            }

                            Box(Modifier.weight(1f)) {
                                InfoCard(
                                    title = "52W Low",
                                    value = get52WeekLow(stock.symbol),
                                    iconRes = android.R.drawable.arrow_down_float,
                                    color = ChartRed,
                                    cardBackground = cardBackground,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    borderColor = borderColor
                                )
                            }
                        }
                    }

                    // Actions
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // TODO: Connect to the watchlist feature.
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBackground,
                                contentColor = textPrimary
                            ),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                tint = SharkyTeal
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = "Add to Watchlist",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                navController.navigate("chart")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SharkyTeal,
                                contentColor = SharkyNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = "View Price Chart",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                state.stockListState == StockListState.Loading -> {
                    LoadingDetails(textSecondary = textSecondary)
                }

                else -> {
                    EmptyDetails(
                        onBack = { navController.popBackStack() },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}


@Composable
fun InfoCard(
    title: String,
    value: String,
    iconRes: Int,
    color: Color,
    cardBackground: Color = if (ThemeManager.isDarkMode) SharkyNavySoft else Color.White,
    textPrimary: Color = if (ThemeManager.isDarkMode) SharkyWhite else Color(0xFF102B35),
    textSecondary: Color = if (ThemeManager.isDarkMode) SharkyMuted else Color(0xFF60777D),
    borderColor: Color = if (ThemeManager.isDarkMode) SharkyLine else Color(0xFFDDE9E6)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = title,
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = value,
                color = color,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingDetails(textSecondary: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = SharkyTeal,
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Loading stock details...",
                color = textSecondary
            )
        }
    }
}

@Composable
private fun EmptyDetails(
    onBack: () -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = null,
                tint = SharkyTeal,
                modifier = Modifier.size(54.dp)
            )

            Text(
                text = "No Stock Selected",
                color = textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select a stock from the home screen.",
                color = textSecondary,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharkyTeal,
                    contentColor = SharkyNavy
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Home", fontWeight = FontWeight.Bold)
            }
        }
    }
}