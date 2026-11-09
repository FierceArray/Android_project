package com.findme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.findme.ui.theme.AppDimens

@Composable
fun ItemDetailScreen(
    item: LostItem,
    foldPosture: FoldPosture = FoldPosture.NORMAL,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onContactClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete_item),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isMine) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.detail_my_badge)) }
                    )
                    Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.cd_share),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = foldPosture == FoldPosture.TABLETOP),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(AppDimens.radiusLarge)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimens.paddingLarge)
            ) {
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
                    text = item.location,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                Text(
                    text = stringResource(R.string.detail_created_at, item.formattedCreatedTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.formattedUpdatedTime?.let { updatedTime ->
                    Text(
                        text = stringResource(R.string.detail_updated_at, updatedTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item.changeNote?.let { note ->
                    Text(
                        text = stringResource(R.string.detail_change_note, note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.detail_exact_millis, item.createdAtMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (item.photoAttachment != PhotoAttachment.NONE) {
                    Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
                    PhotoAttachmentBanner(photoAttachment = item.photoAttachment)
                }

                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (foldPosture == FoldPosture.TABLETOP) {
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
        } else {
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
        ) {
            if (item.isMine) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                        Text(stringResource(R.string.action_edit_item))
                    }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                        Text(stringResource(R.string.action_delete_item))
                    }
                }
            }

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

@Composable
private fun PhotoAttachmentBanner(photoAttachment: PhotoAttachment) {
    val (icon, labelRes) = when (photoAttachment) {
        PhotoAttachment.PHONE_PHOTO -> Icons.Default.PhoneAndroid to R.string.photo_phone
        PhotoAttachment.PET_PHOTO -> Icons.Default.Pets to R.string.photo_pet
        PhotoAttachment.DOCS_PHOTO -> Icons.Default.Description to R.string.photo_docs
        PhotoAttachment.KEYS_PHOTO -> Icons.Default.Key to R.string.photo_keys
        PhotoAttachment.NONE -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.radiusMedium))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(AppDimens.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AppDimens.iconTouchTarget)
        )
        Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
        Text(
            text = stringResource(R.string.detail_photo_attached, stringResource(labelRes)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}