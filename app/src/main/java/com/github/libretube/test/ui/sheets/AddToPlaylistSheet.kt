package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.api.obj.Playlists

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    playlists: List<Playlists>,
    lastSelectedId: String?,
    onCreatePlaylistClick: () -> Unit,
    onAddClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember(playlists, lastSelectedId) {
        mutableIntStateOf(
            playlists.indexOfFirst { it.id == lastSelectedId }.takeIf { it >= 0 } ?: 0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Add to Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = playlists.getOrNull(selectedIndex)?.name ?: "No playlists",
                onValueChange = {},
                readOnly = true,
                label = { Text("Playlist") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = playlists.isNotEmpty()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                playlists.forEachIndexed { index, playlist ->
                    DropdownMenuItem(
                        text = { Text(playlist.name ?: "") },
                        onClick = {
                            selectedIndex = index
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCreatePlaylistClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New")
            }

            Button(
                onClick = { onAddClick(selectedIndex) },
                modifier = Modifier.weight(1f),
                enabled = playlists.isNotEmpty()
            ) {
                Text("Add")
            }
        }
    }
}
