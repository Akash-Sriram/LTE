package com.github.libretube.test.db.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.libretube.test.api.obj.StreamItem
import com.github.libretube.test.extensions.toMillis
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "watchHistoryItem",
    indices = [
        androidx.room.Index(value = ["watchedAt"])
    ]
)
data class WatchHistoryItem(
    @PrimaryKey val videoId: String = "",
    @ColumnInfo var title: String? = null,
    @ColumnInfo val uploadDate: LocalDate? = null,
    @ColumnInfo val uploader: String? = null,
    @ColumnInfo val uploaderUrl: String? = null,
    @ColumnInfo var uploaderAvatar: String? = null,
    @ColumnInfo var thumbnailUrl: String? = null,
    @ColumnInfo var duration: Long? = null,
    @ColumnInfo val isShort: Boolean = false,
    @ColumnInfo var watchedAt: Long = 0,
    @ColumnInfo var position: Long = 0
) {
    val isLive get() = duration?.let { it <= 0L } ?: true

    fun toStreamItem() = StreamItem(
        url = videoId,
        type = StreamItem.TYPE_STREAM,
        title = title,
        thumbnail = thumbnailUrl,
        uploaderName = uploader,
        uploaded = uploadDate?.toMillis() ?: 0,
        uploadedDate = uploadDate?.toString(),
        uploaderAvatar = uploaderAvatar,
        uploaderUrl = uploaderUrl,
        duration = duration,
        isShort = isShort
    )
}

