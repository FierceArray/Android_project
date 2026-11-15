package com.findme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.findme.ui.theme.AppDimens

class EditItemViewModel(
    private val savedState: SavedStateHandle
) : ViewModel() {

    val initialized = savedState.getStateFlow("initialized", false)
    val itemTypeName = savedState.getStateFlow("type", ItemType.LOST.name)
    val categoryTitle = savedState.getStateFlow("title", "")
    val location = savedState.getStateFlow("location", "")
    val description = savedState.getStateFlow("description", "")
    val changeNote = savedState.getStateFlow("changeNote", "")
    val iconCategoryName = savedState.getStateFlow("iconCategory", IconCategory.ELECTRONICS.name)
    val photoAttachmentName = savedState.getStateFlow("photo", PhotoAttachment.NONE.name)

    private var initialSnapshot: String = ""

    fun initIfNeeded(itemId: String?, defaultTypeSegment: String) {
        val existing = itemId?.let { LostItemsRepository.getItemById(it) }
        val defaultType = if (defaultTypeSegment == ItemType.FOUND.pathSegment) {
            ItemType.FOUND
        } else {
            ItemType.LOST
        }

        if (!initialized.value) {
            if (existing != null) {
                savedState["type"] = existing.type.name
                savedState["title"] = existing.category
                savedState["location"] = existing.location
                savedState["description"] = existing.description
                savedState["changeNote"] = existing.changeNote ?: ""
                savedState["iconCategory"] = existing.iconType.name
                savedState["photo"] = existing.photoAttachment.name
            } else {
                savedState["type"] = defaultType.name
                savedState["title"] = ""
                savedState["location"] = ""
                savedState["description"] = ""
                savedState["changeNote"] = ""
                savedState["iconCategory"] = IconCategory.ELECTRONICS.name
                savedState["photo"] = PhotoAttachment.NONE.name
            }
            savedState["initialized"] = true
        }

        initialSnapshot = if (existing != null) {
            "${existing.type.name}|${existing.category}|${existing.location}|${existing.description}|${existing.iconType.name}|${existing.photoAttachment.name}"
        } else {
            "${defaultType.name}||||${IconCategory.ELECTRONICS.name}|${PhotoAttachment.NONE.name}"
        }
    }

    fun hasUnsavedChanges(): Boolean {
        val current = "${itemTypeName.value}|${categoryTitle.value}|${location.value}|${description.value}|${iconCategoryName.value}|${photoAttachmentName.value}"
        return current != initialSnapshot || changeNote.value.isNotBlank()
    }

    fun onTypeChange(type: ItemType) {
        savedState["type"] = type.name
    }

    fun onTitleChange(value: String) {
        savedState["title"] = value
    }

    fun onLocationChange(value: String) {
        savedState["location"] = value
    }

    fun onDescriptionChange(value: String) {
        savedState["description"] = value
    }

    fun onChangeNoteChange(value: String) {
        savedState["changeNote"] = value
    }

    fun onIconCategoryChange(category: IconCategory) {
        savedState["iconCategory"] = category.name
    }

    fun onPhotoChange(photo: PhotoAttachment) {
        savedState["photo"] = photo.name
    }
}

