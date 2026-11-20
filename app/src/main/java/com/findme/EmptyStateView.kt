package com.findme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.findme.ui.theme.AppDimens

@Composable
fun EmptyStateView(
    isSearchResultEmpty: Boolean = false,
    hasActiveFilters: Boolean = false,
    onResetFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = if (isSearchResultEmpty) Icons.Default.Search else Icons.Default.Inbox
    val titleRes = if (isSearchResultEmpty) R.string.empty_search_title else R.string.empty_list_title
    val subtitleRes = if (isSearchResultEmpty) R.string.empty_search_subtitle else R.string.empty_list_subtitle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppDimens.paddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.iconEmptyState),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isSearchResultEmpty || hasActiveFilters) {
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))

            Button(onClick = onResetFilters) {
                Text(stringResource(R.string.action_reset_filters))
            }
        }
    }
}