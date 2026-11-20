package com.findme

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findme.ui.theme.AppFont
import com.findme.ui.theme.ThemeMode
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface FeedState {
    data object Loading : FeedState
    data object Empty : FeedState
    data object EmptySearch : FeedState
    data object Error : FeedState
    data class Content(
        val items: List<LostItem>,
        val isMetadataAvailable: Boolean = true
    ) : FeedState
}

sealed interface UserMessage {
    data class OpenChat(val itemId: String) : UserMessage
    data object ReportSaved : UserMessage
    data object ReportDeleted : UserMessage
    data object Notifications : UserMessage
    data object BlacklistOpened : UserMessage
    data object ProfileSaved : UserMessage
}

data class ProfileUiState(
    val fullName: String = "Алексей Смирнов",
    val nickname: String = "sherlock_mephi",
    val email: String = "a.smirnov@mephi.ru",
    val isEditing: Boolean = false,
    val draftName: String = "Алексей Смирнов",
    val draftNickname: String = "sherlock_mephi",
    val draftEmail: String = "a.smirnov@mephi.ru",
    val trustRating: String = "4.9",
    val trustPoints: Int = 145,
    val returnedCount: Int = 5,
    val lostReportsCount: Int = 2
)

data class SettingsUiState(
    val appFont: AppFont = AppFont.OPEN_SANS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val messageNotificationsEnabled: Boolean = true,
    val hideLastSeen: Boolean = false,
    val autoDeleteMessages: Boolean = true
)

data class MainUiState(
    val selectedRole: AppRole? = null,
    val activeItemType: String? = null,
    val activeItemId: String? = null,
    val profile: ProfileUiState = ProfileUiState(),
    val settings: SettingsUiState = SettingsUiState(),
    val searchQuery: String = "",
    val selectedCategory: IconCategory? = null,
    val isMyReportsTab: Boolean = false,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val feedState: FeedState = FeedState.Loading,
    val userMessage: UserMessage? = null
) {
    val itemCount: Int
        get() = (feedState as? FeedState.Content)?.items?.size ?: 0

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedCategory != null
}

private data class FilterParams(
    val role: AppRole?,
    val category: IconCategory?,
    val isMyReportsTab: Boolean,
    val sortOrder: SortOrder,
    val retryTick: Int
)

private data class ChromeState(
    val activeItemType: String? = null,
    val activeItemId: String? = null,
    val profile: ProfileUiState = ProfileUiState(),
    val settings: SettingsUiState = SettingsUiState(),
    val userMessage: UserMessage? = null
)

