package com.github.libretube.test.ui.sheets

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.github.libretube.test.R

@Composable
fun RenamePlaylistSheet(
    currentName: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    TextFieldSheet(
        title = stringResource(R.string.renamePlaylist),
        hint = stringResource(R.string.playlistName),
        initialValue = currentName,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

@Composable
fun DeletePlaylistConfirmationSheet(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmationSheet(
        title = stringResource(R.string.deletePlaylist),
        message = stringResource(R.string.areYouSure),
        confirmText = stringResource(R.string.yes),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

@Composable
fun EditPlaylistDescriptionSheet(
    currentDescription: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    TextFieldSheet(
        title = stringResource(R.string.change_playlist_description),
        hint = stringResource(R.string.playlist_description),
        initialValue = currentDescription,
        singleLine = false,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}