@Composable
fun EditItemRouteScreen(
    itemId: String?,
    defaultTypeSegment: String,
    foldPosture: FoldPosture,
    onSaved: (LostItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vmKey = itemId ?: "create_$defaultTypeSegment"
    val vm: EditItemViewModel = viewModel(key = vmKey)

    LaunchedEffect(itemId, defaultTypeSegment) {
        vm.initIfNeeded(itemId, defaultTypeSegment)
    }

    val typeName by vm.itemTypeName.collectAsStateWithLifecycle()
    val title by vm.categoryTitle.collectAsStateWithLifecycle()
    val location by vm.location.collectAsStateWithLifecycle()
    val description by vm.description.collectAsStateWithLifecycle()
    val changeNote by vm.changeNote.collectAsStateWithLifecycle()
    val iconCategoryName by vm.iconCategoryName.collectAsStateWithLifecycle()
    val photoName by vm.photoAttachmentName.collectAsStateWithLifecycle()

    val itemType = runCatching { ItemType.valueOf(typeName) }.getOrDefault(ItemType.LOST)
    val iconCategory = runCatching { IconCategory.valueOf(iconCategoryName) }.getOrDefault(IconCategory.ELECTRONICS)
    val photoAttachment = runCatching { PhotoAttachment.valueOf(photoName) }.getOrDefault(PhotoAttachment.NONE)

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val attemptClose = {
        if (vm.hasUnsavedChanges()) {
            showExitDialog = true
        } else {
            onCancel()
        }
    }

    BackHandler(enabled = vm.hasUnsavedChanges()) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.dialog_exit_title)) },
            text = { Text(stringResource(R.string.dialog_exit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onCancel()
                    }
                ) {
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.action_stay))
                }
            }
        )
    }

    val isEditMode = itemId != null
    val canSave = title.isNotBlank() && location.isNotBlank() && description.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.paddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = attemptClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (isEditMode) {
                    stringResource(R.string.edit_title_update)
                } else {
                    stringResource(R.string.edit_title_create)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDimens.paddingMedium)
        ) {
            if (!isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
                ) {
                    FilterChip(
                        selected = itemType == ItemType.LOST,
                        onClick = { vm.onTypeChange(ItemType.LOST) },
                        label = { Text(stringResource(R.string.edit_type_lost)) }
                    )
                    FilterChip(
                        selected = itemType == ItemType.FOUND,
                        onClick = { vm.onTypeChange(ItemType.FOUND) },
                        label = { Text(stringResource(R.string.edit_type_found)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                FilterChip(
                    selected = iconCategory == IconCategory.ELECTRONICS,
                    onClick = { vm.onIconCategoryChange(IconCategory.ELECTRONICS) },
                    label = { Text(stringResource(R.string.filter_electronics)) }
                )
                FilterChip(
                    selected = iconCategory == IconCategory.PET,
                    onClick = { vm.onIconCategoryChange(IconCategory.PET) },
                    label = { Text(stringResource(R.string.filter_pets)) }
                )
                FilterChip(
                    selected = iconCategory == IconCategory.DOCUMENT,
                    onClick = { vm.onIconCategoryChange(IconCategory.DOCUMENT) },
                    label = { Text(stringResource(R.string.filter_documents)) }
                )
                FilterChip(
                    selected = iconCategory == IconCategory.OTHER,
                    onClick = { vm.onIconCategoryChange(IconCategory.OTHER) },
                    label = { Text(stringResource(R.string.filter_other)) }
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = vm::onTitleChange,
                label = { Text(stringResource(R.string.edit_field_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMedium)
            )

            OutlinedTextField(
                value = location,
                onValueChange = vm::onLocationChange,
                label = { Text(stringResource(R.string.edit_field_location)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMedium)
            )

            OutlinedTextField(
                value = description,
                onValueChange = vm::onDescriptionChange,
                label = { Text(stringResource(R.string.edit_field_description)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppDimens.radiusMedium)
            )

            if (isEditMode) {
                OutlinedTextField(
                    value = changeNote,
                    onValueChange = vm::onChangeNoteChange,
                    label = { Text(stringResource(R.string.edit_field_change_note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.radiusMedium)
                )
            }

            Text(
                text = stringResource(R.string.edit_photo_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
            ) {
                PhotoOptionCard(
                    label = stringResource(R.string.photo_none),
                    icon = Icons.Default.HideImage,
                    selected = photoAttachment == PhotoAttachment.NONE,
                    onClick = { vm.onPhotoChange(PhotoAttachment.NONE) }
                )
                PhotoOptionCard(
                    label = stringResource(R.string.photo_phone),
                    icon = Icons.Default.PhoneAndroid,
                    selected = photoAttachment == PhotoAttachment.PHONE_PHOTO,
                    onClick = { vm.onPhotoChange(PhotoAttachment.PHONE_PHOTO) }
                )
                PhotoOptionCard(
                    label = stringResource(R.string.photo_pet),
                    icon = Icons.Default.Pets,
                    selected = photoAttachment == PhotoAttachment.PET_PHOTO,
                    onClick = { vm.onPhotoChange(PhotoAttachment.PET_PHOTO) }
                )
                PhotoOptionCard(
                    label = stringResource(R.string.photo_docs),
                    icon = Icons.Default.Description,
                    selected = photoAttachment == PhotoAttachment.DOCS_PHOTO,
                    onClick = { vm.onPhotoChange(PhotoAttachment.DOCS_PHOTO) }
                )
                PhotoOptionCard(
                    label = stringResource(R.string.photo_keys),
                    icon = Icons.Default.Key,
                    selected = photoAttachment == PhotoAttachment.KEYS_PHOTO,
                    onClick = { vm.onPhotoChange(PhotoAttachment.KEYS_PHOTO) }
                )
            }
        }

        if (foldPosture == FoldPosture.TABLETOP) {
            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))
        } else {
            Spacer(modifier = Modifier.height(AppDimens.paddingMedium))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.paddingSmall)
        ) {
            OutlinedButton(
                onClick = attemptClose,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = {
                    val saved = LostItemsRepository.saveItem(
                        existingId = itemId,
                        type = itemType,
                        categoryTitle = title,
                        description = description,
                        location = location,
                        iconType = iconCategory,
                        photoAttachment = photoAttachment,
                        changeNote = changeNote
                    )
                    onSaved(saved)
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@Composable
private fun PhotoOptionCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(AppDimens.radiusMedium),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier
            .clip(RoundedCornerShape(AppDimens.radiusMedium))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { role = Role.RadioButton }
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimens.iconTouchTarget)
                    .clip(RoundedCornerShape(AppDimens.radiusSmall))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(AppDimens.paddingSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}