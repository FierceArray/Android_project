package com.findme

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RoleSelectionRoute : NavKey

@Serializable
data object FeedListRoute : NavKey

@Serializable
data class ItemDetailRoute(
    val typeSegment: String,
    val itemId: String
) : NavKey

@Serializable
data class EditItemRoute(
    val itemId: String? = null,
    val defaultTypeSegment: String = ItemType.LOST.pathSegment
) : NavKey

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

fun NavBackStack<NavKey>.selectItemInPane(typeSegment: String, itemId: String) {
    while (size > 1 && (last() is ItemDetailRoute || last() is EditItemRoute)) {
        removeLastOrNull()
    }
    add(ItemDetailRoute(typeSegment = typeSegment, itemId = itemId))
}