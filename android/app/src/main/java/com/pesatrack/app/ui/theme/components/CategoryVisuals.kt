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

fun Category.visual(): CategoryVisual = when (this) {
    Category.FOOD -> CategoryVisual(Icons.Filled.Restaurant, FoodContainer, FoodContent)
    Category.FUEL -> CategoryVisual(Icons.Filled.LocalGasStation, FuelContainer, FuelContent)
    Category.SHOPPING -> CategoryVisual(Icons.Filled.ShoppingBag, ShoppingContainer, ShoppingContent)
    Category.UTILITIES -> CategoryVisual(Icons.Filled.Bolt, UtilitiesContainer, UtilitiesContent)
    Category.ENTERTAINMENT -> CategoryVisual(Icons.Filled.Movie, EntertainmentContainer, EntertainmentContent)
    Category.TRANSPORT -> CategoryVisual(Icons.Filled.DirectionsBus, TransportContainer, TransportContent)
    Category.MEDICAL -> CategoryVisual(Icons.Filled.LocalHospital, MedicalContainer, MedicalContent)
    Category.EDUCATION -> CategoryVisual(Icons.Filled.School, EducationContainer, EducationContent)
    Category.OTHER -> CategoryVisual(Icons.Filled.Description, OtherContainer, OtherContent)
}