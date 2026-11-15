package com.findme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.findme.ui.theme.AppDimens
import com.findme.ui.theme.FindMeTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreenContainer(
    deepLinkType: String?,
    deepLinkItemId: String?,
    deepLinkEventId: Int = 0,
    onShareItem: (LostItem) -> Unit,
    onOpenLanguageSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allRepoItems by LostItemsRepository.allItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val foldInfo = rememberFoldUiInfo(context.findActivity())
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(allRepoItems) {
        viewModel.refreshFeed()
    }

    val initialRoutes = remember {
        if (!deepLinkItemId.isNullOrBlank()) {
            val typeSeg = deepLinkType ?: ItemType.FOUND.pathSegment
            viewModel.syncRoleFromDeepLink(typeSeg, deepLinkItemId)
            arrayOf<NavKey>(
                RoleSelectionRoute,
                FeedListRoute,
                ItemDetailRoute(typeSegment = typeSeg, itemId = deepLinkItemId)
            )
        } else if (uiState.selectedRole != null && !uiState.activeItemId.isNullOrBlank()) {
            val typeSeg = uiState.activeItemType ?: ItemType.FOUND.pathSegment
            arrayOf<NavKey>(
                RoleSelectionRoute,
                FeedListRoute,
                ItemDetailRoute(typeSegment = typeSeg, itemId = uiState.activeItemId!!)
            )
        } else if (uiState.selectedRole != null) {
            arrayOf<NavKey>(RoleSelectionRoute, FeedListRoute)
        } else {
            arrayOf<NavKey>(RoleSelectionRoute)
        }
    }

    val backStack = rememberNavBackStack(*initialRoutes)
    var handledDeepLinkEventId by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.selectedRole, uiState.activeItemId) {
        if (uiState.selectedRole != null && backStack.size == 1 && backStack.lastOrNull() is RoleSelectionRoute) {
            backStack.add(FeedListRoute)
            if (!uiState.activeItemId.isNullOrBlank()) {
                val typeSeg = uiState.activeItemType ?: ItemType.FOUND.pathSegment
                backStack.add(ItemDetailRoute(typeSegment = typeSeg, itemId = uiState.activeItemId!!))
            }
        }
    }

    LaunchedEffect(deepLinkEventId, deepLinkType, deepLinkItemId) {
        if (deepLinkEventId > handledDeepLinkEventId && !deepLinkItemId.isNullOrBlank()) {
            handledDeepLinkEventId = deepLinkEventId
            val typeSeg = deepLinkType ?: ItemType.FOUND.pathSegment
            viewModel.syncRoleFromDeepLink(typeSeg, deepLinkItemId)
            backStack.clear()
            backStack.add(RoleSelectionRoute)
            backStack.add(FeedListRoute)
            backStack.add(ItemDetailRoute(typeSegment = typeSeg, itemId = deepLinkItemId))
        }
    }

    val currentMessage = uiState.userMessage
    val messageText = when (currentMessage) {
        is UserMessage.OpenChat -> stringResource(R.string.toast_chat, currentMessage.itemId)
        UserMessage.ReportSaved -> stringResource(R.string.toast_create)
        UserMessage.ReportDeleted -> stringResource(R.string.toast_deleted)
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

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val directive = calculatePaneScaffoldDirective(adaptiveInfo)
    val isMultiPane = directive.maxHorizontalPartitions > 1
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        directive = directive,
        backNavigationBehavior = BackNavigationBehavior.PopLatest
    )

    val topRoute = backStack.lastOrNull()
    val isFeedVisible = topRoute is FeedListRoute || (isMultiPane && topRoute is ItemDetailRoute)
    val showFeedBottomControls = isFeedVisible && !isSearchFocused && !isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible && isSearchFocused) {
            isSearchFocused = false
            focusManager.clearFocus()
        }
    }

    FindMeTheme(
        themeMode = uiState.settings.themeMode,
        appFont = uiState.settings.appFont
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            val bottomInset = innerPadding.calculateBottomPadding()
            val layoutDirection = LocalLayoutDirection.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
            ) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = {
                        isSearchFocused = false
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (backStack.size > 1) {
                            val popped = backStack.removeLastOrNull()
                            if (popped is ItemDetailRoute) {
                                viewModel.onAction(MainAction.SetActiveItem(null, null))
                            } else if (popped is FeedListRoute) {
                                viewModel.onAction(MainAction.NavigateBackToRoles)
                            }
                        }
                    },
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    popTransitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    predictivePopTransitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    sceneStrategies = listOf(listDetailStrategy),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<RoleSelectionRoute> {
                            RoleSelectionScreen(
                                trustRating = uiState.profile.trustRating,
                                onRoleSelected = { selectedRole ->
                                    viewModel.onAction(MainAction.SelectRole(selectedRole))
                                    if (backStack.lastOrNull() != FeedListRoute) {
                                        backStack.add(FeedListRoute)
                                    }
                                },
                                onOpenProfile = {
                                    backStack.add(ProfileRoute)
                                },
                                modifier = Modifier.padding(bottom = bottomInset)
                            )
                        }

                        entry<FeedListRoute>(
                            metadata = ListDetailSceneStrategy.listPane(
                                sceneKey = "findme_catalog",
                                detailPlaceholder = {
                                    DetailPlaceholderPane()
                                }
                            )
                        ) {
                            FeedListPane(
                                state = uiState,
                                bottomInset = bottomInset,
                                showBottomBar = showFeedBottomControls,
                                showFabInsidePane = !isMultiPane,
                                onSearchFocusChanged = { focused ->
                                    isSearchFocused = focused
                                },
                                onAction = { action ->
                                    when (action) {
                                        MainAction.NavigateBackToRoles -> {
                                            isSearchFocused = false
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            if (uiState.isMyReportsTab) {
                                                viewModel.onAction(MainAction.SetMyReportsTab(false))
                                            } else {
                                                viewModel.onAction(MainAction.NavigateBackToRoles)
                                                if (backStack.size > 1) backStack.removeLastOrNull()
                                            }
                                        }
                                        else -> viewModel.onAction(action)
                                    }
                                },
                                onItemClick = { item ->
                                    isSearchFocused = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.onAction(MainAction.SetActiveItem(item.type.pathSegment, item.id))
                                    backStack.selectItemInPane(item.type.pathSegment, item.id)
                                },
                                onCreateClick = {
                                    isSearchFocused = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    val defaultCreateType = if (uiState.selectedRole == AppRole.LOST_SOMETHING) {
                                        ItemType.LOST.pathSegment
                                    } else {
                                        ItemType.FOUND.pathSegment
                                    }
                                    backStack.add(
                                        EditItemRoute(
                                            itemId = null,
                                            defaultTypeSegment = defaultCreateType
                                        )
                                    )
                                },
                                onOpenProfile = {
                                    isSearchFocused = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    if (backStack.lastOrNull() != ProfileRoute) {
                                        backStack.add(ProfileRoute)
                                    }
                                },
                                onOpenSettings = {
                                    isSearchFocused = false
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    if (backStack.lastOrNull() != SettingsRoute) {
                                        backStack.add(SettingsRoute)
                                    }
                                },
                                onShareItem = onShareItem
                            )
                        }

                        entry<ItemDetailRoute>(
                            metadata = ListDetailSceneStrategy.detailPane(sceneKey = "findme_catalog")
                        ) { route ->
                            val item = remember(route.typeSegment, route.itemId, allRepoItems) {
                                LostItemsRepository.getItemByLink(route.typeSegment, route.itemId)
                            }
                            if (item != null) {
                                ItemDetailScreen(
                                    item = item,
                                    foldPosture = foldInfo.posture,
                                    onBackClick = {
                                        viewModel.onAction(MainAction.SetActiveItem(null, null))
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                    },
                                    onShareClick = { onShareItem(item) },
                                    onContactClick = {
                                        viewModel.onAction(MainAction.ContactUserClicked(item.id))
                                    },
                                    onEditClick = {
                                        backStack.add(
                                            EditItemRoute(
                                                itemId = item.id,
                                                defaultTypeSegment = item.type.pathSegment
                                            )
                                        )
                                    },
                                    onDeleteClick = {
                                        viewModel.onAction(MainAction.DeleteItem(item.id))
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                    },
                                    modifier = Modifier.padding(bottom = bottomInset)
                                )
                            } else {
                                ErrorScreen(
                                    onBackClick = {
                                        viewModel.onAction(MainAction.SetActiveItem(null, null))
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                    },
                                    modifier = Modifier.padding(bottom = bottomInset)
                                )
                            }
                        }

                        entry<EditItemRoute>(
                            metadata = ListDetailSceneStrategy.extraPane(sceneKey = "findme_catalog")
                        ) { route ->
                            EditItemRouteScreen(
                                itemId = route.itemId,
                                defaultTypeSegment = route.defaultTypeSegment,
                                foldPosture = foldInfo.posture,
                                onSaved = { savedItem ->
                                    viewModel.onAction(MainAction.NotifyReportSaved)
                                    viewModel.onAction(MainAction.SetActiveItem(savedItem.type.pathSegment, savedItem.id))
                                    backStack.removeLastOrNull()
                                    backStack.selectItemInPane(savedItem.type.pathSegment, savedItem.id)
                                },
                                onCancel = {
                                    if (backStack.size > 1) backStack.removeLastOrNull()
                                },
                                modifier = Modifier.padding(bottom = bottomInset)
                            )
                        }

                        entry<ProfileRoute> {
                            ProfileScreen(
                                profile = uiState.profile,
                                onAction = { action ->
                                    when (action) {
                                        MainAction.CloseProfile -> {
                                            viewModel.onAction(MainAction.CancelEditProfile)
                                            if (backStack.size > 1) backStack.removeLastOrNull()
                                        }
                                        MainAction.OpenSettings -> backStack.add(SettingsRoute)
                                        else -> viewModel.onAction(action)
                                    }
                                },
                                modifier = Modifier.padding(bottom = bottomInset)
                            )
                        }

                        entry<SettingsRoute> {
                            SettingsScreen(
                                settings = uiState.settings,
                                onAction = { action ->
                                    when (action) {
                                        MainAction.CloseSettings -> {
                                            if (backStack.size > 1) backStack.removeLastOrNull()
                                        }
                                        else -> viewModel.onAction(action)
                                    }
                                },
                                onOpenSystemLanguageSettings = onOpenLanguageSettings,
                                modifier = Modifier.padding(bottom = bottomInset)
                            )
                        }
                    }
                )

                AnimatedVisibility(
                    visible = showFeedBottomControls && isMultiPane,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = AppDimens.paddingMedium,
                            bottom = bottomInset + AppDimens.paddingMedium
                        )
                ) {
                    val defaultCreateType = if (uiState.selectedRole == AppRole.LOST_SOMETHING) {
                        ItemType.LOST.pathSegment
                    } else {
                        ItemType.FOUND.pathSegment
                    }
                    FloatingActionButton(
                        onClick = {
                            isSearchFocused = false
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            backStack.add(
                                EditItemRoute(
                                    itemId = null,
                                    defaultTypeSegment = defaultCreateType
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingOvalBottomBar(
    isMyReportsTab: Boolean,
    onSelectFeedTab: () -> Unit,
    onSelectMyReportsTab: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = AppDimens.paddingSmall,
        shadowElevation = AppDimens.paddingSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.paddingSmall, vertical = AppDimens.radiusSmall),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OvalBarItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = stringResource(R.string.nav_feed),
                selected = !isMyReportsTab,
                onClick = onSelectFeedTab,
                modifier = Modifier.weight(1f)
            )

            OvalBarItem(
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = stringResource(R.string.nav_my_reports),
                selected = isMyReportsTab,
                onClick = onSelectMyReportsTab,
                modifier = Modifier.weight(1f)
            )

            OvalBarItem(
                icon = Icons.Default.Notifications,
                label = stringResource(R.string.nav_notifications),
                selected = false,
                onClick = onNotificationsClick,
                modifier = Modifier.weight(1f)
            )

            OvalBarItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.nav_profile),
                selected = false,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )

            OvalBarItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.nav_settings),
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OvalBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.Tab }
            .padding(vertical = AppDimens.radiusSmall, horizontal = AppDimens.radiusSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(AppDimens.iconSmall)
        )

        Spacer(modifier = Modifier.height(AppDimens.radiusSmall))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailPlaceholderPane() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.paddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconEmptyState),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Text(
                text = stringResource(R.string.detail_select_placeholder),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeedListPane(
    state: MainUiState,
    onAction: (MainAction) -> Unit,
    onItemClick: (LostItem) -> Unit,
    onShareItem: (LostItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = AppDimens.radiusSmall,
    showBottomBar: Boolean = true,
    showFabInsidePane: Boolean = true,
    onSearchFocusChanged: (Boolean) -> Unit = {},
    onCreateClick: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val role = state.selectedRole ?: AppRole.LOST_SOMETHING
    val screenTitle = when {
        state.isMyReportsTab -> stringResource(R.string.feed_my_title)
        role == AppRole.LOST_SOMETHING -> stringResource(R.string.feed_lost_title)
        else -> stringResource(R.string.feed_found_title)
    }
    val bannerTitle = when {
        state.isMyReportsTab -> stringResource(R.string.banner_my_title)
        role == AppRole.LOST_SOMETHING -> stringResource(R.string.banner_lost_title)
        else -> stringResource(R.string.banner_found_title)
    }
    val bannerSubtitle = when {
        state.isMyReportsTab -> stringResource(R.string.banner_my_desc)
        role == AppRole.LOST_SOMETHING -> stringResource(R.string.banner_lost_desc)
        else -> stringResource(R.string.banner_found_desc)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AppDimens.paddingMedium,
                end = AppDimens.paddingMedium,
                top = AppDimens.paddingSmall,
                bottom = bottomInset + AppDimens.fabBottomSpace + AppDimens.iconEmptyState
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.paddingBannerIcon)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onAction(MainAction.NavigateBackToRoles) },
                        modifier = Modifier.padding(end = AppDimens.radiusSmall)
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
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(AppDimens.radiusMedium)
                ) {
                    Row(modifier = Modifier.padding(AppDimens.paddingBannerIcon)) {
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
                SearchAndFilterBar(
                    query = state.searchQuery,
                    selectedCategory = state.selectedCategory,
                    sortOrder = state.sortOrder,
                    onQueryChange = { onAction(MainAction.SearchQueryChanged(it)) },
                    onCategorySelect = { onAction(MainAction.SelectCategory(it)) },
                    onSortOrderChange = { onAction(MainAction.ChangeSortOrder(it)) },
                    onSearchFocusChanged = onSearchFocusChanged
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
                            onItemClick = { onItemClick(item) },
                            onShareClick = { onShareItem(item) },
                            onContactClick = {
                                onAction(MainAction.ContactUserClicked(item.id))
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = AppDimens.paddingMedium,
                    end = AppDimens.paddingMedium,
                    bottom = bottomInset + AppDimens.paddingSmall
                )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                FloatingOvalBottomBar(
                    isMyReportsTab = state.isMyReportsTab,
                    onSelectFeedTab = {
                        onAction(MainAction.SetMyReportsTab(false))
                    },
                    onSelectMyReportsTab = {
                        onAction(MainAction.SetMyReportsTab(true))
                    },
                    onNotificationsClick = {
                        onAction(MainAction.NotificationsClicked)
                    },
                    onProfileClick = onOpenProfile,
                    onSettingsClick = onOpenSettings,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (showFabInsidePane) {
                    FloatingActionButton(
                        onClick = onCreateClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = AppDimens.fabBottomSpace)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAndFilterBar(
    query: String,
    selectedCategory: IconCategory?,
    sortOrder: SortOrder,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (IconCategory?) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    onSearchFocusChanged: (Boolean) -> Unit = {}
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val hasFilter = selectedCategory != null

    var allowSearchFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(allowSearchFocus) {
        if (allowSearchFocus) {
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusProperties { canFocus = allowSearchFocus }
                    .onFocusChanged { focusState ->
                        onSearchFocusChanged(focusState.isFocused)
                        if (!focusState.isFocused) {
                            allowSearchFocus = false
                        }
                    },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        allowSearchFocus = false
                        focusManager.clearFocus()
                    }
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_label),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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

            if (!allowSearchFocus) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    allowSearchFocus = true
                                }
                            )
                        }
                )
            }
        }

        Box {
            FilledTonalIconButton(
                onClick = { categoryMenuExpanded = true },
                modifier = Modifier.size(AppDimens.iconTouchTarget),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (hasFilter) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.cd_filter_category),
                    tint = if (hasFilter) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                CategoryMenuItem(
                    title = stringResource(R.string.filter_all),
                    selected = selectedCategory == null,
                    onClick = {
                        onCategorySelect(null)
                        categoryMenuExpanded = false
                    }
                )

                CategoryMenuItem(
                    title = stringResource(R.string.filter_electronics),
                    selected = selectedCategory == IconCategory.ELECTRONICS,
                    onClick = {
                        onCategorySelect(IconCategory.ELECTRONICS)
                        categoryMenuExpanded = false
                    }
                )

                CategoryMenuItem(
                    title = stringResource(R.string.filter_pets),
                    selected = selectedCategory == IconCategory.PET,
                    onClick = {
                        onCategorySelect(IconCategory.PET)
                        categoryMenuExpanded = false
                    }
                )

                CategoryMenuItem(
                    title = stringResource(R.string.filter_documents),
                    selected = selectedCategory == IconCategory.DOCUMENT,
                    onClick = {
                        onCategorySelect(IconCategory.DOCUMENT)
                        categoryMenuExpanded = false
                    }
                )

                CategoryMenuItem(
                    title = stringResource(R.string.filter_other),
                    selected = selectedCategory == IconCategory.OTHER,
                    onClick = {
                        onCategorySelect(IconCategory.OTHER)
                        categoryMenuExpanded = false
                    }
                )
            }
        }

        Box {
            FilledTonalIconButton(
                onClick = { sortMenuExpanded = true },
                modifier = Modifier.size(AppDimens.iconTouchTarget),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.cd_sort_time),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                CategoryMenuItem(
                    title = stringResource(R.string.sort_newest),
                    selected = sortOrder == SortOrder.NEWEST_FIRST,
                    onClick = {
                        onSortOrderChange(SortOrder.NEWEST_FIRST)
                        sortMenuExpanded = false
                    }
                )

                CategoryMenuItem(
                    title = stringResource(R.string.sort_oldest),
                    selected = sortOrder == SortOrder.OLDEST_FIRST,
                    onClick = {
                        onSortOrderChange(SortOrder.OLDEST_FIRST)
                        sortMenuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryMenuItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(title) },
        onClick = onClick,
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

private val previewFoundItems = listOf(
    LostItem(
        id = "found_1",
        type = ItemType.FOUND,
        category = "Найдена электроника",
        description = "Серый смартфон (детали скрыты слепым поиском).",
        location = "Район ст. м. Бауманская",
        createdAtMillis = 1762516800000L,
        isMine = false,
        photoAttachment = PhotoAttachment.PHONE_PHOTO,
        iconType = IconCategory.ELECTRONICS
    )
)

@Preview(showBackground = true, name = "1. FeedListPane - Content")
@Composable
private fun MainScreenContentPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            FeedListPane(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Content(previewFoundItems)
                ),
                onAction = {},
                onItemClick = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. FeedListPane - Loading")
@Composable
private fun MainScreenLoadingPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            FeedListPane(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Loading
                ),
                onAction = {},
                onItemClick = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. FeedListPane - Empty")
@Composable
private fun MainScreenEmptyPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            FeedListPane(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    searchQuery = "Ключи",
                    feedState = FeedState.Empty
                ),
                onAction = {},
                onItemClick = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "4. FeedListPane - Error")
@Composable
private fun MainScreenErrorPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            FeedListPane(
                state = MainUiState(
                    selectedRole = AppRole.LOST_SOMETHING,
                    feedState = FeedState.Error
                ),
                onAction = {},
                onItemClick = {},
                onShareItem = {}
            )
        }
    }
}