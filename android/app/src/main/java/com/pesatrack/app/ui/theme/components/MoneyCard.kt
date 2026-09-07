package com.pesatrack.app.ui.theme.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.presentation.dashboard.DailySparklinePoint
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.FoodContainerDark
import com.pesatrack.app.ui.theme.FoodContentDark
import com.pesatrack.app.ui.theme.FuelContainerDark
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.LocalPesaTrackColors
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import java.time.LocalDate

private val AccentContainer = Color(0xFFFFF8E1)
private val AccentContent = Color(0xFFA67C00)

// Dark-mode container swaps -- reusing the category dark pairs of the same hue
// (amber/red/green) rather than declaring near-duplicate constants.

@Composable
fun MoneyCard(
    todaySpending: Double,
    yesterdaySpending: Double,
    sevenDayAverage: Double,
    sevenDaySparkline: List<DailySparklinePoint>,
    monthIncome: Double,
    remainingBudget: Double?,
    onTodaySpendingClick: () -> Unit,
    onSparklineDayClick: (LocalDate) -> Unit,
    onRemainingBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalPesaTrackColors.current.isDark

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        TodaySpendingRow(
            todaySpending = todaySpending,
            yesterdaySpending = yesterdaySpending,
            sevenDayAverage = sevenDayAverage,
            sparkline = sevenDaySparkline,
            onClick = onTodaySpendingClick,
            onDayClick = onSparklineDayClick
        )
        HorizontalDivider(color = Divider)
        MoneyRow(
            label = "Income this month",
            amountText = formatKsh(monthIncome),
            amountColor = Income,
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            iconContainer = if (isDark) FuelContainerDark else Color(0xFFE8F5E9),
            iconContent = LocalPesaTrackColors.current.income
        )
        HorizontalDivider(color = Divider)
        MoneyRow(
            label = "Remaining budget",
            amountText = remainingBudget?.let { formatKsh(it) } ?: "—",
            amountColor = Primary,
            icon = Icons.Outlined.AccountBalanceWallet,
            iconContainer = if (isDark) FoodContainerDark else AccentContainer,
            iconContent = if (isDark) FoodContentDark else AccentContent,
            onClick = onRemainingBudgetClick
        )
    }
}

@Composable
private fun TodaySpendingRow(
    todaySpending: Double,
    yesterdaySpending: Double,
    sevenDayAverage: Double,
    sparkline: List<DailySparklinePoint>,
    onClick: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Today's spending",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = formatKsh(todaySpending),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = "Yesterday ${formatKsh(yesterdaySpending)} · 7-day avg ${formatKsh(sevenDayAverage)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Sparkline(
            points = sparkline,
            onDayClick = onDayClick,
            modifier = Modifier.size(width = 64.dp, height = 40.dp)
        )
    }
}

// Bars are tappable individually -- consuming the tap inside this Canvas
// takes priority over the row's own clickable (see TodaySpendingRow), so a
// bar opens its own day while the rest of the row opens today.
@Composable
private fun Sparkline(
    points: List<DailySparklinePoint>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = Primary
    val today = LocalDate.now()

    Canvas(
        modifier = modifier.pointerInput(points) {
            detectTapGestures { offset ->
                if (points.isEmpty()) return@detectTapGestures
                val barWidth = size.width / points.size
                val index = (offset.x / barWidth).toInt().coerceIn(0, points.size - 1)
                onDayClick(points[index].date)
            }
        }
    ) {
        if (points.isEmpty()) return@Canvas

        val maxAmount = points.maxOf { it.amount }.coerceAtLeast(1.0)
        val barWidth = size.width / points.size
        val gap = (barWidth * 0.25f).coerceAtMost(2.dp.toPx())

        points.forEachIndexed { index, point ->
            val isToday = point.date == today
            val heightFraction = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f)
            // A zero-spend day still gets a visible, tappable sliver rather
            // than disappearing entirely -- sparse history shouldn't look
            // like a rendering bug or leave a gap nothing can tap.
            val barHeight = size.height * heightFraction.coerceAtLeast(0.12f)
            val left = index * barWidth + gap / 2
            drawRoundRect(
                color = if (isToday) barColor else barColor.copy(alpha = 0.35f),
                topLeft = Offset(left, size.height - barHeight),
                size = Size((barWidth - gap).coerceAtLeast(1f), barHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun MoneyRow(
    label: String,
    amountText: String,
    amountColor: Color,
    icon: ImageVector,
    iconContainer: Color,
    iconContent: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleLarge,
                color = amountColor
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconContent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
