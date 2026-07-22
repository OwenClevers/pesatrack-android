package com.pesatrack.app.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Good Evening 👋",
            style = MaterialTheme.typography.headlineMedium
        )

        Card {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    "Today's Spending",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "KSh 0.00",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}