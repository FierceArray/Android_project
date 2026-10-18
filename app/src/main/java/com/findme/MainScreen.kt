package com.findme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

sealed interface FeedState {
    data object Loading : FeedState
    data object Empty : FeedState
    data class Error(val message: String) : FeedState
    data class Content(val items: List<LostItem>) : FeedState
}

@Composable
fun MainScreen(
    role: AppRole,
    initialSelectedId: String?,
    onNavigateBackToRoles: () -> Unit
) {
    val context = LocalContext.current
    val screenTitle = if (role == AppRole.LOST_SOMETHING) stringResource(R.string.feed_lost_title) else stringResource(R.string.feed_found_title)
    val bannerTitle = if (role == AppRole.LOST_SOMETHING) stringResource(R.string.banner_lost_title) else stringResource(R.string.banner_found_title)
    val bannerSubtitle = if (role == AppRole.LOST_SOMETHING) stringResource(R.string.banner_lost_desc) else stringResource(R.string.banner_found_desc)
    val testItems = remember {
        listOf(
            LostItem("1", "Найдена электроника", "Серый смартфон в черном чехле. Экран заблокирован.", "Район ст. м. Бауманская", "Сегодня в 14:00", IconCategory.ELECTRONICS),
            LostItem("2", "Найден питомец", "Собака в ошейнике без адресника. Очень напугана.", "Район ст. м. Сокольники", "Сегодня в 12:30", IconCategory.PET),
            LostItem("3", "Найдены документы", "Синяя папка с чертежами формата А4.", "Район ст. м. Курская", "Сегодня в 11:15", IconCategory.DOCUMENT),
            LostItem("4", "Найдена электроника", "Беспроводные наушники в белом кейсе. Найдены на скамейке.", "Район ст. м. Китай-город", "Вчера в 19:40", IconCategory.ELECTRONICS),
            LostItem("5", "Найдены вещи", "Связка из 4 ключей с металлическим брелоком в виде домика.", "Район ВДНХ", "Вчера в 16:00", IconCategory.OTHER),
            LostItem("6", "Найден питомец", "Рыжий пушистый кот. Сидел в подъезде, забрали на передержку.", "Район ст. м. Чистые пруды", "Вчера в 09:15", IconCategory.PET),
            LostItem("7", "Найдены документы", "Студенческий билет в синей обложке.", "Район ст. м. Университет", "Позавчера в 15:30", IconCategory.DOCUMENT),
            LostItem("8", "Найдена электроника", "Серебристый ноутбук в черном тканевом чехле. Забыт на фудкорте.", "ТЦ Атриум", "Позавчера в 20:00", IconCategory.ELECTRONICS),
            LostItem("9", "Найдены вещи", "Черный городской рюкзак.", "Парк Горького", "02.09.26 в 14.30", IconCategory.OTHER),
            LostItem("10", "Найден питомец", "Волнистый попугай желто-зеленого цвета. Залетел в окно.", "Район Тверской улицы", "12.10.26 в 09.45", IconCategory.PET)
        )
    }
    // FeedState.Content(testItems) FeedState.Loading FeedState.Empty FeedState.Error("Нет соединения с Wi-Fi")
    var uiState by remember { mutableStateOf<FeedState>(FeedState.Content(testItems)) }
    var selectedItemId by rememberSaveable { mutableStateOf(initialSelectedId) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedItemId == null) {
                FloatingActionButton(
                    onClick = { android.widget.Toast.makeText(context, context.getString(R.string.toast_create), android.widget.Toast.LENGTH_SHORT).show() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        if (selectedItemId != null) {
            val currentItem = testItems.find { it.id == selectedItemId }
            if (currentItem != null) {
                ItemDetailScreen(
                    item = currentItem,
                    onBackClick = { selectedItemId = null },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                ErrorScreen(
                    onBackClick = { selectedItemId = null },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBackToRoles,
                            modifier = Modifier.padding(end = 8.dp)
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
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { android.widget.Toast.makeText(context, context.getString(R.string.toast_notifications), android.widget.Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.cd_notifications), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(bannerTitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(bannerSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                when (val state = uiState) {
                    is FeedState.Loading -> items(3) { SkeletonCard() }
                    is FeedState.Empty -> item { EmptyStateView() }
                    is FeedState.Error -> item { ErrorStateView(state.message) { uiState = FeedState.Loading } }
                    is FeedState.Content -> {
                        items(items = state.items, key = { it.id }) { item ->
                            LostItemCard(
                                item = item,
                                onItemClick = { selectedItemId = item.id }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Box(modifier = Modifier.height(20.dp).width(150.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(16.dp).width(100.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.height(48.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}