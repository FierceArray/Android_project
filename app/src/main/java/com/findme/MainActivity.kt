package com.findme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.findme.ui.theme.FindMeTheme

class MainActivity : ComponentActivity() {
    private val TAG = "ActivityLifecycle"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate: Activity создана")
        enableEdgeToEdge()
        val intentData = intent.data
        val deepLinkItemId = if (intent.action == android.content.Intent.ACTION_VIEW && intentData != null) {
            intentData.pathSegments.getOrNull(1)
        } else {
            null
        }
        setContent {
            FindMeTheme {
                var openedItemId by rememberSaveable { mutableStateOf(deepLinkItemId) }
                var selectedRole by rememberSaveable { mutableStateOf<AppRole?>(null) }
                if (selectedRole == null && openedItemId == null) {
                    RoleSelectionScreen(
                        onRoleSelected = { role -> selectedRole = role }
                    )
                } else {
                    MainScreen(
                        role = selectedRole ?: AppRole.LOST_SOMETHING,
                        initialSelectedId = openedItemId,
                        onNavigateBackToRoles = {
                            selectedRole = null
                            openedItemId = null
                        }
                    )
                }
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


