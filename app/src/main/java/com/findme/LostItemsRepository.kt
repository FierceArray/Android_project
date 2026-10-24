package com.findme

class LostItemsRepository {
    private val foundItems = listOf(
        LostItem(
            id = "found_1",
            type = ItemType.FOUND,
            category = "Найдена электроника",
            description = "Смартфон (детали скрыты слепым поиском). Подтвердите модель и приметы в чате.",
            location = "Район ст. м. Бауманская",
            time = "Сегодня в 14:00",
            timestampOrder = 1,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "found_2",
            type = ItemType.FOUND,
            category = "Найден питомец",
            description = "Собака в ошейнике без адресника. Очень напугана, ждёт хозяина.",
            location = "Район ст. м. Сокольники",
            time = "Сегодня в 12:30",
            timestampOrder = 2,
            iconType = IconCategory.PET
        ),
        LostItem(
            id = "found_3",
            type = ItemType.FOUND,
            category = "Найдены документы",
            description = "Папка с документами формата А4 (ФИО скрыто системой безопасности).",
            location = "Район ст. м. Курская",
            time = "Сегодня в 11:15",
            timestampOrder = 3,
            iconType = IconCategory.DOCUMENT
        ),
        LostItem(
            id = "found_4",
            type = ItemType.FOUND,
            category = "Найдена электроника",
            description = "Беспроводные наушники в кейсе. Найдены на скамейке в парке.",
            location = "Район ст. м. Китай-город",
            time = "Вчера в 19:40",
            timestampOrder = 4,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "found_5",
            type = ItemType.FOUND,
            category = "Найдены вещи",
            description = "Связка ключей с брелоком (точное число ключей скрыто).",
            location = "Район ВДНХ",
            time = "Вчера в 16:00",
            timestampOrder = 5,
            iconType = IconCategory.OTHER
        ),
        LostItem(
            id = "found_6",
            type = ItemType.FOUND,
            category = "Найден питомец",
            description = "Рыжий пушистый кот. Сидел в подъезде, забрали на передержку.",
            location = "Район ст. м. Чистые пруды",
            time = "Позавчера в 09:15",
            timestampOrder = 6,
            iconType = IconCategory.PET
        )
    )
    private val lostItems = listOf(
        LostItem(
            id = "lost_1",
            type = ItemType.LOST,
            category = "Потеряна электроника",
            description = "Ищу серый смартфон в темном чехле. На экране небольшая царапина снизу.",
            location = "Район ст. м. Каширская (МИФИ)",
            time = "Сегодня в 15:20",
            timestampOrder = 1,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "lost_2",
            type = ItemType.LOST,
            category = "Потерян питомец",
            description = "Убежал рыжий кот в зеленом ошейнике, откликается на кличку Персик.",
            location = "Район ст. м. Чистые пруды",
            time = "Сегодня в 09:15",
            timestampOrder = 2,
            iconType = IconCategory.PET
        ),
        LostItem(
            id = "lost_3",
            type = ItemType.LOST,
            category = "Потеряны документы",
            description = "Утерян студенческий билет и пропуск в синей обложке.",
            location = "Район ст. м. Университет",
            time = "Вчера в 18:00",
            timestampOrder = 3,
            iconType = IconCategory.DOCUMENT
        ),
        LostItem(
            id = "lost_4",
            type = ItemType.LOST,
            category = "Потеряна электроника",
            description = "Забыт серебристый ноутбук в тканевом чехле на фудкорте.",
            location = "ТЦ Атриум (Курская)",
            time = "Вчера в 20:00",
            timestampOrder = 4,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "lost_5",
            type = ItemType.LOST,
            category = "Потеряны вещи",
            description = "Оставлен черный городской рюкзак со спортивной формой.",
            location = "Парк Горького",
            time = "Позавчера в 14:30",
            timestampOrder = 5,
            iconType = IconCategory.OTHER
        )
    )
    fun getItemsForRole(
        role: AppRole,
        query: String,
        category: IconCategory?,
        sortOrder: SortOrder
    ): List<LostItem> {
        val sourceList = if (role == AppRole.LOST_SOMETHING) foundItems else lostItems
        val trimmedQuery = query.trim()
        val filtered = sourceList.filter { item ->
            val matchesCategory = category == null || item.iconType == category
            val matchesQuery = trimmedQuery.isEmpty() ||
                    item.category.contains(trimmedQuery, ignoreCase = true) ||
                    item.description.contains(trimmedQuery, ignoreCase = true) ||
                    item.location.contains(trimmedQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        return when (sortOrder) {
            SortOrder.NEWEST_FIRST -> filtered.sortedBy { it.timestampOrder }
            SortOrder.OLDEST_FIRST -> filtered.sortedByDescending { it.timestampOrder }
        }
    }
    fun getItemByLink(typeSegment: String?, itemId: String?): LostItem? {
        if (itemId.isNullOrBlank()) return null
        return when (typeSegment) {
            ItemType.FOUND.pathSegment -> foundItems.find { it.id == itemId }
            ItemType.LOST.pathSegment -> lostItems.find { it.id == itemId }
            else -> (foundItems + lostItems).find { it.id == itemId }
        }
    }
}