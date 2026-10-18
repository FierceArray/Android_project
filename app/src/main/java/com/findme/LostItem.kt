package com.findme

data class LostItem(
    val id: String,
    val category: String,
    val description: String,
    val location: String,
    val time: String,
    val iconType: IconCategory
)

enum class IconCategory {
    ELECTRONICS, PET, DOCUMENT, OTHER
}