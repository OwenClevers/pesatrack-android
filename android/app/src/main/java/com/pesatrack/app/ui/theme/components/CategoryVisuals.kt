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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.ui.theme.*

data class CategoryVisual(
    val icon: ImageVector,
    val container: Color,
    val content: Color
)

// Keyed by iconKey (stable) rather than identity, since Category is Room-backed, not an enum.
fun Category.visual(): CategoryVisual = when (iconKey) {
    "food" -> CategoryVisual(Icons.Filled.Restaurant, FoodContainer, FoodContent)
    "fuel" -> CategoryVisual(Icons.Filled.LocalGasStation, FuelContainer, FuelContent)
    "shopping" -> CategoryVisual(Icons.Filled.ShoppingBag, ShoppingContainer, ShoppingContent)
    "utilities" -> CategoryVisual(Icons.Filled.Bolt, UtilitiesContainer, UtilitiesContent)
    "entertainment" -> CategoryVisual(Icons.Filled.Movie, EntertainmentContainer, EntertainmentContent)
    "transport" -> CategoryVisual(Icons.Filled.DirectionsBus, TransportContainer, TransportContent)
    "medical" -> CategoryVisual(Icons.Filled.LocalHospital, MedicalContainer, MedicalContent)
    "education" -> CategoryVisual(Icons.Filled.School, EducationContainer, EducationContent)
    else -> CategoryVisual(Icons.Filled.Description, OtherContainer, OtherContent)
}
