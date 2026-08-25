package com.pesatrack.app.domain.model

enum class Category(val id: Long, val label: String) {
    FOOD(1, "Food"),
    FUEL(2, "Fuel"),
    SHOPPING(3, "Shopping"),
    UTILITIES(4, "Utilities"),
    ENTERTAINMENT(5, "Entertainment"),
    TRANSPORT(6, "Transport"),
    MEDICAL(7, "Medical"),
    EDUCATION(8, "Education"),
    OTHER(9, "Other");

    companion object {
        fun fromId(id: Long): Category = entries.firstOrNull { it.id == id } ?: OTHER
    }
}