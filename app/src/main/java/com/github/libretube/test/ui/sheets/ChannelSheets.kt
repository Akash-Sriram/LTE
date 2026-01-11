package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.libretube.test.R
import com.github.libretube.test.api.SubscriptionHelper
import com.github.libretube.test.api.obj.Subscription
import com.github.libretube.test.db.DatabaseHolder
import com.github.libretube.test.db.obj.SubscriptionGroup
import com.github.libretube.test.extensions.toID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelOptionsSheet(
    channelId: String,
    channelName: String?,
    isSubscribed: Boolean,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onAddToGroupClick: () -> Unit,
    onPlayLatestClick: () -> Unit,
    onPlayBackgroundClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            channelName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.share)) },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                modifier = Modifier.clickable { 
                    onDismissRequest()
                    onShareClick()
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.play_latest_videos)) },
                leadingContent = { Icon(painterResource(R.drawable.ic_play), contentDescription = null) },
                modifier = Modifier.clickable { 
                    onDismissRequest()
                    onPlayLatestClick()
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.playOnBackground)) },
                leadingContent = { Icon(painterResource(R.drawable.ic_play), contentDescription = null) },
                modifier = Modifier.clickable { 
                    onDismissRequest()
                    onPlayBackgroundClick()
                }
            )

            if (isSubscribed) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.add_to_group)) },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        onDismissRequest()
                        onAddToGroupClick()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChannelToGroupSheet(
    channelId: String,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<SubscriptionGroup>>(emptyList()) }
    var selectedGroups by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val allGroups = DatabaseHolder.Database.subscriptionGroupsDao().getAll().sortedBy { it.index }
            groups = allGroups
            selectedGroups = allGroups.filter { it.channels.contains(channelId) }.map { it.name }.toSet()
            isLoading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_to_group),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(groups) { group ->
                        val isChecked = selectedGroups.contains(group.name)
                        ListItem(
                            headlineContent = { Text(group.name) },
                            trailingContent = {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedGroups = if (checked) {
                                            selectedGroups + group.name
                                        } else {
                                            selectedGroups - group.name
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                val checked = !isChecked
                                selectedGroups = if (checked) {
                                    selectedGroups + group.name
                                } else {
                                    selectedGroups - group.name
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val updatedGroups = groups.map { group ->
                                if (selectedGroups.contains(group.name)) {
                                    if (!group.channels.contains(channelId)) {
                                        group.copy(channels = group.channels + channelId)
                                    } else group
                                } else {
                                    if (group.channels.contains(channelId)) {
                                        group.copy(channels = group.channels - channelId)
                                    } else group
                                }
                            }
                            DatabaseHolder.Database.subscriptionGroupsDao().updateAll(updatedGroups)
                            withContext(Dispatchers.Main) {
                                onDismissRequest()
                            }
                        }
                    }) {
                        Text(stringResource(R.string.okay))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelGroupsSheet(
    onDismissRequest: () -> Unit,
    onEditGroup: (SubscriptionGroup?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<SubscriptionGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            groups = DatabaseHolder.Database.subscriptionGroupsDao().getAll().sortedBy { it.index }
            isLoading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.channel_groups),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { onEditGroup(null) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Group")
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(groups) { group ->
                        ListItem(
                            headlineContent = { Text(group.name) },
                            supportingContent = { Text("${group.channels.size} channels") },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        val index = groups.indexOf(group)
                                        if (index > 0) {
                                            val newGroups = groups.toMutableList()
                                            val prev = newGroups[index - 1]
                                            newGroups[index - 1] = group
                                            newGroups[index] = prev
                                            groups = newGroups
                                        }
                                    }) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                    }
                                    IconButton(onClick = {
                                        val index = groups.indexOf(group)
                                        if (index < groups.size - 1) {
                                            val newGroups = groups.toMutableList()
                                            val next = newGroups[index + 1]
                                            newGroups[index + 1] = group
                                            newGroups[index] = next
                                            groups = newGroups
                                        }
                                    }) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                    }
                                    IconButton(onClick = { onEditGroup(group) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            DatabaseHolder.Database.subscriptionGroupsDao().deleteGroup(group.name)
                                            groups = groups.filter { it.name != group.name }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            groups.forEachIndexed { index, group -> group.index = index }
                            DatabaseHolder.Database.subscriptionGroupsDao().updateAll(groups)
                            withContext(Dispatchers.Main) {
                                onDismissRequest()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.okay))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChannelGroupSheet(
    groupToEdit: SubscriptionGroup?,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var groupName by remember { mutableStateOf(groupToEdit?.name ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    var subscriptions by remember { mutableStateOf<List<Subscription>>(emptyList()) }
    var selectedChannelIds by remember { mutableStateOf(groupToEdit?.channels?.toSet() ?: emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            subscriptions = SubscriptionHelper.getSubscriptions()
            isLoading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = if (groupToEdit == null) "New Group" else "Edit Group",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Channels") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredSubs = remember(subscriptions, searchQuery) {
                    subscriptions.filter { it.name.lowercase().contains(searchQuery.lowercase()) }
                }

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(filteredSubs) { sub ->
                        val subId = sub.url.toID()
                        val isChecked = selectedChannelIds.contains(subId)
                        ListItem(
                            headlineContent = { Text(sub.name) },
                            leadingContent = {
                                AsyncImage(
                                    model = sub.avatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedChannelIds = if (checked) {
                                            selectedChannelIds + subId
                                        } else {
                                            selectedChannelIds - subId
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                val checked = !isChecked
                                selectedChannelIds = if (checked) {
                                    selectedChannelIds + subId
                                } else {
                                    selectedChannelIds - subId
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (groupName.isBlank() || selectedChannelIds.isEmpty()) return@Button
                            scope.launch(Dispatchers.IO) {
                                val newGroup = SubscriptionGroup(
                                    name = groupName,
                                    channels = selectedChannelIds.toMutableList(),
                                    index = groupToEdit?.index ?: 0
                                )
                                // If editing and name changed, we need to delete old one first
                                if (groupToEdit != null && groupToEdit.name != groupName) {
                                    DatabaseHolder.Database.subscriptionGroupsDao().deleteGroup(groupToEdit.name)
                                }
                                DatabaseHolder.Database.subscriptionGroupsDao().createGroup(newGroup)
                                withContext(Dispatchers.Main) {
                                    onDismissRequest()
                                }
                            }
                        },
                        enabled = groupName.isNotBlank() && selectedChannelIds.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.okay))
                    }
                }
            }
        }
    }
}
