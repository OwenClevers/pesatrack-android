package com.pesatrack.app.domain.model

data class Category(
    val id: Long,
    val name: String,
    val iconKey: String,
    val colorKey: String
) {
    companion object {
        fun unknown(id: Long): Category =
            Category(id = id, name = "Other", iconKey = "other", colorKey = "other")
    }
}
