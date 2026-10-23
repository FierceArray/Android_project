package com.findme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.findme.ui.theme.AppDimens

enum class AppRole {
    LOST_SOMETHING,
    FOUND_SOMETHING
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (AppRole) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.paddingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                    Text("Find me", style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = stringResource(R.string.cd_profile), tint = MaterialTheme.colorScheme.primary)
                    Badge(containerColor = MaterialTheme.colorScheme.onSurface) {
                        Text("⭐ 4.9", color = MaterialTheme.colorScheme.surface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingExtraLarge))
            Text(stringResource(R.string.role_headline), style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
            Text(stringResource(R.string.role_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(AppDimens.paddingExtraLarge))
            RoleCard(
                title = stringResource(R.string.role_lost_title),
                subtitle = stringResource(R.string.role_lost_desc),
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = { onRoleSelected(AppRole.LOST_SOMETHING) }
            )
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
            RoleCard(
                title = stringResource(R.string.role_found_title),
                subtitle = stringResource(R.string.role_found_desc),
                icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = { onRoleSelected(AppRole.FOUND_SOMETHING) }
            )
            Spacer(modifier = Modifier.height(AppDimens.paddingExtraLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppDimens.iconSmall))
                Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                Text(stringResource(R.string.role_footer), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
        }
    }
}

@Composable
fun RoleCard(title: String, subtitle: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusExtraLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(shape = RoundedCornerShape(AppDimens.radiusLarge), color = MaterialTheme.colorScheme.surface) {
                    Box(modifier = Modifier.padding(AppDimens.paddingMedium)) { icon() }
                }
                Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}