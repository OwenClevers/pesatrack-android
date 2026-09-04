package com.pesatrack.app.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.Surface as SurfaceColor
import com.pesatrack.app.ui.theme.TextSecondary

private val FabSize = 56.dp

private fun NavController.navigateToTab(route: String) {
    // Anchored on Dashboard, not graph.findStartDestination(): Splash pops itself
    // with inclusive = true after launch, so it's never in the back stack to pop to.
    navigate(route) {
        popUpTo(Screen.Dashboard.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PesaBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            HorizontalDivider(color = Divider)
            Surface(color = SurfaceColor) {
                // navigationBarsPadding() before height() so the inset adds to the
                // bar's total height (background bleeds behind the gesture/button
                // nav bar) instead of shrinking the 64dp content area into it.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomBarItem(
                        icon = Icons.Outlined.Home,
                        label = "Home",
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = { navController.navigateToTab(Screen.Dashboard.route) }
                    )
                    BottomBarItem(
                        icon = Icons.AutoMirrored.Outlined.List,
                        label = "Transactions",
                        selected = currentRoute == Screen.Transactions.route,
                        onClick = { navController.navigateToTab(Screen.Transactions.route) }
                    )

                    Box(modifier = Modifier.width(FabSize))

                    BottomBarItem(
                        icon = Icons.Outlined.BarChart,
                        label = "Reports",
                        selected = currentRoute == Screen.Reports.route,
                        onClick = { navController.navigateToTab(Screen.Reports.route) }
                    )
                    BottomBarItem(
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigateToTab(Screen.Settings.route) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Screen.AddTransaction.route) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -(FabSize / 2))
                .size(FabSize),
            shape = CircleShape,
            containerColor = Primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add transaction")
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) Primary else TextSecondary

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
