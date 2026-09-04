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

private const val DEFAULT_KEY = "other"

// Icon lookup, keyed by iconKey. Independent of color -- a category's icon and
// its color are picked separately, so this only ever decides the glyph.
private val iconByKey: Map<String, ImageVector> = mapOf(
    "food" to Icons.Filled.Restaurant,
    "fuel" to Icons.Filled.LocalGasStation,
    "shopping" to Icons.Filled.ShoppingBag,
    "utilities" to Icons.Filled.Bolt,
    "entertainment" to Icons.Filled.Movie,
    "transport" to Icons.Filled.DirectionsBus,
    "medical" to Icons.Filled.LocalHospital,
    "education" to Icons.Filled.School,
    DEFAULT_KEY to Icons.Filled.Description
)

// Keys in a fixed display order for icon/color pickers.
val CategoryIconKeys: List<String> = listOf(
    "food", "fuel", "shopping", "utilities", "entertainment", "transport", "medical", "education", DEFAULT_KEY
)

fun categoryIcon(iconKey: String): ImageVector =
    iconByKey[iconKey] ?: iconByKey.getValue(DEFAULT_KEY)

private data class ColorTint(val container: Color, val content: Color, val containerDark: Color, val contentDark: Color)

// Color lookup, keyed by colorKey -- the same tint pairs used since the original
// hardcoded per-category mapping, just addressable on their own now instead of
// being tied 1:1 to iconKey. Seeded categories set colorKey == iconKey, which is
// exactly the pairing this used to hardcode, so their appearance is unchanged.
private val colorByKey: Map<String, ColorTint> = mapOf(
    "food" to ColorTint(FoodContainer, FoodContent, FoodContainerDark, FoodContentDark),
    "fuel" to ColorTint(FuelContainer, FuelContent, FuelContainerDark, FuelContentDark),
    "shopping" to ColorTint(ShoppingContainer, ShoppingContent, ShoppingContainerDark, ShoppingContentDark),
    "utilities" to ColorTint(UtilitiesContainer, UtilitiesContent, UtilitiesContainerDark, UtilitiesContentDark),
    "entertainment" to ColorTint(EntertainmentContainer, EntertainmentContent, EntertainmentContainerDark, EntertainmentContentDark),
    "transport" to ColorTint(TransportContainer, TransportContent, TransportContainerDark, TransportContentDark),
    "medical" to ColorTint(MedicalContainer, MedicalContent, MedicalContainerDark, MedicalContentDark),
    "education" to ColorTint(EducationContainer, EducationContent, EducationContainerDark, EducationContentDark),
    DEFAULT_KEY to ColorTint(OtherContainer, OtherContent, OtherContainerDark, OtherContentDark)
)

val CategoryColorKeys: List<String> = CategoryIconKeys

// Representative swatch for a color picker -- the saturated "content" shade reads
// clearly as a color chip regardless of theme.
fun categoryColorSwatch(colorKey: String): Color =
    (colorByKey[colorKey] ?: colorByKey.getValue(DEFAULT_KEY)).content

// Keyed by iconKey/colorKey (stable) rather than identity, since Category is
// Room-backed, not an enum. @Composable so the container/content pair can switch
// with dark mode.
@Composable
fun Category.visual(): CategoryVisual {
    val isDark = LocalPesaTrackColors.current.isDark
    val icon = categoryIcon(iconKey)
    val tint = colorByKey[colorKey] ?: colorByKey.getValue(DEFAULT_KEY)
    val container = if (isDark) tint.containerDark else tint.container
    val content = if (isDark) tint.contentDark else tint.content
    return CategoryVisual(icon, container, content)
}
