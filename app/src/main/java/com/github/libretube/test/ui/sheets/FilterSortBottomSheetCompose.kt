package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.R
import com.github.libretube.test.enums.ContentFilter
import com.github.libretube.test.ui.models.SubscriptionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortBottomSheetCompose(
    selectedSortOrder: Int,
    onSortOrderChange: (Int) -> Unit,
    hideWatched: Boolean,
    onHideWatchedChange: (Boolean) -> Unit,
    showUpcoming: Boolean,
    onShowUpcomingChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            val sortOptions = listOf(
                R.string.most_recent to "Most Recent",
                R.string.least_recent to "Least Recent",
                R.string.most_views to "Most Views",
                R.string.least_views to "Least Views",
                R.string.sort_by to "Channel A-Z",  // Using generic sort_by for now
                R.string.sort_by to "Channel Z-A"   // Using generic sort_by for now
            )
            
            sortOptions.forEachIndexed { index, (stringRes, _) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortOrderChange(index) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSortOrder == index,
                        onClick = { onSortOrderChange(index) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(stringRes))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.tooltip_filter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Content Type Filters (Videos, Shorts, Livestreams)
            ContentFilter.entries.forEach { filter ->
                var isEnabled by remember { mutableStateOf(filter.isEnabled) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            isEnabled = !isEnabled
                            filter.isEnabled = isEnabled
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { 
                            isEnabled = it
                            filter.isEnabled = it
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (filter) {
                            ContentFilter.VIDEOS -> stringResource(R.string.videos)
                            ContentFilter.SHORTS -> stringResource(R.string.yt_shorts)
                            ContentFilter.LIVESTREAMS -> stringResource(R.string.livestreams)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hide Watched / Show Upcoming
            FilterCheckboxRow(
                text = stringResource(R.string.hide_watched_from_feed),
                checked = hideWatched,
                onCheckedChange = onHideWatchedChange
            )

            FilterCheckboxRow(
                text = stringResource(R.string.show_upcoming_videos),
                checked = showUpcoming,
                onCheckedChange = onShowUpcomingChange
            )
        }
    }
}

@Composable
private fun FilterCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text)
    }
}
