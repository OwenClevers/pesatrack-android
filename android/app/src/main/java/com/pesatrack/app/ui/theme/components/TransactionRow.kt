package com.pesatrack.app.ui.theme.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(
    transaction: Transaction,
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false
) {
    val visual = category.visual()
    val isIncome = transaction.type == TransactionType.INCOME

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            Checkbox(
                checked = selected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            Spacer(Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(visual.container),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = category.name,
                tint = visual.content,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = transaction.merchant ?: category.name,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = transaction.transactionDate.format(dateFormat) +
                        " · " + transaction.transactionDate.format(timeFormat),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Text(
            text = (if (isIncome) "+ " else "- ") + formatKsh(transaction.amount),
            style = MaterialTheme.typography.labelLarge,
            color = if (isIncome) Income else Expense
        )
    }
}
