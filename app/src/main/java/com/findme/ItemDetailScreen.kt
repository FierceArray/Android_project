package com.findme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.findme.ui.theme.AppDimens

@Composable
fun ItemDetailScreen(
    item: LostItem,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contactButtonText = if (item.type == ItemType.FOUND) {
        stringResource(R.string.action_contact_finder)
    } else {
        stringResource(R.string.action_contact_owner)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.paddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.cd_share),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(AppDimens.paddingLarge)) {
                Text(
                    text = stringResource(R.string.detail_id_format, item.id),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = item.deepLink,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = "${item.location} • ${item.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
                Button(
                    onClick = onContactClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = contactButtonText,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}