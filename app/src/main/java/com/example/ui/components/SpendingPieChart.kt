package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpendingSummary
import com.example.data.model.Currency
import com.example.data.model.SpendingCategory
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Recharts / D3-style Native Jetpack Compose Donut/Pie Chart Component
 * Visualizes monthly spending categories (Airtime, Utility Bills, Transfers, etc.)
 * with interactive slice tapping, responsive animations, and currency conversion.
 */
@Composable
fun MonthlySpendingPieChartCard(
    breakdown: List<CategorySpendingSummary>,
    activeCurrency: Currency,
    selectedCategory: SpendingCategory?,
    selectedMonthIndex: Int,
    onSelectMonth: (Int) -> Unit,
    onSelectCategory: (SpendingCategory?) -> Unit,
    onToggleCurrency: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    val dfCdf = remember { DecimalFormat("#,###") }
    val dfUsd = remember { DecimalFormat("#,##0.00") }

    val totalSpendingAmount = remember(breakdown, activeCurrency) {
        if (activeCurrency == Currency.CDF) {
            breakdown.sumOf { it.totalCdf }
        } else {
            breakdown.sumOf { it.totalUsd }
        }
    }

    val totalTxCount = remember(breakdown) {
        breakdown.sumOf { it.transactionCount }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_spending_pie_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header with Title & Quick Currency Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FutaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Graphique des dépenses",
                            tint = FutaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dépenses Mensuelles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Analyse & Répartition par catégorie",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Currency Toggle Pill on Card Header
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val next = if (activeCurrency == Currency.CDF) Currency.USD else Currency.CDF
                            onToggleCurrency(next)
                        }
                        .testTag("spending_chart_currency_toggle"),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Changer devise",
                            tint = FutaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeCurrency == Currency.CDF) "CDF (FC)" else "USD ($)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month Filter Chips (Ce mois, Mois dernier, Août)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Ce mois (Octobre)",
                    1 to "Septembre",
                    2 to "Août"
                ).forEach { (idx, label) ->
                    val isSelected = selectedMonthIndex == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onSelectMonth(idx)
                            onSelectCategory(null)
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FutaPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("spending_month_chip_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (breakdown.isEmpty() || totalSpendingAmount <= 0.0) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucune dépense enregistrée pour cette période.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Interactive Donut Chart Canvas with Center Metric
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutPieChartCanvas(
                        breakdown = breakdown,
                        selectedCategory = selectedCategory,
                        onSliceSelected = { cat ->
                            if (selectedCategory == cat) {
                                onSelectCategory(null) // deselect on re-tap
                            } else {
                                onSelectCategory(cat)
                            }
                        },
                        modifier = Modifier
                            .size(220.dp)
                            .testTag("interactive_pie_chart_canvas")
                    )

                    // Center Content (Recharts style dynamic metric)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(130.dp)
                            .padding(4.dp)
                    ) {
                        AnimatedContent(
                            targetState = selectedCategory,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "center_chart_metric"
                        ) { cat ->
                            if (cat == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total Dépenses",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val amountText = if (activeCurrency == Currency.CDF) {
                                        "${dfCdf.format(totalSpendingAmount)} FC"
                                    } else {
                                        "$${dfUsd.format(totalSpendingAmount)}"
                                    }
                                    Text(
                                        text = amountText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$totalTxCount opérations",
                                        fontSize = 10.sp,
                                        color = FutaPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                val catSummary = breakdown.find { it.category == cat }
                                val catAmount = if (activeCurrency == Currency.CDF) {
                                    "${dfCdf.format(catSummary?.totalCdf ?: 0.0)} FC"
                                } else {
                                    "$${dfUsd.format(catSummary?.totalUsd ?: 0.0)}"
                                }
                                val percentage = catSummary?.percentage ?: 0f

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = cat.shortName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(cat.colorHex),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = catAmount,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        color = Color(cat.colorHex).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.1f", percentage)}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(cat.colorHex),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Recharts-style Tooltip / Detail Banner when category is selected
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    val cat = selectedCategory
                    if (cat != null) {
                        val summary = breakdown.find { it.category == cat }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(cat.colorHex).copy(alpha = 0.10f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("selected_category_detail_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(cat.colorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${summary?.transactionCount ?: 0} transaction(s) • ${String.format("%.1f", summary?.percentage ?: 0f)}% du budget",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onSelectCategory(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Réinitialiser sélection",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Legend & Category Breakdown Items
                Text(
                    text = "Répartition par poste de dépense",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    breakdown.forEach { item ->
                        SpendingCategoryRowItem(
                            item = item,
                            activeCurrency = activeCurrency,
                            isSelected = selectedCategory == item.category,
                            onClick = {
                                if (selectedCategory == item.category) {
                                    onSelectCategory(null)
                                } else {
                                    onSelectCategory(item.category)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing the Donut Pie Chart segments.
 * Calculates touch angle to select the exact category slice.
 */
@Composable
private fun DonutPieChartCanvas(
    breakdown: List<CategorySpendingSummary>,
    selectedCategory: SpendingCategory?,
    onSliceSelected: (SpendingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(breakdown) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val totalPercentage = remember(breakdown) {
        breakdown.sumOf { it.percentage.toDouble() }.toFloat().coerceAtLeast(1f)
    }

    // Prepare slice angles
    val sliceAngles = remember(breakdown, totalPercentage) {
        var currentStart = -90f // Start from top 12 o'clock
        breakdown.map { item ->
            val sweep = (item.percentage / totalPercentage) * 360f
            val start = currentStart
            currentStart += sweep
            Triple(item.category, start, sweep)
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(breakdown, sliceAngles) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = tapOffset.x - center.x
                    val dy = tapOffset.y - center.y
                    val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                    val baseRadius = minOf(size.width, size.height).toFloat() / 2f
                    val innerCutoutRadius = baseRadius * 0.52f
                    val outerActiveRadius = baseRadius * 0.98f

                    // Check if tap fell inside donut ring
                    if (dist in innerCutoutRadius..outerActiveRadius) {
                        var angleDeg = (atan2(dy.toDouble(), dx.toDouble()) * (180.0 / PI)).toFloat()
                        // Normalize to [0..360] starting from -90 deg (top)
                        var normalized = angleDeg - (-90f)
                        if (normalized < 0f) normalized += 360f

                        sliceAngles.find { (_, start, sweep) ->
                            var sliceStartNorm = start - (-90f)
                            if (sliceStartNorm < 0f) sliceStartNorm += 360f
                            val sliceEndNorm = sliceStartNorm + sweep
                            if (sliceEndNorm <= 360f) {
                                normalized >= sliceStartNorm && normalized < sliceEndNorm
                            } else {
                                normalized >= sliceStartNorm || normalized < (sliceEndNorm - 360f)
                            }
                        }?.let { (cat, _, _) ->
                            onSliceSelected(cat)
                        }
                    }
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f * 0.88f
        val normalStrokeWidth = 28.dp.toPx()
        val activeStrokeWidth = 36.dp.toPx()
        val padAngle = 2.5f // D3/Recharts padAngle gap between segments

        sliceAngles.forEach { (category, startAngle, sweepAngle) ->
            val isSelected = selectedCategory == category
            val strokeWidth = if (isSelected) activeStrokeWidth else normalStrokeWidth
            val color = Color(category.colorHex)

            // Calculate animated sweep
            val animatedSweep = (sweepAngle * animationProgress.value)

            if (animatedSweep > padAngle) {
                val effectiveStart = startAngle + (padAngle / 2f)
                val effectiveSweep = (animatedSweep - padAngle).coerceAtLeast(0.5f)

                val radius = if (isSelected) outerRadius + 4.dp.toPx() else outerRadius
                val arcSize = Size(radius * 2f, radius * 2f)
                val topLeft = Offset(center.x - radius, center.y - radius)

                // Optional subtle outer ring for selected slice (Recharts activeShape effect)
                if (isSelected) {
                    drawArc(
                        color = color.copy(alpha = 0.25f),
                        startAngle = effectiveStart - 1f,
                        sweepAngle = effectiveSweep + 2f,
                        useCenter = false,
                        topLeft = Offset(center.x - (radius + 6.dp.toPx()), center.y - (radius + 6.dp.toPx())),
                        size = Size((radius + 6.dp.toPx()) * 2f, (radius + 6.dp.toPx()) * 2f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                drawArc(
                    color = color,
                    startAngle = effectiveStart,
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

/**
 * Category breakdown item row with color dot, title, transaction count, percentage progress, and amount.
 */
@Composable
private fun SpendingCategoryRowItem(
    item: CategorySpendingSummary,
    activeCurrency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dfCdf = remember { DecimalFormat("#,###") }
    val dfUsd = remember { DecimalFormat("#,##0.00") }

    val categoryColor = Color(item.category.colorHex)
    val amountText = if (activeCurrency == Currency.CDF) {
        "${dfCdf.format(item.totalCdf)} FC"
    } else {
        "$${dfUsd.format(item.totalUsd)}"
    }

    val animatedElevation by animateFloatAsState(
        targetValue = if (isSelected) 3f else 0f,
        label = "item_elevation"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            categoryColor.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        tonalElevation = animatedElevation.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spending_legend_item_${item.category.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Colored Dot
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(item.category),
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Transactions count
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category.shortName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Mini Percentage Progress Bar
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (item.percentage / 100f).coerceIn(0f, 1f))
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(categoryColor)
                        )
                    }
                    Text(
                        text = "${item.transactionCount} op.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount and Percentage Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${String.format("%.1f", item.percentage)}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryColor
                )
            }
        }
    }
}

/**
 * Returns distinct Material icon for each spending category.
 */
fun getCategoryIcon(category: SpendingCategory): ImageVector {
    return when (category) {
        SpendingCategory.AIRTIME -> Icons.Default.PhoneAndroid
        SpendingCategory.UTILITY_BILLS -> Icons.Default.Bolt
        SpendingCategory.TRANSFERS -> Icons.Default.Send
        SpendingCategory.MERCHANT -> Icons.Default.ShoppingCart
        SpendingCategory.WITHDRAWALS -> Icons.Default.LocalAtm
        SpendingCategory.OTHER -> Icons.Default.ReceiptLong
    }
}
