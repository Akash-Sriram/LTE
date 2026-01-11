package com.github.libretube.test.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.libretube.test.obj.BottomSheetItem

@Composable
fun GenericBottomSheetScreen(
    title: String?,
    items: List<BottomSheetItem>,
    onItemClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LazyColumn {
            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(index) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    if (item.drawable != null && item.drawable != 0) {
                        Icon(
                            painter = painterResource(item.drawable),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else if (item.iconDrawable != null) {
                        Icon(
                            painter = rememberAsyncImagePainter(item.iconDrawable),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            tint = androidx.compose.ui.graphics.Color.Unspecified
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
