package com.findme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.findme.ui.theme.AppDimens
import com.findme.ui.theme.FindMeTheme

@Composable
fun ProfileScreen(
    profile: ProfileUiState,
    onAction: (MainAction) -> Unit,
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
            IconButton(onClick = { onAction(MainAction.CloseProfile) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            IconButton(onClick = { onAction(MainAction.OpenSettings) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(AppDimens.iconEmptyState)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(AppDimens.iconTouchTarget),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.onSurface) {
                        Text(
                            text = "⭐ ${profile.trustRating}",
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@${profile.nickname}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.profile_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!profile.isEditing) {
                        TextButton(onClick = { onAction(MainAction.StartEditProfile) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimens.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(AppDimens.paddingSmall))
                            Text(stringResource(R.string.action_edit_profile))
                        }
                    }
                }

                AnimatedVisibility(visible = !profile.isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)) {
                        ProfileInfoRow(
                            icon = Icons.Default.Badge,
                            label = stringResource(R.string.profile_field_name),
                            value = profile.fullName
                        )
                        ProfileInfoRow(
                            icon = Icons.Default.AlternateEmail,
                            label = stringResource(R.string.profile_field_nickname),
                            value = "@${profile.nickname}"
                        )
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.profile_field_email),
                            value = profile.email
                        )
                    }
                }

                AnimatedVisibility(visible = profile.isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)) {
                        OutlinedTextField(
                            value = profile.draftName,
                            onValueChange = { onAction(MainAction.UpdateDraftName(it)) },
                            label = { Text(stringResource(R.string.profile_field_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.radiusMedium)
                        )
                        OutlinedTextField(
                            value = profile.draftNickname,
                            onValueChange = { onAction(MainAction.UpdateDraftNickname(it)) },
                            label = { Text(stringResource(R.string.profile_field_nickname)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.radiusMedium)
                        )
                        OutlinedTextField(
                            value = profile.draftEmail,
                            onValueChange = { onAction(MainAction.UpdateDraftEmail(it)) },
                            label = { Text(stringResource(R.string.profile_field_email)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppDimens.radiusMedium)
                        )
                        Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
                        ) {
                            OutlinedButton(
                                onClick = { onAction(MainAction.CancelEditProfile) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                            Button(
                                onClick = { onAction(MainAction.SaveProfile) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.action_save),
                                    color = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
                Text(
                    text = stringResource(R.string.profile_stats_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(
                        value = profile.returnedCount.toString(),
                        label = stringResource(R.string.profile_stat_returned)
                    )
                    ProfileStatItem(
                        value = profile.lostReportsCount.toString(),
                        label = stringResource(R.string.profile_stat_lost)
                    )
                    ProfileStatItem(
                        value = profile.trustPoints.toString(),
                        label = stringResource(R.string.profile_stat_points)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppDimens.radiusLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                Text(
                    text = stringResource(R.string.profile_achievements_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_achievements_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

                AchievementRow(
                    title = stringResource(R.string.ach_sherlock_title),
                    description = stringResource(R.string.ach_sherlock_desc),
                    icon = Icons.Default.Search,
                    unlocked = true
                )
                AchievementRow(
                    title = stringResource(R.string.ach_verified_title),
                    description = stringResource(R.string.ach_verified_desc),
                    icon = Icons.Default.VerifiedUser,
                    unlocked = true
                )
                AchievementRow(
                    title = stringResource(R.string.ach_loser_title),
                    description = stringResource(R.string.ach_loser_desc),
                    icon = Icons.Default.EmojiEvents,
                    unlocked = false
                )
                AchievementRow(
                    title = stringResource(R.string.ach_legend_title),
                    description = stringResource(R.string.ach_legend_desc),
                    icon = Icons.Default.Star,
                    unlocked = false
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileStatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AchievementRow(
    title: String,
    description: String,
    icon: ImageVector,
    unlocked: Boolean
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        color = if (unlocked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimens.iconTouchTarget)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.paddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "ProfileScreen Preview")
@Composable
private fun ProfileScreenPreview() {
    FindMeTheme(dynamicColor = false) {
        Surface {
            ProfileScreen(
                profile = ProfileUiState(),
                onAction = {}
            )
        }
    }
}