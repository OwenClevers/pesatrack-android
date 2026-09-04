package com.pesatrack.app.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val defaultMonthFormat = DateTimeFormatter.ofPattern("MMMM yyyy")

// A tappable "<Month Year> v" label that opens a dropdown of the last 12 months.
// Used on Dashboard, Reports and Budgets so month-scoped figures aren't locked
// to the current month.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    month: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White.copy(alpha = 0.88f),
    formatter: DateTimeFormatter = defaultMonthFormat
) {
    var expanded by remember { mutableStateOf(false) }
    val months = remember { (0..11).map { YearMonth.now().minusMonths(it.toLong()) } }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = month.format(formatter),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.format(formatter)) },
                    onClick = {
                        onMonthSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