sealed interface MainAction {
    data class SelectRole(val role: AppRole) : MainAction
    data object NavigateBackToRoles : MainAction
    data class SetActiveItem(val typeSegment: String?, val itemId: String?) : MainAction
    data object OpenProfile : MainAction
    data object CloseProfile : MainAction
    data object StartEditProfile : MainAction
    data object CancelEditProfile : MainAction
    data object SaveProfile : MainAction
    data class UpdateDraftName(val name: String) : MainAction
    data class UpdateDraftNickname(val nickname: String) : MainAction
    data class UpdateDraftEmail(val email: String) : MainAction
    data object OpenSettings : MainAction
    data object CloseSettings : MainAction
    data class ChangeFont(val font: AppFont) : MainAction
    data class ChangeTheme(val themeMode: ThemeMode) : MainAction
    data class ToggleNotifications(val enabled: Boolean) : MainAction
    data class ToggleMessageNotifications(val enabled: Boolean) : MainAction
    data class ToggleHideLastSeen(val hide: Boolean) : MainAction
    data class ToggleAutoDeleteMessages(val enabled: Boolean) : MainAction
    data object OpenBlacklistClicked : MainAction
    data class SearchQueryChanged(val query: String) : MainAction
    data class SelectCategory(val category: IconCategory?) : MainAction
    data class SetMyReportsTab(val isMyReports: Boolean) : MainAction
    data class ChangeSortOrder(val sortOrder: SortOrder) : MainAction
    data object ResetFilters : MainAction
    data object RetryLoad : MainAction
    data class ContactUserClicked(val itemId: String) : MainAction
    data class DeleteItem(val itemId: String) : MainAction
    data object NotifyReportSaved : MainAction
    data object NotificationsClicked : MainAction
    data object UserMessageShown : MainAction
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_ROLE = "saved_role"
        private const val KEY_ACTIVE_TYPE = "saved_active_type"
        private const val KEY_ACTIVE_ID = "saved_active_id"
        private const val KEY_QUERY = "saved_search_query"
        private const val KEY_CATEGORY = "saved_category"
        private const val KEY_MY_REPORTS_TAB = "saved_my_reports_tab"
        private const val KEY_SORT = "saved_sort_order"
        private const val KEY_PROFILE_EDITING = "saved_profile_editing"
        private const val KEY_USER_NAME = "saved_user_name"
        private const val KEY_USER_NICK = "saved_user_nick"
        private const val KEY_USER_EMAIL = "saved_user_email"
        private const val KEY_DRAFT_NAME = "saved_draft_name"
        private const val KEY_DRAFT_NICK = "saved_draft_nick"
        private const val KEY_DRAFT_EMAIL = "saved_draft_email"
        private const val KEY_FONT = "saved_font"
        private const val KEY_THEME = "saved_theme"
        private const val KEY_NOTIF = "saved_notif"
        private const val KEY_MSG_NOTIF = "saved_msg_notif"
        private const val KEY_HIDE_SEEN = "saved_hide_seen"
        private const val KEY_AUTO_DELETE = "saved_auto_delete"
    }

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val roleFlow: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_ROLE, null)
    private val categoryFlow: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_CATEGORY, null)
    private val isMyReportsTabFlow: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_MY_REPORTS_TAB, false)
    private val sortOrderFlow: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SORT, SortOrder.NEWEST_FIRST.name)
    private val retryTrigger = MutableStateFlow(0)

    private val chromeState = MutableStateFlow(createInitialChromeState())

    private val debouncedQuery: Flow<String> = query
        .map { text -> text.trim() }
        .debounce(300.milliseconds)
        .distinctUntilChanged()

    private val filterParamsFlow: Flow<FilterParams> = combine(
        roleFlow,
        categoryFlow,
        isMyReportsTabFlow,
        sortOrderFlow,
        retryTrigger
    ) { roleName, categoryName, isMyTab, sortName, retryTick ->
        val role = roleName?.let { runCatching { AppRole.valueOf(it) }.getOrNull() }
        val category = categoryName?.let { runCatching { IconCategory.valueOf(it) }.getOrNull() }
        val sort = runCatching { SortOrder.valueOf(sortName) }.getOrDefault(SortOrder.NEWEST_FIRST)
        FilterParams(
            role = role,
            category = category,
            isMyReportsTab = isMyTab,
            sortOrder = sort,
            retryTick = retryTick
        )
    }

    private val searchRequestFlow: Flow<SearchRequest> = combine(
        debouncedQuery,
        filterParamsFlow,
        LostItemsRepository.allItems
    ) { normalizedQuery, params, _ ->
        SearchRequest(
            role = params.role ?: AppRole.LOST_SOMETHING,
            query = normalizedQuery,
            category = params.category,
            onlyMine = params.isMyReportsTab,
            sortOrder = params.sortOrder
        )
    }

    private val feedStateFlow: Flow<FeedState> = searchRequestFlow
        .flatMapLatest { request ->
            flow<FeedState> {
                emit(FeedState.Loading)
                val result = LostItemsRepository.searchItemsAsync(request)
                val nextState = when {
                    result.items.isNotEmpty() -> FeedState.Content(
                        items = result.items,
                        isMetadataAvailable = result.isMetadataAvailable
                    )
                    result.totalInSectionCount == 0 && request.query.isEmpty() && request.category == null -> {
                        FeedState.Empty
                    }
                    else -> FeedState.EmptySearch
                }
                emit(nextState)
            }.catch { error ->
                if (error is IOException) {
                    emit(FeedState.Error)
                } else {
                    throw error
                }
            }
        }

    val uiState: StateFlow<MainUiState> = combine(
        query,
        filterParamsFlow,
        feedStateFlow,
        chromeState
    ) { rawQuery, params, feed, chrome ->
        MainUiState(
            selectedRole = params.role,
            activeItemType = chrome.activeItemType,
            activeItemId = chrome.activeItemId,
            profile = chrome.profile,
            settings = chrome.settings,
            searchQuery = rawQuery,
            selectedCategory = params.category,
            isMyReportsTab = params.isMyReportsTab,
            sortOrder = params.sortOrder,
            feedState = feed,
            userMessage = chrome.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = createInitialUiState()
    )

    private fun createInitialChromeState(): ChromeState {
        val activeType = savedStateHandle.get<String>(KEY_ACTIVE_TYPE)
        val activeId = savedStateHandle.get<String>(KEY_ACTIVE_ID)

        val savedName = savedStateHandle.get<String>(KEY_USER_NAME) ?: "Алексей Смирнов"
        val savedNick = savedStateHandle.get<String>(KEY_USER_NICK) ?: "sherlock_mephi"
        val savedEmail = savedStateHandle.get<String>(KEY_USER_EMAIL) ?: "a.smirnov@mephi.ru"

        val profile = ProfileUiState(
            fullName = savedName,
            nickname = savedNick,
            email = savedEmail,
            isEditing = savedStateHandle.get<Boolean>(KEY_PROFILE_EDITING) ?: false,
            draftName = savedStateHandle.get<String>(KEY_DRAFT_NAME) ?: savedName,
            draftNickname = savedStateHandle.get<String>(KEY_DRAFT_NICK) ?: savedNick,
            draftEmail = savedStateHandle.get<String>(KEY_DRAFT_EMAIL) ?: savedEmail
        )

        val font = savedStateHandle.get<String>(KEY_FONT)?.let {
            runCatching { AppFont.valueOf(it) }.getOrNull()
        } ?: AppFont.OPEN_SANS

        val theme = savedStateHandle.get<String>(KEY_THEME)?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM

        val settings = SettingsUiState(
            appFont = font,
            themeMode = theme,
            notificationsEnabled = savedStateHandle.get<Boolean>(KEY_NOTIF) ?: true,
            messageNotificationsEnabled = savedStateHandle.get<Boolean>(KEY_MSG_NOTIF) ?: true,
            hideLastSeen = savedStateHandle.get<Boolean>(KEY_HIDE_SEEN) ?: false,
            autoDeleteMessages = savedStateHandle.get<Boolean>(KEY_AUTO_DELETE) ?: true
        )

        return ChromeState(
            activeItemType = activeType,
            activeItemId = activeId,
            profile = profile,
            settings = settings,
            userMessage = null
        )
    }

    private fun createInitialUiState(): MainUiState {
        val chrome = chromeState.value
        val role = roleFlow.value?.let { runCatching { AppRole.valueOf(it) }.getOrNull() }
        val category = categoryFlow.value?.let { runCatching { IconCategory.valueOf(it) }.getOrNull() }
        val sort = runCatching { SortOrder.valueOf(sortOrderFlow.value) }.getOrDefault(SortOrder.NEWEST_FIRST)

        return MainUiState(
            selectedRole = role,
            activeItemType = chrome.activeItemType,
            activeItemId = chrome.activeItemId,
            profile = chrome.profile,
            settings = chrome.settings,
            searchQuery = query.value,
            selectedCategory = category,
            isMyReportsTab = isMyReportsTabFlow.value,
            sortOrder = sort,
            feedState = FeedState.Loading,
            userMessage = chrome.userMessage
        )
    }

    fun syncRoleFromDeepLink(typeSegment: String?, itemId: String? = null) {
        if (typeSegment.isNullOrBlank()) return
        val inferredRole = if (typeSegment == ItemType.LOST.pathSegment) {
            AppRole.FOUND_SOMETHING
        } else {
            AppRole.LOST_SOMETHING
        }
        savedStateHandle[KEY_ROLE] = inferredRole.name
        savedStateHandle[KEY_ACTIVE_TYPE] = typeSegment
        savedStateHandle[KEY_ACTIVE_ID] = itemId
        savedStateHandle[KEY_MY_REPORTS_TAB] = false
        chromeState.update {
            it.copy(
                activeItemType = typeSegment,
                activeItemId = itemId
            )
        }
    }

    fun refreshFeed() {
        retryTrigger.update { it + 1 }
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SelectRole -> {
                savedStateHandle[KEY_ROLE] = action.role.name
                savedStateHandle[KEY_ACTIVE_TYPE] = null
                savedStateHandle[KEY_ACTIVE_ID] = null
                savedStateHandle[KEY_MY_REPORTS_TAB] = false
                chromeState.update {
                    it.copy(
                        activeItemType = null,
                        activeItemId = null
                    )
                }
            }
            MainAction.NavigateBackToRoles -> {
                savedStateHandle[KEY_ROLE] = null
                savedStateHandle[KEY_ACTIVE_TYPE] = null
                savedStateHandle[KEY_ACTIVE_ID] = null
                savedStateHandle[KEY_MY_REPORTS_TAB] = false
                chromeState.update {
                    it.copy(
                        activeItemType = null,
                        activeItemId = null
                    )
                }
            }
            is MainAction.SetActiveItem -> {
                savedStateHandle[KEY_ACTIVE_TYPE] = action.typeSegment
                savedStateHandle[KEY_ACTIVE_ID] = action.itemId
                chromeState.update {
                    it.copy(
                        activeItemType = action.typeSegment,
                        activeItemId = action.itemId
                    )
                }
            }
            MainAction.OpenProfile, MainAction.CloseProfile,
            MainAction.OpenSettings, MainAction.CloseSettings -> Unit
            MainAction.StartEditProfile -> {
                savedStateHandle[KEY_PROFILE_EDITING] = true
                chromeState.update { current ->
                    val p = current.profile
                    savedStateHandle[KEY_DRAFT_NAME] = p.fullName
                    savedStateHandle[KEY_DRAFT_NICK] = p.nickname
                    savedStateHandle[KEY_DRAFT_EMAIL] = p.email
                    current.copy(
                        profile = p.copy(
                            isEditing = true,
                            draftName = p.fullName,
                            draftNickname = p.nickname,
                            draftEmail = p.email
                        )
                    )
                }
            }
            MainAction.CancelEditProfile -> {
                savedStateHandle[KEY_PROFILE_EDITING] = false
                chromeState.update { current ->
                    val p = current.profile
                    current.copy(
                        profile = p.copy(
                            isEditing = false,
                            draftName = p.fullName,
                            draftNickname = p.nickname,
                            draftEmail = p.email
                        )
                    )
                }
            }
            MainAction.SaveProfile -> {
                val p = chromeState.value.profile
                val newName = p.draftName.trim().ifEmpty { p.fullName }
                val newNick = p.draftNickname.trim().removePrefix("@").ifEmpty { p.nickname }
                val newEmail = p.draftEmail.trim().ifEmpty { p.email }
                savedStateHandle[KEY_USER_NAME] = newName
                savedStateHandle[KEY_USER_NICK] = newNick
                savedStateHandle[KEY_USER_EMAIL] = newEmail
                savedStateHandle[KEY_PROFILE_EDITING] = false
                chromeState.update { current ->
                    current.copy(
                        profile = current.profile.copy(
                            fullName = newName,
                            nickname = newNick,
                            email = newEmail,
                            isEditing = false,
                            draftName = newName,
                            draftNickname = newNick,
                            draftEmail = newEmail
                        ),
                        userMessage = UserMessage.ProfileSaved
                    )
                }
            }
            is MainAction.UpdateDraftName -> {
                savedStateHandle[KEY_DRAFT_NAME] = action.name
                chromeState.update { current ->
                    current.copy(profile = current.profile.copy(draftName = action.name))
                }
            }
            is MainAction.UpdateDraftNickname -> {
                savedStateHandle[KEY_DRAFT_NICK] = action.nickname
                chromeState.update { current ->
                    current.copy(profile = current.profile.copy(draftNickname = action.nickname))
                }
            }
            is MainAction.UpdateDraftEmail -> {
                savedStateHandle[KEY_DRAFT_EMAIL] = action.email
                chromeState.update { current ->
                    current.copy(profile = current.profile.copy(draftEmail = action.email))
                }
            }
            is MainAction.ChangeFont -> {
                savedStateHandle[KEY_FONT] = action.font.name
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(appFont = action.font))
                }
            }
            is MainAction.ChangeTheme -> {
                savedStateHandle[KEY_THEME] = action.themeMode.name
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(themeMode = action.themeMode))
                }
            }
            is MainAction.ToggleNotifications -> {
                savedStateHandle[KEY_NOTIF] = action.enabled
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(notificationsEnabled = action.enabled))
                }
            }
            is MainAction.ToggleMessageNotifications -> {
                savedStateHandle[KEY_MSG_NOTIF] = action.enabled
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(messageNotificationsEnabled = action.enabled))
                }
            }
            is MainAction.ToggleHideLastSeen -> {
                savedStateHandle[KEY_HIDE_SEEN] = action.hide
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(hideLastSeen = action.hide))
                }
            }
            is MainAction.ToggleAutoDeleteMessages -> {
                savedStateHandle[KEY_AUTO_DELETE] = action.enabled
                chromeState.update { current ->
                    current.copy(settings = current.settings.copy(autoDeleteMessages = action.enabled))
                }
            }
            MainAction.OpenBlacklistClicked -> {
                chromeState.update { it.copy(userMessage = UserMessage.BlacklistOpened) }
            }
            is MainAction.SearchQueryChanged -> {
                savedStateHandle[KEY_QUERY] = action.query
            }
            is MainAction.SelectCategory -> {
                savedStateHandle[KEY_CATEGORY] = action.category?.name
            }
            is MainAction.SetMyReportsTab -> {
                savedStateHandle[KEY_MY_REPORTS_TAB] = action.isMyReports
            }
            is MainAction.ChangeSortOrder -> {
                savedStateHandle[KEY_SORT] = action.sortOrder.name
            }
            MainAction.ResetFilters -> {
                savedStateHandle[KEY_QUERY] = ""
                savedStateHandle[KEY_CATEGORY] = null
            }
            MainAction.RetryLoad -> {
                if (query.value.trim().equals("error", ignoreCase = true) ||
                    query.value.trim().equals("partial", ignoreCase = true)
                ) {
                    savedStateHandle[KEY_QUERY] = ""
                }
                retryTrigger.update { it + 1 }
            }
            is MainAction.ContactUserClicked -> {
                chromeState.update { it.copy(userMessage = UserMessage.OpenChat(action.itemId)) }
            }
            is MainAction.DeleteItem -> {
                val deleted = LostItemsRepository.deleteOwnItem(action.itemId)
                if (deleted) {
                    savedStateHandle[KEY_ACTIVE_TYPE] = null
                    savedStateHandle[KEY_ACTIVE_ID] = null
                    chromeState.update {
                        it.copy(
                            activeItemType = null,
                            activeItemId = null,
                            userMessage = UserMessage.ReportDeleted
                        )
                    }
                }
            }
            MainAction.NotifyReportSaved -> {
                savedStateHandle[KEY_MY_REPORTS_TAB] = true
                chromeState.update {
                    it.copy(userMessage = UserMessage.ReportSaved)
                }
            }
            MainAction.NotificationsClicked -> {
                chromeState.update { it.copy(userMessage = UserMessage.Notifications) }
            }
            MainAction.UserMessageShown -> {
                chromeState.update { it.copy(userMessage = null) }
            }
        }
    }
}