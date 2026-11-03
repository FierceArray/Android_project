package com.findme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.findme.ui.theme.AppDimens
import com.findme.ui.theme.AppFont
import com.findme.ui.theme.FindMeTheme
import com.findme.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    settings: SettingsUiState,
    onAction: (MainAction) -> Unit,
    onOpenSystemLanguageSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAction(MainAction.CloseSettings) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
        }

        // Выбор шрифта приложения: Open Sans, Montserrat, Lato, Inter
        SettingsSectionCard(
            title = stringResource(R.string.settings_font_title),
            subtitle = stringResource(R.string.settings_font_desc),
            icon = {
                Icon(
                    imageVector = Icons.Default.FontDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                FilterChip(
                    selected = settings.appFont == AppFont.OPEN_SANS,
                    onClick = { onAction(MainAction.ChangeFont(AppFont.OPEN_SANS)) },
                    label = { Text(stringResource(R.string.font_open_sans)) }
                )
                FilterChip(
                    selected = settings.appFont == AppFont.MONTSERRAT,
                    onClick = { onAction(MainAction.ChangeFont(AppFont.MONTSERRAT)) },
                    label = { Text(stringResource(R.string.font_montserrat)) }
                )
                FilterChip(
                    selected = settings.appFont == AppFont.LATO,
                    onClick = { onAction(MainAction.ChangeFont(AppFont.LATO)) },
                    label = { Text(stringResource(R.string.font_lato)) }
                )
                FilterChip(
                    selected = settings.appFont == AppFont.INTER,
                    onClick = { onAction(MainAction.ChangeFont(AppFont.INTER)) },
                    label = { Text(stringResource(R.string.font_inter)) }
                )
            }
        }

        // Выбор темы
        SettingsSectionCard(
            title = stringResource(R.string.settings_theme_title),
            subtitle = stringResource(R.string.settings_theme_desc),
            icon = {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                FilterChip(
                    selected = settings.themeMode == ThemeMode.SYSTEM,
                    onClick = { onAction(MainAction.ChangeTheme(ThemeMode.SYSTEM)) },
                    label = { Text(stringResource(R.string.theme_system)) }
                )
                FilterChip(
                    selected = settings.themeMode == ThemeMode.LIGHT,
                    onClick = { onAction(MainAction.ChangeTheme(ThemeMode.LIGHT)) },
                    label = { Text(stringResource(R.string.theme_light)) }
                )
                FilterChip(
                    selected = settings.themeMode == ThemeMode.DARK,
                    onClick = { onAction(MainAction.ChangeTheme(ThemeMode.DARK)) },
                    label = { Text(stringResource(R.string.theme_dark)) }
                )
            }
        }

        // Язык интерфейса
        SettingsSectionCard(
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_desc),
            icon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Button(
                onClick = onOpenSystemLanguageSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_language_button))
            }
        }

        // Уведомления
        SettingsSectionCard(
            title = stringResource(R.string.settings_notif_title),
            subtitle = stringResource(R.string.settings_notif_desc),
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            SettingsCheckboxRow(
                title = stringResource(R.string.settings_notif_toggle),
                checked = settings.notificationsEnabled,
                onCheckedChange = { onAction(MainAction.ToggleNotifications(it)) }
            )
        }

        // Конфиденциальность
        SettingsSectionCard(
            title = stringResource(R.string.settings_privacy_title),
            subtitle = stringResource(R.string.settings_privacy_desc),
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)) {
                SettingsCheckboxRow(
                    title = stringResource(R.string.settings_privacy_hide_seen),
                    checked = settings.hideLastSeen,
                    onCheckedChange = { onAction(MainAction.ToggleHideLastSeen(it)) }
                )
                SettingsCheckboxRow(
                    title = stringResource(R.string.settings_privacy_auto_delete),
                    checked = settings.autoDeleteMessages,
                    onCheckedChange = { onAction(MainAction.ToggleAutoDeleteMessages(it)) }
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Button(
                    onClick = { onAction(MainAction.OpenBlacklistClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                    Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                    Text(
                        text = stringResource(R.string.settings_privacy_blacklist),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
            content()
        }
    }
}

@Composable
fun SettingsCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .semantics(mergeDescendants = true) { role = Role.Checkbox }
            .padding(vertical = AppDimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Preview(showBackground = true, name = "SettingsScreen Preview")
@Composable
private fun SettingsScreenPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            SettingsScreen(
                settings = SettingsUiState(),
                onAction = {},
                onOpenSystemLanguageSettings = {}
            )
        }
    }
}