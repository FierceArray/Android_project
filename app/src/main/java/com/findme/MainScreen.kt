package com.findme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

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

    LaunchedEffect(allRepoItems) {
        viewModel.refreshFeed()
    }

    val initialRoutes = remember {
        if (!deepLinkItemId.isNullOrBlank()) {
            val typeSeg = deepLinkType ?: ItemType.FOUND.pathSegment
            viewModel.syncRoleFromDeepLink(typeSeg)
            arrayOf<NavKey>(
                RoleSelectionRoute,
                FeedListRoute,
                ItemDetailRoute(typeSegment = typeSeg, itemId = deepLinkItemId)
            )
        } else if (uiState.selectedRole != null) {
            arrayOf<NavKey>(RoleSelectionRoute, FeedListRoute)
        } else {
            arrayOf<NavKey>(RoleSelectionRoute)
        }
    }

    val backStack = rememberNavBackStack(*initialRoutes)
    var handledDeepLinkEventId by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(deepLinkEventId, deepLinkType, deepLinkItemId) {
        if (deepLinkEventId > handledDeepLinkEventId && !deepLinkItemId.isNullOrBlank()) {
            handledDeepLinkEventId = deepLinkEventId
            val typeSeg = deepLinkType ?: ItemType.FOUND.pathSegment
            viewModel.syncRoleFromDeepLink(typeSeg)
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
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        directive = directive,
        backNavigationBehavior = BackNavigationBehavior.PopLatest
    )

    FindMeTheme(
        themeMode = uiState.settings.themeMode,
        appFont = uiState.settings.appFont
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            val bottomInset = innerPadding.calculateBottomPadding()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current)
                    )
            ) {
                if (foldInfo.posture != FoldPosture.NORMAL) {
                    FoldPostureBanner(posture = foldInfo.posture)
                }

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.weight(1f),
                    onBack = {
                        if (backStack.size > 1) {
                            val popped = backStack.removeLastOrNull()
                            if (popped is FeedListRoute) {
                                viewModel.onAction(MainAction.NavigateBackToRoles)
                            }
                        }
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
                            val defaultCreateType = if (uiState.selectedRole == AppRole.LOST_SOMETHING) {
                                ItemType.LOST.pathSegment
                            } else {
                                ItemType.FOUND.pathSegment
                            }
                            FeedListPane(
                                state = uiState,
                                bottomInset = bottomInset,
                                onAction = { action ->
                                    when (action) {
                                        MainAction.NavigateBackToRoles -> {
                                            viewModel.onAction(MainAction.NavigateBackToRoles)
                                            if (backStack.size > 1) backStack.removeLastOrNull()
                                        }
                                        MainAction.OpenSettings -> backStack.add(SettingsRoute)
                                        else -> viewModel.onAction(action)
                                    }
                                },
                                onItemClick = { item ->
                                    backStack.selectItemInPane(item.type.pathSegment, item.id)
                                },
                                onCreateClick = {
                                    backStack.add(EditItemRoute(itemId = null, defaultTypeSegment = defaultCreateType))
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
                                    val roleForSaved = if (savedItem.type == ItemType.FOUND) {
                                        AppRole.LOST_SOMETHING
                                    } else {
                                        AppRole.FOUND_SOMETHING
                                    }
                                    viewModel.onAction(MainAction.SelectRole(roleForSaved))
                                    viewModel.onAction(MainAction.NotifyReportSaved)
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
            }
        }
        }
    }


@Composable
private fun FoldPostureBanner(posture: FoldPosture) {
    val text = when (posture) {
        FoldPosture.BOOK -> stringResource(R.string.fold_book_mode)
        FoldPosture.TABLETOP -> stringResource(R.string.fold_tabletop_mode)
        FoldPosture.NORMAL -> return
    }
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = AppDimens.paddingSmall, horizontal = AppDimens.paddingMedium)
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
    onCreateClick: () -> Unit,
    onShareItem: (LostItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: androidx.compose.ui.unit.Dp = AppDimens.paddingSmall
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AppDimens.paddingMedium,
                end = AppDimens.paddingMedium,
                top = AppDimens.paddingMedium,
                bottom = bottomInset + AppDimens.fabBottomSpace
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
                SearchAndFilterBar(
                    query = state.searchQuery,
                    selectedCategory = state.selectedCategory,
                    onlyMyReports = state.onlyMyReports,
                    sortOrder = state.sortOrder,
                    onQueryChange = { onAction(MainAction.SearchQueryChanged(it)) },
                    onCategorySelect = { onAction(MainAction.SelectCategory(it)) },
                    onToggleOnlyMyReports = { onAction(MainAction.ToggleOnlyMyReports) },
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

        FloatingActionButton(
            onClick = onCreateClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppDimens.paddingMedium,
                    bottom = bottomInset + AppDimens.paddingMedium
                )
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
        }
    }
}

@Composable
fun SearchAndFilterBar(
    query: String,
    selectedCategory: IconCategory?,
    onlyMyReports: Boolean,
    sortOrder: SortOrder,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (IconCategory?) -> Unit,
    onToggleOnlyMyReports: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val hasFilter = selectedCategory != null || onlyMyReports

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
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
                    title = stringResource(R.string.filter_only_mine),
                    selected = onlyMyReports,
                    onClick = {
                        onToggleOnlyMyReports()
                        categoryMenuExpanded = false
                    }
                )
                HorizontalDivider()
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

@Composable
fun SkeletonCard() {
    var sweepTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        sweepTriggered = true
    }
    val shimmerTranslate by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (sweepTriggered) 1400f else -400f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 1200,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        finishedListener = {
            sweepTriggered = !sweepTriggered
        },
        label = "skeleton_shimmer_sweep"
    )
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
    val highlightColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(x = shimmerTranslate - 250f, y = shimmerTranslate - 250f),
        end = Offset(x = shimmerTranslate, y = shimmerTranslate)
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
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
                Column {
                    Box(
                        modifier = Modifier
                            .height(AppDimens.skeletonHeight)
                            .width(AppDimens.skeletonWidthLong)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                    Box(
                        modifier = Modifier
                            .height(AppDimens.iconSmall)
                            .width(AppDimens.skeletonWidthShort)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(shimmerBrush)
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
            Box(
                modifier = Modifier
                    .height(AppDimens.iconTouchTarget)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusExtraLarge))
                    .background(shimmerBrush)
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
        createdAtMillis = 1762516800000L,
        isMine = true,
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
                onCreateClick = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. FeedListPane - Empty")
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
                onCreateClick = {},
                onShareItem = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. FeedListPane - Error")
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
                onCreateClick = {},
                onShareItem = {}
            )
        }
    }
}