package com.findme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(initialSelectedId: String?) {
    val context = LocalContext.current
    // Защита от двойных кликов
    var isClickBlocked by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val testItems = remember {
        listOf(
            LostItem("1", "Ключи", "Связка ключей с брелком-котом"),
            LostItem("2", "Документы", "Паспорт в синей обложке"),
            LostItem("3", "Электроника", "Наушники в белом кейсе"),
            LostItem("4", "Животные", "Рыжий кот, откликается на Барсика"),
            LostItem("5", "Одежда", "Черный шарф крупной вязки"),
            LostItem("6", "Другое", "Зонт-трость черный"),
            LostItem("7", "Электроника", "Зарядное устройство Type-C"),
            LostItem("8", "Документы", "Студенческий билет")
        )
    }
    var selectedItemId by rememberSaveable { mutableStateOf(initialSelectedId) }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (selectedItemId != null) {
            val currentItem = testItems.find { it.id == selectedItemId }
            if (currentItem != null) {
                ItemDetailScreen(
                    item = currentItem,
                    modifier = Modifier.padding(innerPadding),
                    onBackClick = { selectedItemId = null }
                )
            } else {
                ErrorScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBackClick = { selectedItemId = null }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.lost_items_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items = testItems, key = { it.id }) { item ->
                    LostItemCard(
                        item = item,
                        onItemClick = {
                            selectedItemId = item.id
                        },
                        onShareClick = {
                            if (!isClickBlocked) {
                                isClickBlocked = true
                                val deepLink = "findme://app/lost/${item.id}"
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, deepLink)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                                coroutineScope.launch {
                                    delay(1000)
                                    isClickBlocked = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}