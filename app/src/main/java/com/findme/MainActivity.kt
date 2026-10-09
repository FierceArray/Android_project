package com.findme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.findme.ui.theme.FindMeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "ActivityLifecycle"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate: Activity создана")
        enableEdgeToEdge()
        val intentData = intent.data
        var openedItemId: String? = null
        if (intent.action == android.content.Intent.ACTION_VIEW && intentData != null) {
            openedItemId = intentData.pathSegments.getOrNull(1)
        }
        setContent {
            FindMeTheme {
                MainScreen(initialSelectedId = openedItemId)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        android.util.Log.d(TAG, "onStart: Activity стала видимой")
    }
    override fun onResume() {
        super.onResume()
        android.util.Log.d(TAG, "onResume: Activity получила фокус")
    }
    override fun onPause() {
        super.onPause()
        android.util.Log.d(TAG, "onPause: Activity теряет фокус")
    }
    override fun onStop() {
        super.onStop()
        android.util.Log.d(TAG, "onStop: Activity больше не видна")
    }
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "onDestroy: Activity уничтожена")
    }
    override fun onRestart() {
        super.onRestart()
        android.util.Log.d(TAG, "onRestart: Возврат в Activity")
    }
}

@Composable
fun MainScreen(initialSelectedId: String?) {
    val context = LocalContext.current
    // защита от двойных кликов
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
            val currentItem = testItems.find {it.id == selectedItemId}
            if (currentItem != null) {
                ItemDetailScreen(
                    item = currentItem,
                    modifier = Modifier.padding(innerPadding),
                    onBackClick = {selectedItemId = null}
                )
            } else {
                ErrorScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBackClick = {selectedItemId = null}
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
                items(items = testItems, key = {it.id}) {
                    item -> LostItemCard(
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
                                    putExtra(android.content.Intent.EXTRA_TEXT, "$deepLink")
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

@Composable
fun ItemDetailScreen(
    item: LostItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "⟵"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "ID: #${item.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Анкета не найдена или ссылка повреждена",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick) {
            Text("Вернуться к списку")
        }
    }
}

@Composable
fun LostItemCard(
    item: LostItem,
    onItemClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onItemClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.category_label, item.category),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onShareClick, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.share_button))
            }
        }
    }
}
