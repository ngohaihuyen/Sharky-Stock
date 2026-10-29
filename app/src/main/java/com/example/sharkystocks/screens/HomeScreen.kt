package com.example.sharkystocks.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sharkystocks.StockListState
import com.example.sharkystocks.StockViewModel
import com.example.sharkystocks.data.model.Stock
import com.example.sharkystocks.ui.theme.ChartGreen
import com.example.sharkystocks.ui.theme.ChartRed
import com.example.sharkystocks.ui.theme.ThemeManager

private val SharkyNavy = Color(0xFF071A23)
private val SharkyNavySoft = Color(0xFF071A23)
private val SharkyTeal = Color(0xFF55E6B1)
private val SharkyMuted = Color(0xFF8FA8AF)
private val SharkyLine = Color(0xFF24414A)
private val SharkyWhite = Color(0xFFF4FAF8)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StockViewModel) {
    val screenState by viewModel.state.collectAsStateWithLifecycle()
    val miniChartPrices by viewModel.miniChartPrices.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val allStocks = when (val listState = screenState.stockListState) {
        is StockListState.Success -> listState.stocks
        else -> screenState.stocks.orEmpty()
    }

    val filteredStocks = remember(allStocks, searchText) {
        if (searchText.isBlank()) {
            allStocks
        } else {
            val query = searchText.trim().lowercase()
            allStocks.filter { stock ->
                stock.symbol.lowercase().contains(query) ||
                        getCompanyName(stock.symbol).lowercase().contains(query) ||
                        getSector(stock.symbol).lowercase().contains(query)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.refreshHomeScreen()
    }

    LaunchedEffect(allStocks) {
        val symbols = allStocks
            .map { it.symbol.trim().uppercase() }
            .filter { it.isNotBlank() }
            .distinct()

        if (symbols.isNotEmpty()) {
            viewModel.refreshMiniCharts(symbols)

        }
    }

    val isLoading = screenState.stockListState == StockListState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshStocks() }
    )

    val pageBackground = if (ThemeManager.isDarkMode) {
        SharkyTeal
    } else {
        Color(0xFFF2F7F6)
    }
    val textPrimary = if (ThemeManager.isDarkMode) SharkyWhite else Color(0xFF102B35)
    val textSecondary = if (ThemeManager.isDarkMode) SharkyMuted else Color(0xFF60777D)
    val cardBackground = if (ThemeManager.isDarkMode) SharkyNavySoft else Color.White
    val borderColor = if (ThemeManager.isDarkMode) SharkyLine else Color(0xFFDDE9E6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SharkyNavy, Color(0xFF0B3037))
                        )
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(SharkyTeal.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            color = SharkyTeal,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SHARKY STOCKS",
                            color = SharkyNavy,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "MARKET WATCH",
                            color = SharkyMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.1.sp
                        )
                    }

                    IconButton(
                        onClick = { ThemeManager.toggleTheme() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.09f))
                    ) {
                        Icon(
                            imageVector = if (ThemeManager.isDarkMode) {
                                Icons.Filled.LightMode
                            } else {
                                Icons.Filled.DarkMode
                            },
                            contentDescription = "Toggle theme",
                            tint = SharkyTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your market,\nclearly in view.",
                    color = SharkyWhite,
                    fontSize = 29.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Follow the assets that matter to you.",
                    color = SharkyMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                SearchBar(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    onSearchActiveChanged = { isSearchActive = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Overview uses only the stock count already available in the app.
            // No fabricated index values or market performance figures are shown.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (searchText.isBlank()) "Market watch" else "Search results",
                            color = textPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (searchText.isBlank()) {
                                "Your tracked assets in one place"
                            } else {
                                "Matches for “$searchText”"
                            },
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        color = if (ThemeManager.isDarkMode) {
                            SharkyTeal.copy(alpha = 0.12f)
                        } else {
                            Color(0xFFDDF8EC)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (ThemeManager.isDarkMode) SharkyTeal.copy(alpha = 0.25f)
                            else Color(0xFFBCEBD8)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SharkyTeal)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "${filteredStocks.size} assets",
                                color = if (ThemeManager.isDarkMode) SharkyTeal else Color(0xFF167A59),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .pullRefresh(pullRefreshState)
            ) {
                when (val listState = screenState.stockListState) {
                    is StockListState.Loading -> {
                        if (screenState.stocks == null) {
                            LoadingContent()
                        } else {
                            StockListContent(
                                stocks = filteredStocks,
                                miniChartPrices = miniChartPrices,
                                viewModel = viewModel,
                                navController = navController,
                                searchText = searchText,
                                cardBackground = cardBackground,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                borderColor = borderColor
                            )
                        }
                    }

                    is StockListState.Success -> {
                        StockListContent(
                            stocks = filteredStocks,
                            miniChartPrices = miniChartPrices,
                            viewModel = viewModel,
                            navController = navController,
                            searchText = searchText,
                            cardBackground = cardBackground,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            borderColor = borderColor
                        )
                    }

                    is StockListState.Error -> {
                        MessageState(
                            title = "Couldn't connect",
                            message = listState.message,
                            buttonText = "Try again",
                            onButtonClick = { viewModel.retryLoading() },
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    is StockListState.Empty -> {
                        MessageState(
                            title = if (searchText.isBlank()) "No assets yet" else "No matches found",
                            message = if (searchText.isBlank()) {
                                "Your stock list is empty right now."
                            } else {
                                "Try another ticker, company name, or sector."
                            },
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = cardBackground,
                    contentColor = SharkyTeal
                )

            }

            Text(
                text = "SHARKY STOCKS  •  MARKET DATA",
                color = textSecondary.copy(alpha = 0.65f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Loading stock data…",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MessageState(
    title: String,
    message: String,
    textPrimary: Color,
    textSecondary: Color,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SharkyTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = SharkyTeal,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = message,
                color = textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            if (buttonText != null && onButtonClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SharkyTeal,
                        contentColor = SharkyNavy
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockListContent(
    stocks: List<Stock>,
    miniChartPrices: Map<String, List<Float>> = emptyMap(),
    viewModel: StockViewModel,
    navController: NavController,
    searchText: String,
    cardBackground: Color = MaterialTheme.colorScheme.surface,
    textPrimary: Color = MaterialTheme.colorScheme.onSurface,
    textSecondary: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    borderColor: Color = Color.Transparent
) {
    if (stocks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (searchText.isNotBlank()) "No matching assets" else "No assets available",
                    color = textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (searchText.isNotBlank()) {
                        "Try a different ticker or company name."
                    } else {
                        "Pull down to refresh the list."
                    },
                    color = textSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stocks, key = { it.symbol }) { stock ->
                EnhancedStockItem(
                    stock = stock,
                    historicalPrices = miniChartPrices[stock.symbol].orEmpty(),
                    onClick = {
                        viewModel.selectStock(stock)
                        navController.navigate("details")
                    },
                    cardBackground = cardBackground,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    borderColor = borderColor
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .onFocusChanged { focusState ->
                onSearchActiveChanged(focusState.isFocused || searchText.isNotEmpty())
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search stocks",
                tint = SharkyTeal,
                modifier = Modifier.size(21.dp)
            )

            Spacer(modifier = Modifier.width(11.dp))

            BasicTextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                textStyle = TextStyle(
                    color = SharkyWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = Brush.verticalGradient(
                    colors = listOf(SharkyTeal, SharkyTeal)
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Search ticker, company, sector",
                                color = SharkyMuted,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                },
                singleLine = true
            )

            if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchTextChanged("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = SharkyMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedStockItem(
    stock: Stock,
    historicalPrices: List<Float> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardBackground: Color = MaterialTheme.colorScheme.surface,
    textPrimary: Color = MaterialTheme.colorScheme.onSurface,
    textSecondary: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    borderColor: Color = Color.Transparent
) {
    val isPositive = !stock.change.trim().startsWith("-")
    val changeColor = if (isPositive) ChartGreen else ChartRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(49.dp)
                    .clip(RoundedCornerShape(15.dp))
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
                    fontSize = if (stock.symbol.length > 4) 11.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getCompanyName(stock.symbol),
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getSector(stock.symbol),
                    color = textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Historical mini chart: real closing prices loaded by StockViewModel.
            MiniMovementChart(
                prices = historicalPrices,
                modifier = Modifier
                    .width(84.dp)
                    .height(42.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stock.price,
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = changeColor.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text(
                            text = "${if (isPositive) "↗" else "↘"} ${stock.changePercent}",
                            color = changeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMovementChart(
    prices: List<Float>,
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "—",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        return
    }

    val isPositive = prices.last() >= prices.first()
    val lineColor = if (isPositive) ChartGreen else ChartRed

    Canvas(modifier = modifier) {
        val minPrice = prices.minOrNull() ?: return@Canvas
        val maxPrice = prices.maxOrNull() ?: return@Canvas
        val priceRange = maxPrice - minPrice

        val verticalPadding = 4.dp.toPx()
        val chartHeight = (size.height - verticalPadding * 2).coerceAtLeast(1f)
        val path = Path()

        prices.forEachIndexed { index, price ->
            val x = index * size.width / (prices.size - 1)

            // Normalize each real price to the chart's available vertical range.
            val normalizedY = if (priceRange > 0f) {
                (price - minPrice) / priceRange
            } else {
                0.5f
            }

            val y = verticalPadding + chartHeight * (1f - normalizedY)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        // Highlight the most recent closing price.
        val lastNormalizedY = if (priceRange > 0f) {
            (prices.last() - minPrice) / priceRange
        } else {
            0.5f
        }
        val lastY = verticalPadding + chartHeight * (1f - lastNormalizedY)

        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(size.width, lastY)
        )
    }
}
