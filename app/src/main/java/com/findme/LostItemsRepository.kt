package com.findme

import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class SearchRequest(
    val role: AppRole,
    val query: String,
    val category: IconCategory?,
    val onlyMine: Boolean,
    val sortOrder: SortOrder
)

data class SearchResultData(
    val items: List<LostItem>,
    val totalInSectionCount: Int,
    val isMetadataAvailable: Boolean
)

object LostItemsRepository {
    private val baseTimeMillis = System.currentTimeMillis()
    private val mutex = Mutex()

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val verifiedReportIds = setOf("found_1", "found_3", "found_5", "lost_1", "lost_2")

    private val initialItems = listOf(
        LostItem(
            id = "found_1",
            type = ItemType.FOUND,
            category = "Найдена электроника",
            description = "Смартфон (детали скрыты слепым поиском). Подтвердите модель и приметы в чате.",
            location = "Район ст. м. Бауманская",
            createdAtMillis = baseTimeMillis - 15 * 60 * 1000L - 342L,
            isMine = false,
            photoAttachment = PhotoAttachment.PHONE_PHOTO,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "found_2",
            type = ItemType.FOUND,
            category = "Найден питомец",
            description = "Собака в ошейнике без адресника. Очень напугана, ждёт хозяина.",
            location = "Район ст. м. Сокольники",
            createdAtMillis = baseTimeMillis - 2 * 3600 * 1000L - 819L,
            isMine = false,
            photoAttachment = PhotoAttachment.PET_PHOTO,
            iconType = IconCategory.PET
        ),
        LostItem(
            id = "found_3",
            type = ItemType.FOUND,
            category = "Найдены документы",
            description = "Папка с документами формата А4 (ФИО скрыто системой безопасности).",
            location = "Район ст. м. Курская",
            createdAtMillis = baseTimeMillis - 4 * 3600 * 1000L - 124L,
            isMine = false,
            photoAttachment = PhotoAttachment.DOCS_PHOTO,
            iconType = IconCategory.DOCUMENT
        ),
        LostItem(
            id = "found_4",
            type = ItemType.FOUND,
            category = "Найдена электроника",
            description = "Беспроводные наушники в кейсе. Найдены на скамейке в парке.",
            location = "Район ст. м. Китай-город",
            createdAtMillis = baseTimeMillis - 20 * 3600 * 1000L - 551L,
            isMine = false,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "found_5",
            type = ItemType.FOUND,
            category = "Найдены вещи",
            description = "Связка ключей с брелоком (точное число ключей скрыто).",
            location = "Район ВДНХ",
            createdAtMillis = baseTimeMillis - 26 * 3600 * 1000L - 908L,
            isMine = true,
            photoAttachment = PhotoAttachment.KEYS_PHOTO,
            iconType = IconCategory.OTHER
        ),
        LostItem(
            id = "lost_1",
            type = ItemType.LOST,
            category = "Потеряна электроника",
            description = "Ищу серый смартфон в темном чехле. На экране небольшая царапина снизу.",
            location = "Район ст. м. Каширская (МИФИ)",
            createdAtMillis = baseTimeMillis - 10 * 60 * 1000L - 117L,
            updatedAtMillis = baseTimeMillis - 5 * 60 * 1000L - 409L,
            changeNote = "Добавил примету про царапину на экране и фото",
            isMine = true,
            photoAttachment = PhotoAttachment.PHONE_PHOTO,
            iconType = IconCategory.ELECTRONICS
        ),
        LostItem(
            id = "lost_2",
            type = ItemType.LOST,
            category = "Потерян питомец",
            description = "Убежал рыжий кот в зеленом ошейнике, откликается на кличку Персик.",
            location = "Район ст. м. Чистые пруды",
            createdAtMillis = baseTimeMillis - 5 * 3600 * 1000L - 612L,
            isMine = false,
            photoAttachment = PhotoAttachment.PET_PHOTO,
            iconType = IconCategory.PET
        ),
        LostItem(
            id = "lost_3",
            type = ItemType.LOST,
            category = "Потеряны документы",
            description = "Утерян студенческий билет и пропуск в синей обложке.",
            location = "Район ст. м. Университет",
            createdAtMillis = baseTimeMillis - 22 * 3600 * 1000L - 205L,
            isMine = false,
            photoAttachment = PhotoAttachment.DOCS_PHOTO,
            iconType = IconCategory.DOCUMENT
        ),
        LostItem(
            id = "lost_4",
            type = ItemType.LOST,
            category = "Потеряны вещи",
            description = "Оставлен черный городской рюкзак со спортивной формой.",
            location = "Парк Горького",
            createdAtMillis = baseTimeMillis - 48 * 3600 * 1000L - 773L,
            isMine = false,
            photoAttachment = PhotoAttachment.KEYS_PHOTO,
            iconType = IconCategory.OTHER
        )
    )

    private val _allItems = MutableStateFlow(initialItems)
    val allItems: StateFlow<List<LostItem>> = _allItems.asStateFlow()

