package com.findme

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val TAG = "ActivityLifecycle"

    private var deepLinkTypeState by mutableStateOf<String?>(null)
    private var deepLinkItemIdState by mutableStateOf<String?>(null)
    private var deepLinkEventId by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity создана")
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            extractDeepLink(intent)
        }

        setContent {
            MainScreenContainer(
                deepLinkType = deepLinkTypeState,
                deepLinkItemId = deepLinkItemIdState,
                deepLinkEventId = deepLinkEventId,
                onShareItem = { item -> shareItem(item) },
                onOpenLanguageSettings = { openSystemLocaleSettings() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(TAG, "onNewIntent: Получен новый Intent")
        extractDeepLink(intent)
    }

    private fun extractDeepLink(incomingIntent: Intent?) {
        val intentData = incomingIntent?.data
        if (incomingIntent?.action == Intent.ACTION_VIEW && intentData != null) {
            deepLinkTypeState = intentData.pathSegments.getOrNull(0)
            deepLinkItemIdState = intentData.pathSegments.getOrNull(1)
            deepLinkEventId++
        }
    }

    private fun shareItem(item: LostItem) {
        val shareMessage = if (item.type == ItemType.FOUND) {
            getString(R.string.share_message_found, item.deepLink)
        } else {
            getString(R.string.share_message_lost, item.deepLink)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun openSystemLocaleSettings() {
        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity стала видимой")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity получила фокус")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity теряет фокус")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity больше не видна")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity уничтожена")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: Возврат в Activity")
    }
}