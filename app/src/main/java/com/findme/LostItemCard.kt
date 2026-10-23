package com.findme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.findme.ui.theme.AppDimens

@Composable
fun LostItemCard(
    item: LostItem,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deepLink = "findme://app/lost/${item.id}"
    val shareMessage = stringResource(R.string.share_message, deepLink)
    val toastChatMsg = stringResource(R.string.toast_chat)
    Card(
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { role = Role.Button },
        shape = RoundedCornerShape(AppDimens.radiusLarge), // ИСПОЛЬЗУЕМ ТОКЕН
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(AppDimens.iconTouchTarget) // 48.dp
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.iconType) {
                                IconCategory.ELECTRONICS -> Icons.Default.PhoneAndroid
                                IconCategory.PET -> Icons.Default.Pets
                                IconCategory.DOCUMENT -> Icons.Default.Description
                                else -> Icons.Default.QuestionMark
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
                    Column {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.cd_share),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.iconSmall),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
            Button(
                onClick = {
                    android.widget.Toast.makeText(context, toastChatMsg, android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.action_contact),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}