package com.findme

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.findme.ui.theme.AppFont
import com.findme.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface FeedState {
    data object Loading : FeedState
    data object Empty : FeedState
    data object Error : FeedState
    data class Content(val items: List<LostItem>) : FeedState
}
sealed interface SelectedItemState {
    data object None : SelectedItemState
    data class Found(val item: LostItem) : SelectedItemState
    data class NotFound(val invalidId: String) : SelectedItemState
}
sealed interface UserMessage {
    data class OpenChat(val itemId: String) : UserMessage
    data object CreateReport : UserMessage
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
    val hideLastSeen: Boolean = false,
    val autoDeleteMessages: Boolean = true
)
data class MainUiState(
    val selectedRole: AppRole? = null,
    val isProfileOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val profile: ProfileUiState = ProfileUiState(),
    val settings: SettingsUiState = SettingsUiState(),
    val searchQuery: String = "",
    val selectedCategory: IconCategory? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val feedState: FeedState = FeedState.Loading,
    val selectedItemState: SelectedItemState = SelectedItemState.None,
    val userMessage: UserMessage? = null
) {
    val itemCount: Int
        get() = (feedState as? FeedState.Content)?.items?.size ?: 0

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedCategory != null
}
sealed interface MainAction {
    data class SelectRole(val role: AppRole) : MainAction
    data object NavigateBackToRoles : MainAction
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
    data class ToggleHideLastSeen(val hide: Boolean) : MainAction
    data class ToggleAutoDeleteMessages(val enabled: Boolean) : MainAction
    data object OpenBlacklistClicked : MainAction
    data class SearchQueryChanged(val query: String) : MainAction
    data class SelectCategory(val category: IconCategory?) : MainAction
    data class ChangeSortOrder(val sortOrder: SortOrder) : MainAction
    data object ResetFilters : MainAction
    data object RetryLoad : MainAction
    data class OpenItem(val typeSegment: String, val itemId: String) : MainAction
    data object CloseItemDetails : MainAction
    data class ContactUserClicked(val itemId: String) : MainAction
    data object CreateReportClicked : MainAction
    data object NotificationsClicked : MainAction
    data object UserMessageShown : MainAction
}

class MainViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repository = LostItemsRepository()
    companion object {
        private const val KEY_ROLE = "saved_role"
        private const val KEY_QUERY = "saved_search_query"
        private const val KEY_CATEGORY = "saved_category"
        private const val KEY_SORT = "saved_sort_order"
        private const val KEY_ITEM_TYPE = "saved_item_type"
        private const val KEY_ITEM_ID = "saved_item_id"
        private const val KEY_PROFILE_OPEN = "saved_profile_open"
        private const val KEY_PROFILE_EDITING = "saved_profile_editing"
        private const val KEY_SETTINGS_OPEN = "saved_settings_open"
        private const val KEY_USER_NAME = "saved_user_name"
        private const val KEY_USER_NICK = "saved_user_nick"
        private const val KEY_USER_EMAIL = "saved_user_email"
        private const val KEY_DRAFT_NAME = "saved_draft_name"
        private const val KEY_DRAFT_NICK = "saved_draft_nick"
        private const val KEY_DRAFT_EMAIL = "saved_draft_email"
        private const val KEY_FONT = "saved_font"
        private const val KEY_THEME = "saved_theme"
        private const val KEY_NOTIF = "saved_notif"
        private const val KEY_HIDE_SEEN = "saved_hide_seen"
        private const val KEY_AUTO_DELETE = "saved_auto_delete"
    }

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private fun createInitialState(): MainUiState {
        val role = savedStateHandle.get<String>(KEY_ROLE)?.let {
            runCatching { AppRole.valueOf(it) }.getOrNull()
        }
        val query = savedStateHandle.get<String>(KEY_QUERY) ?: ""
        val category = savedStateHandle.get<String>(KEY_CATEGORY)?.let {
            runCatching { IconCategory.valueOf(it) }.getOrNull()
        }
        val sortOrder = savedStateHandle.get<String>(KEY_SORT)?.let {
            runCatching { SortOrder.valueOf(it) }.getOrNull()
        } ?: SortOrder.NEWEST_FIRST
        val isProfileOpen = savedStateHandle.get<Boolean>(KEY_PROFILE_OPEN) ?: false
        val isSettingsOpen = savedStateHandle.get<Boolean>(KEY_SETTINGS_OPEN) ?: false
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
            hideLastSeen = savedStateHandle.get<Boolean>(KEY_HIDE_SEEN) ?: false,
            autoDeleteMessages = savedStateHandle.get<Boolean>(KEY_AUTO_DELETE) ?: true
        )
        val savedType = savedStateHandle.get<String>(KEY_ITEM_TYPE)
        val savedItemId = savedStateHandle.get<String>(KEY_ITEM_ID)
        val selectedItemState = resolveSelectedItemState(savedType, savedItemId)
        val effectiveRole = role ?: AppRole.LOST_SOMETHING
        val feedState = computeFeedState(effectiveRole, query, category, sortOrder)
        return MainUiState(
            selectedRole = role,
            isProfileOpen = isProfileOpen,
            isSettingsOpen = isSettingsOpen,
            profile = profile,
            settings = settings,
            searchQuery = query,
            selectedCategory = category,
            sortOrder = sortOrder,
            feedState = feedState,
            selectedItemState = selectedItemState
        )
    }
    fun handleDeepLink(typeSegment: String?, itemId: String?) {
        if (itemId.isNullOrBlank()) return
        if (savedStateHandle.get<String>(KEY_ITEM_ID) == null) {
            val inferredRole = if (typeSegment == ItemType.LOST.pathSegment) {
                AppRole.FOUND_SOMETHING
            } else {
                AppRole.LOST_SOMETHING
            }
            savedStateHandle[KEY_ROLE] = inferredRole.name
            savedStateHandle[KEY_ITEM_TYPE] = typeSegment
            savedStateHandle[KEY_ITEM_ID] = itemId

            _uiState.update { current ->
                current.copy(
                    selectedRole = inferredRole,
                    feedState = computeFeedState(
                        inferredRole,
                        current.searchQuery,
                        current.selectedCategory,
                        current.sortOrder
                    ),
                    selectedItemState = resolveSelectedItemState(typeSegment, itemId)
                )
            }
        }
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.SelectRole -> {
                savedStateHandle[KEY_ROLE] = action.role.name
                _uiState.update { current ->
                    current.copy(
                        selectedRole = action.role,
                        feedState = computeFeedState(
                            action.role,
                            current.searchQuery,
                            current.selectedCategory,
                            current.sortOrder
                        )
                    )
                }
            }
            MainAction.NavigateBackToRoles -> {
                savedStateHandle[KEY_ROLE] = null
                savedStateHandle[KEY_ITEM_TYPE] = null
                savedStateHandle[KEY_ITEM_ID] = null
                _uiState.update {
                    it.copy(
                        selectedRole = null,
                        selectedItemState = SelectedItemState.None
                    )
                }
            }
            MainAction.OpenProfile -> {
                savedStateHandle[KEY_PROFILE_OPEN] = true
                _uiState.update { it.copy(isProfileOpen = true) }
            }
            MainAction.CloseProfile -> {
                savedStateHandle[KEY_PROFILE_OPEN] = false
                savedStateHandle[KEY_PROFILE_EDITING] = false
                _uiState.update { current ->
                    current.copy(
                        isProfileOpen = false,
                        profile = current.profile.copy(isEditing = false)
                    )
                }
            }
            MainAction.StartEditProfile -> {
                savedStateHandle[KEY_PROFILE_EDITING] = true
                _uiState.update { current ->
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
                _uiState.update { current ->
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
                val p = _uiState.value.profile
                val newName = p.draftName.trim().ifEmpty { p.fullName }
                val newNick = p.draftNickname.trim().removePrefix("@").ifEmpty { p.nickname }
                val newEmail = p.draftEmail.trim().ifEmpty { p.email }
                savedStateHandle[KEY_USER_NAME] = newName
                savedStateHandle[KEY_USER_NICK] = newNick
                savedStateHandle[KEY_USER_EMAIL] = newEmail
                savedStateHandle[KEY_PROFILE_EDITING] = false
                _uiState.update { current ->
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
                _uiState.update { current ->
                    current.copy(profile = current.profile.copy(draftName = action.name))
                }
            }
            is MainAction.UpdateDraftNickname -> {
                savedStateHandle[KEY_DRAFT_NICK] = action.nickname
                _uiState.update { current ->
                    current.copy(profile = current.profile.copy(draftNickname = action.nickname))
                }
            }
            is MainAction.UpdateDraftEmail -> {
                savedStateHandle[KEY_DRAFT_EMAIL] = action.email
                _uiState.update { current ->
                    current.copy(profile = current.profile.copy(draftEmail = action.email))
                }
            }
            MainAction.OpenSettings -> {
                savedStateHandle[KEY_SETTINGS_OPEN] = true
                _uiState.update { it.copy(isSettingsOpen = true) }
            }
            MainAction.CloseSettings -> {
                savedStateHandle[KEY_SETTINGS_OPEN] = false
                _uiState.update { it.copy(isSettingsOpen = false) }
            }
            is MainAction.ChangeFont -> {
                savedStateHandle[KEY_FONT] = action.font.name
                _uiState.update { current ->
                    current.copy(settings = current.settings.copy(appFont = action.font))
                }
            }
            is MainAction.ChangeTheme -> {
                savedStateHandle[KEY_THEME] = action.themeMode.name
                _uiState.update { current ->
                    current.copy(settings = current.settings.copy(themeMode = action.themeMode))
                }
            }
            is MainAction.ToggleNotifications -> {
                savedStateHandle[KEY_NOTIF] = action.enabled
                _uiState.update { current ->
                    current.copy(settings = current.settings.copy(notificationsEnabled = action.enabled))
                }
            }
            is MainAction.ToggleHideLastSeen -> {
                savedStateHandle[KEY_HIDE_SEEN] = action.hide
                _uiState.update { current ->
                    current.copy(settings = current.settings.copy(hideLastSeen = action.hide))
                }
            }
            is MainAction.ToggleAutoDeleteMessages -> {
                savedStateHandle[KEY_AUTO_DELETE] = action.enabled
                _uiState.update { current ->
                    current.copy(settings = current.settings.copy(autoDeleteMessages = action.enabled))
                }
            }
            MainAction.OpenBlacklistClicked -> {
                _uiState.update { it.copy(userMessage = UserMessage.BlacklistOpened) }
            }
            is MainAction.SearchQueryChanged -> {
                savedStateHandle[KEY_QUERY] = action.query
                _uiState.update { current ->
                    val role = current.selectedRole ?: AppRole.LOST_SOMETHING
                    current.copy(
                        searchQuery = action.query,
                        feedState = computeFeedState(
                            role,
                            action.query,
                            current.selectedCategory,
                            current.sortOrder
                        )
                    )
                }
            }
            is MainAction.SelectCategory -> {
                val newCategory = if (_uiState.value.selectedCategory == action.category) null else action.category
                savedStateHandle[KEY_CATEGORY] = newCategory?.name
                _uiState.update { current ->
                    val role = current.selectedRole ?: AppRole.LOST_SOMETHING
                    current.copy(
                        selectedCategory = newCategory,
                        feedState = computeFeedState(
                            role,
                            current.searchQuery,
                            newCategory,
                            current.sortOrder
                        )
                    )
                }
            }
            is MainAction.ChangeSortOrder -> {
                savedStateHandle[KEY_SORT] = action.sortOrder.name
                _uiState.update { current ->
                    val role = current.selectedRole ?: AppRole.LOST_SOMETHING
                    current.copy(
                        sortOrder = action.sortOrder,
                        feedState = computeFeedState(
                            role,
                            current.searchQuery,
                            current.selectedCategory,
                            action.sortOrder
                        )
                    )
                }
            }
            MainAction.ResetFilters -> {
                savedStateHandle[KEY_QUERY] = ""
                savedStateHandle[KEY_CATEGORY] = null
                _uiState.update { current ->
                    val role = current.selectedRole ?: AppRole.LOST_SOMETHING
                    current.copy(
                        searchQuery = "",
                        selectedCategory = null,
                        feedState = computeFeedState(role, "", null, current.sortOrder)
                    )
                }
            }
            MainAction.RetryLoad -> {
                savedStateHandle[KEY_QUERY] = ""
                _uiState.update { current ->
                    val role = current.selectedRole ?: AppRole.LOST_SOMETHING
                    current.copy(
                        searchQuery = "",
                        feedState = computeFeedState(role, "", current.selectedCategory, current.sortOrder)
                    )
                }
            }
            is MainAction.OpenItem -> {
                savedStateHandle[KEY_ITEM_TYPE] = action.typeSegment
                savedStateHandle[KEY_ITEM_ID] = action.itemId
                _uiState.update {
                    it.copy(selectedItemState = resolveSelectedItemState(action.typeSegment, action.itemId))
                }
            }
            MainAction.CloseItemDetails -> {
                savedStateHandle[KEY_ITEM_TYPE] = null
                savedStateHandle[KEY_ITEM_ID] = null
                _uiState.update { it.copy(selectedItemState = SelectedItemState.None) }
            }

            is MainAction.ContactUserClicked -> {
                _uiState.update { it.copy(userMessage = UserMessage.OpenChat(action.itemId)) }
            }

            MainAction.CreateReportClicked -> {
                _uiState.update { it.copy(userMessage = UserMessage.CreateReport) }
            }

            MainAction.NotificationsClicked -> {
                _uiState.update { it.copy(userMessage = UserMessage.Notifications) }
            }

            MainAction.UserMessageShown -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }
    private fun computeFeedState(
        role: AppRole,
        query: String,
        category: IconCategory?,
        sortOrder: SortOrder
    ): FeedState {
        val trimmed = query.trim()
        if (trimmed.equals("loading", ignoreCase = true)) {
            return FeedState.Loading
        }
        if (trimmed.equals("error", ignoreCase = true)) {
            return FeedState.Error
        }
        val items = repository.getItemsForRole(
            role = role,
            query = trimmed,
            category = category,
            sortOrder = sortOrder
        )
        return if (items.isEmpty()) {
            FeedState.Empty
        } else {
            FeedState.Content(items)
        }
    }
    private fun resolveSelectedItemState(typeSegment: String?, itemId: String?): SelectedItemState {
        if (itemId.isNullOrBlank()) return SelectedItemState.None
        val found = repository.getItemByLink(typeSegment, itemId)
        return if (found != null) {
            SelectedItemState.Found(found)
        } else {
            SelectedItemState.NotFound(itemId)
        }
    }
}