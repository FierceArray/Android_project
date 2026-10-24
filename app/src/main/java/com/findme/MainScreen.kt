package com.findme

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.findme.ui.theme.AppDimens
import com.findme.ui.theme.FindMeTheme

@Composable
fun MainScreenContainer(
    deepLinkType: String?,
    deepLinkItemId: String?,
    onShareItem: (LostItem) -> Unit,
    onOpenLanguageSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deepLinkType, deepLinkItemId) {
        viewModel.handleDeepLink(deepLinkType, deepLinkItemId)
    }
    val currentMessage = uiState.userMessage
    val messageText = when (currentMessage) {
        is UserMessage.OpenChat -> stringResource(R.string.toast_chat, currentMessage.itemId)
        UserMessage.CreateReport -> stringResource(R.string.toast_create)
        UserMessage.Notifications -> stringResource(R.string.toast_notifications)
        UserMessage.BlacklistOpened -> stringResource(R.string.toast_blacklist)
        UserMessage.ProfileSaved -> stringResource(R.string.toast_profile_saved)
        null -> null
    }
    LaunchedEffect(currentMessage, messageText) {
        if (currentMessage != null && messageText != null) {
            snackbarHostState.showSnackbar(message = messageText)
            viewModel.onAction(MainAction.UserMessageShown)
        }
    }
    FindMeTheme(
        themeMode = uiState.settings.themeMode,
        appFont = uiState.settings.appFont
    ) {
        MainScreen(
            state = uiState,
            onAction = viewModel::onAction,
            onShareItem = onShareItem,
            onOpenLanguageSettings = onOpenLanguageSettings,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun MainScreen(
    state: MainUiState,
    onAction: (MainAction) -> Unit,
    onShareItem: (LostItem) -> Unit,
    modifier: Modifier = Modifier,
    onOpenLanguageSettings: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val role = state.selectedRole ?: AppRole.LOST_SOMETHING
    val screenTitle = if (role == AppRole.LOST_SOMETHING) {
        stringResource(R.string.feed_lost_title)
    } else {
        stringResource(R.string.feed_found_title)
    }
    val bannerTitle = if (role == AppRole.LOST_SOMETHING) {
        stringResource(R.string.banner_lost_title)
    } else {
        stringResource(R.string.banner_found_title)
    }
    val bannerSubtitle = if (role == AppRole.LOST_SOMETHING) {
        stringResource(R.string.banner_lost_desc)
    } else {
        stringResource(R.string.banner_found_desc)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (!state.isSettingsOpen &&
                !state.isProfileOpen &&
                state.selectedRole != null &&
                state.selectedItemState is SelectedItemState.None
            ) {
                FloatingActionButton(
                    onClick = { onAction(MainAction.CreateReportClicked) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        when {
            state.isSettingsOpen -> {
                SettingsScreen(
                    settings = state.settings,
                    onAction = onAction,
                    onOpenSystemLanguageSettings = onOpenLanguageSettings,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            state.isProfileOpen -> {
                ProfileScreen(
                    profile = state.profile,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            state.selectedRole == null && state.selectedItemState is SelectedItemState.None -> {
                RoleSelectionScreen(
                    trustRating = state.profile.trustRating,
                    onRoleSelected = { selected -> onAction(MainAction.SelectRole(selected)) },
                    onOpenProfile = { onAction(MainAction.OpenProfile) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                when (val selected = state.selectedItemState) {
                    is SelectedItemState.Found -> {
                        ItemDetailScreen(
                            item = selected.item,
                            onBackClick = { onAction(MainAction.CloseItemDetails) },
                            onShareClick = { onShareItem(selected.item) },
                            onContactClick = { onAction(MainAction.ContactUserClicked(selected.item.id)) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    is SelectedItemState.NotFound -> {
                        ErrorScreen(
                            onBackClick = { onAction(MainAction.CloseItemDetails) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    SelectedItemState.None -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = AppDimens.paddingMedium,
                                end = AppDimens.paddingMedium,
                                top = innerPadding.calculateTopPadding() + AppDimens.paddingMedium,
                                bottom = innerPadding.calculateBottomPadding() + AppDimens.fabBottomSpace
                            ),
                            verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onAction(MainAction.NavigateBackToRoles) },
                                        modifier = Modifier.padding(end = AppDimens.paddingSmall)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.cd_back),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = screenTitle,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { heading() }
                                    )
                                    IconButton(onClick = { onAction(MainAction.NotificationsClicked) }) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = stringResource(R.string.cd_notifications),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(onClick = { onAction(MainAction.OpenSettings) }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.cd_settings),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(AppDimens.radiusMedium)
                                ) {
                                    Row(modifier = Modifier.padding(AppDimens.paddingMedium)) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(AppDimens.paddingBannerIcon))
                                        Column {
                                            Text(
                                                text = bannerTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = bannerSubtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                SearchField(
                                    query = state.searchQuery,
                                    onQueryChange = { onAction(MainAction.SearchQueryChanged(it)) }
                                )
                            }
                            item {
                                CategoryFilterRow(
                                    selectedCategory = state.selectedCategory,
                                    onCategorySelect = { onAction(MainAction.SelectCategory(it)) }
                                )
                            }
                            item {
                                SortOrderRow(
                                    sortOrder = state.sortOrder,
                                    onSortOrderChange = { onAction(MainAction.ChangeSortOrder(it)) }
                                )
                            }

                            when (val feed = state.feedState) {
                                is FeedState.Loading -> {
                                    items(3) { SkeletonCard() }
                                }

                                is FeedState.Empty -> {
                                    item {
                                        EmptyStateView(
                                            hasActiveFilters = state.hasActiveFilters,
                                            onResetFilters = { onAction(MainAction.ResetFilters) }
                                        )
                                    }
                                }

                                is FeedState.Error -> {
                                    item {
                                        ErrorStateView(
                                            message = stringResource(R.string.error_network),
                                            onRetry = { onAction(MainAction.RetryLoad) }
                                        )
                                    }
                                }

                                is FeedState.Content -> {
                                    item {
                                        Text(
                                            text = stringResource(R.string.found_count_label, state.itemCount),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    items(
                                        items = feed.items,
                                        key = { it.id },
                                        contentType = { "lost_item_card" }
                                    ) { item ->
                                        LostItemCard(
                                            item = item,
                                            onItemClick = {
                                                onAction(MainAction.OpenItem(item.type.pathSegment, item.id))
                                            },
                                            onShareClick = { onShareItem(item) },
                                            onContactClick = {
                                                onAction(MainAction.ContactUserClicked(item.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(AppDimens.radiusLarge),
        label = { Text(stringResource(R.string.search_label)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.search_clear)
                    )
                }
            }
        }
    )
}

@Composable
fun CategoryFilterRow(
    selectedCategory: IconCategory?,
    onCategorySelect: (IconCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) },
            label = { Text(stringResource(R.string.filter_all)) }
        )
        FilterChip(
            selected = selectedCategory == IconCategory.ELECTRONICS,
            onClick = { onCategorySelect(IconCategory.ELECTRONICS) },
            label = { Text(stringResource(R.string.filter_electronics)) }
        )
        FilterChip(
            selected = selectedCategory == IconCategory.PET,
            onClick = { onCategorySelect(IconCategory.PET) },
            label = { Text(stringResource(R.string.filter_pets)) }
        )
        FilterChip(
            selected = selectedCategory == IconCategory.DOCUMENT,
            onClick = { onCategorySelect(IconCategory.DOCUMENT) },
            label = { Text(stringResource(R.string.filter_documents)) }
        )
        FilterChip(
            selected = selectedCategory == IconCategory.OTHER,
            onClick = { onCategorySelect(IconCategory.OTHER) },
            label = { Text(stringResource(R.string.filter_other)) }
        )
    }
}

@Composable
fun SortOrderRow(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AppDimens.iconSmall)
        )
        Text(
            text = stringResource(R.string.sort_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
            selected = sortOrder == SortOrder.NEWEST_FIRST,
            onClick = { onSortOrderChange(SortOrder.NEWEST_FIRST) },
            label = { Text(stringResource(R.string.sort_newest)) }
        )
        FilterChip(
            selected = sortOrder == SortOrder.OLDEST_FIRST,
            onClick = { onSortOrderChange(SortOrder.OLDEST_FIRST) },
            label = { Text(stringResource(R.string.sort_oldest)) }
        )
    }
}

@Composable
fun SkeletonCard() {
    var isHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isHighlighted = true
    }
    val skeletonColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800),
        finishedListener = {
            isHighlighted = !isHighlighted
        },
        label = "skeleton_shimmer"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.iconTouchTarget)
                        .clip(CircleShape)
                        .background(skeletonColor)
                )
                Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
                Column {
                    Box(
                        modifier = Modifier
                            .height(AppDimens.skeletonHeight)
                            .width(AppDimens.skeletonWidthLong)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(skeletonColor)
                    )
                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                    Box(
                        modifier = Modifier
                            .height(AppDimens.iconSmall)
                            .width(AppDimens.skeletonWidthShort)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(skeletonColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
            Box(
                modifier = Modifier
                    .height(AppDimens.iconTouchTarget)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusExtraLarge))
                    .background(skeletonColor)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    hasActiveFilters: Boolean = false,
    onResetFilters: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimens.paddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.iconEmptyState),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
        Text(
            text = stringResource(R.string.empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
            Button(onClick = onResetFilters) {
                Text(stringResource(R.string.action_reset_filters))
            }
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimens.paddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.iconEmptyState),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

private val previewFoundItems = listOf(
    LostItem(
        id = "found_1",
        type = ItemType.FOUND,
        category = "Найдена электроника",
        description = "Серый смартфон (детали скрыты слепым поиском).",
        location = "Район ст. м. Бауманская",
        time = "Сегодня в 14:00",
        timestampOrder = 1,
        iconType = IconCategory.ELECTRONICS
    )
)

@Preview(showBackground = true, name = "1. MainScreen - Content")
@Composable
private fun MainScreenContentPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            MainScreen(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Content(previewFoundItems)
                ),
                onAction = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. MainScreen - Loading")
@Composable
private fun MainScreenLoadingPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            MainScreen(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Loading
                ),
                onAction = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. MainScreen - Empty")
@Composable
private fun MainScreenEmptyPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            MainScreen(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    searchQuery = "Ключи",
                    feedState = FeedState.Empty
                ),
                onAction = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "4. MainScreen - Error")
@Composable
private fun MainScreenErrorPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            MainScreen(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Error
                ),
                onAction = {},
                onShareItem = {}
            )
        }
    }
}