package com.findme

enum class ItemType(val pathSegment: String) {
    LOST("lost"),
    FOUND("found")
}

enum class IconCategory {
    ELECTRONICS, PET, DOCUMENT, OTHER
}

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

data class LostItem(
    val id: String,
    val type: ItemType,
    val category: String,
    val description: String,
    val location: String,
    val time: String,
    val timestampOrder: Int,
    val iconType: IconCategory
) {
    val deepLink: String
        get() = "findme://app/${type.pathSegment}/$id"
}