    suspend fun searchItemsAsync(request: SearchRequest): SearchResultData = supervisorScope {
        val rawItemsDeferred = async(ioDispatcher) {
            loadSectionItemsFromSource(request)
        }
        val verifiedIdsDeferred = async(ioDispatcher) {
            loadVerifiedIdsFromSource(request.query)
        }

        val sectionItems = rawItemsDeferred.await()

        val verifiedIds: Set<String>? = try {
            verifiedIdsDeferred.await()
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            null
        }

        withContext(cpuDispatcher) {
            val trimmedQuery = request.query.trim()
            val queryForFilter = if (trimmedQuery.equals("partial", ignoreCase = true)) "" else trimmedQuery
            val filtered = mutableListOf<LostItem>()

            for (item in sectionItems) {
                ensureActive()
                val matchesCategory = request.category == null || item.iconType == request.category
                val matchesQuery = queryForFilter.isEmpty() ||
                        item.category.contains(queryForFilter, ignoreCase = true) ||
                        item.description.contains(queryForFilter, ignoreCase = true) ||
                        item.location.contains(queryForFilter, ignoreCase = true)

                if (matchesCategory && matchesQuery) {
                    val isItemVerified = verifiedIds?.contains(item.id) == true || item.isMine
                    filtered.add(item.copy(isVerified = isItemVerified))
                }
            }

            val sorted = when (request.sortOrder) {
                SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.createdAtMillis }
                SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.createdAtMillis }
            }

            SearchResultData(
                items = sorted,
                totalInSectionCount = sectionItems.size,
                isMetadataAvailable = verifiedIds != null
            )
        }
    }

    private suspend fun loadSectionItemsFromSource(request: SearchRequest): List<LostItem> {
        delay(300.milliseconds)
        val trimmed = request.query.trim()
        if (trimmed.equals("error", ignoreCase = true)) {
            throw IOException("Simulated primary data source network error")
        }
        if (trimmed.equals("loading", ignoreCase = true)) {
            delay(60_000.milliseconds)
        }
        val snapshot = mutex.withLock { _allItems.value }
        val targetType = if (request.role == AppRole.LOST_SOMETHING) ItemType.FOUND else ItemType.LOST
        return snapshot.filter { item ->
            if (request.onlyMine) {
                item.isMine
            } else {
                !item.isMine && item.type == targetType
            }
        }
    }

    private suspend fun loadVerifiedIdsFromSource(query: String): Set<String> {
        delay(200.milliseconds)
        if (query.trim().equals("partial", ignoreCase = true)) {
            throw IOException("Simulated verification metadata service error")
        }
        return mutex.withLock { verifiedReportIds.toSet() }
    }

    fun getItemByLink(typeSegment: String?, itemId: String?): LostItem? {
        if (itemId.isNullOrBlank()) return null
        val list = _allItems.value
        val found = when (typeSegment) {
            ItemType.FOUND.pathSegment -> list.find { it.type == ItemType.FOUND && it.id == itemId }
            ItemType.LOST.pathSegment -> list.find { it.type == ItemType.LOST && it.id == itemId }
            else -> list.find { it.id == itemId }
        }
        return found?.copy(isVerified = verifiedReportIds.contains(found.id) || found.isMine)
    }

    fun getItemById(itemId: String?): LostItem? {
        if (itemId.isNullOrBlank()) return null
        val found = _allItems.value.find { it.id == itemId }
        return found?.copy(isVerified = verifiedReportIds.contains(found.id) || found.isMine)
    }

    fun saveItem(
        existingId: String?,
        type: ItemType,
        categoryTitle: String,
        description: String,
        location: String,
        iconType: IconCategory,
        photoAttachment: PhotoAttachment,
        changeNote: String
    ): LostItem {
        val nowMillis = System.currentTimeMillis()
        val existing = existingId?.let { getItemById(it) }

        val savedItem = if (existing != null) {
            existing.copy(
                type = type,
                category = categoryTitle.trim(),
                description = description.trim(),
                location = location.trim(),
                iconType = iconType,
                photoAttachment = photoAttachment,
                updatedAtMillis = nowMillis,
                changeNote = changeNote.trim().ifEmpty { null },
                isVerified = true
            )
        } else {
            val newId = "${type.pathSegment}_${nowMillis % 100000}"
            LostItem(
                id = newId,
                type = type,
                category = categoryTitle.trim(),
                description = description.trim(),
                location = location.trim(),
                createdAtMillis = nowMillis,
                updatedAtMillis = null,
                changeNote = null,
                isMine = true,
                isVerified = true,
                photoAttachment = photoAttachment,
                iconType = iconType
            )
        }

        _allItems.update { currentList ->
            if (existing != null) {
                currentList.map { if (it.id == existing.id) savedItem else it }
            } else {
                listOf(savedItem) + currentList
            }
        }
        return savedItem
    }

    fun deleteOwnItem(itemId: String): Boolean {
        val item = getItemById(itemId) ?: return false
        if (!item.isMine) return false
        _allItems.update { list -> list.filterNot { it.id == itemId } }
        return true
    }
}