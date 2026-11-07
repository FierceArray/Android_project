package com.findme

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

enum class PhotoAttachment {
    NONE, PHONE_PHOTO, PET_PHOTO, DOCS_PHOTO, KEYS_PHOTO
}

data class LostItem(
    val id: String,
    val type: ItemType,
    val category: String,
    val description: String,
    val location: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long? = null,
    val changeNote: String? = null,
    val isMine: Boolean = false,
    val photoAttachment: PhotoAttachment = PhotoAttachment.NONE,
    val iconType: IconCategory
) {
    val deepLink: String
        get() = "findme://app/${type.pathSegment}/$id"

    val formattedCreatedTime: String
        get() = formatMillisToUserTime(createdAtMillis)

    val formattedUpdatedTime: String?
        get() = updatedAtMillis?.let { formatMillisToUserTime(it) }
}

fun formatMillisToUserTime(millis: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}