package com.pesatrack.app.ui.theme.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.LocalPesaTrackColors
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary

private val AccentContainer = Color(0xFFFFF8E1)
private val AccentContent = Color(0xFFA67C00)
private val ExpenseContainer = Color(0xFFFDECEA)
private val IncomeContainer = Color(0xFFE8F5E9)

@Composable
fun MoneyCard(
    todaySpending: Double,
    monthIncome: Double,
    remainingBudget: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        MoneyRow(
            label = "Today's spending",
            amountText = formatKsh(todaySpending),
            amountColor = TextPrimary,
            icon = Icons.Outlined.ShoppingCart,
            iconContainer = ExpenseContainer,
            iconContent = LocalPesaTrackColors.current.expense
        )
        HorizontalDivider(color = Divider)
        MoneyRow(
            label = "Income this month",
            amountText = formatKsh(monthIncome),
            amountColor = Income,
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            iconContainer = IncomeContainer,
            iconContent = LocalPesaTrackColors.current.income
        )
        HorizontalDivider(color = Divider)
        MoneyRow(
            label = "Remaining budget",
            amountText = remainingBudget?.let { formatKsh(it) } ?: "—",
            amountColor = Primary,
            icon = Icons.Outlined.AccountBalanceWallet,
            iconContainer = AccentContainer,
            iconContent = AccentContent
        )
    }
}

@Composable
private fun MoneyRow(
    label: String,
    amountText: String,
    amountColor: Color,
    icon: ImageVector,
    iconContainer: Color,
    iconContent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
