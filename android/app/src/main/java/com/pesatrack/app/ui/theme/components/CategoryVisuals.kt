package com.pesatrack.app.ui.theme.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.ui.theme.*

data class CategoryVisual(
    val icon: ImageVector,
    val container: Color,
    val content: Color
)

// Keyed by iconKey (stable) rather than identity, since Category is Room-backed, not
// an enum. @Composable so the container/content pair can switch with dark mode.
@Composable
fun Category.visual(): CategoryVisual {
    val isDark = LocalPesaTrackColors.current.isDark
    val (icon, container, content) = when (iconKey) {
        "food" -> Triple(Icons.Filled.Restaurant, if (isDark) FoodContainerDark else FoodContainer, if (isDark) FoodContentDark else FoodContent)
        "fuel" -> Triple(Icons.Filled.LocalGasStation, if (isDark) FuelContainerDark else FuelContainer, if (isDark) FuelContentDark else FuelContent)
        "shopping" -> Triple(Icons.Filled.ShoppingBag, if (isDark) ShoppingContainerDark else ShoppingContainer, if (isDark) ShoppingContentDark else ShoppingContent)
        "utilities" -> Triple(Icons.Filled.Bolt, if (isDark) UtilitiesContainerDark else UtilitiesContainer, if (isDark) UtilitiesContentDark else UtilitiesContent)
        "entertainment" -> Triple(Icons.Filled.Movie, if (isDark) EntertainmentContainerDark else EntertainmentContainer, if (isDark) EntertainmentContentDark else EntertainmentContent)
        "transport" -> Triple(Icons.Filled.DirectionsBus, if (isDark) TransportContainerDark else TransportContainer, if (isDark) TransportContentDark else TransportContent)
        "medical" -> Triple(Icons.Filled.LocalHospital, if (isDark) MedicalContainerDark else MedicalContainer, if (isDark) MedicalContentDark else MedicalContent)
        "education" -> Triple(Icons.Filled.School, if (isDark) EducationContainerDark else EducationContainer, if (isDark) EducationContentDark else EducationContent)
        else -> Triple(Icons.Filled.Description, if (isDark) OtherContainerDark else OtherContainer, if (isDark) OtherContentDark else OtherContent)
    }
    return CategoryVisual(icon, container, content)
}
