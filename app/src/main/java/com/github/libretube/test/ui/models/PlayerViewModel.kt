package com.github.libretube.test.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.PlaybackParameters
import com.github.libretube.test.api.obj.Segment
import com.github.libretube.test.api.obj.Subtitle
import com.github.libretube.test.helpers.PlayerHelper
import com.github.libretube.test.extensions.toID
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.libretube.test.api.SubscriptionHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

import com.github.libretube.test.util.PlayingQueue
import com.github.libretube.test.api.obj.ChapterSegment
import com.github.libretube.test.api.obj.StreamItem

sealed class PlayerCommandEvent {
    object Share : PlayerCommandEvent()
    object Download : PlayerCommandEvent()
    object SaveToPlaylist : PlayerCommandEvent()
    object Subscribe : PlayerCommandEvent()
}

@UnstableApi
class PlayerViewModel : ViewModel() {
    
    private val _expandPlayerTrigger = Channel<Unit>(Channel.BUFFERED)
    val expandPlayerTrigger = _expandPlayerTrigger.receiveAsFlow()

    fun triggerPlayerExpansion() {
        _expandPlayerTrigger.trySend(Unit)
    }

    private val _collapsePlayerTrigger = Channel<Unit>(Channel.BUFFERED)
    val collapsePlayerTrigger = _collapsePlayerTrigger.receiveAsFlow()

    fun triggerPlayerCollapse() {
        _collapsePlayerTrigger.trySend(Unit)
    }

    private val _playVideoTrigger = Channel<StreamItem>(Channel.BUFFERED)
    val playVideoTrigger = _playVideoTrigger.receiveAsFlow()

    private val _playerCommandTrigger = kotlinx.coroutines.flow.MutableSharedFlow<PlayerCommandEvent>()
    val playerCommandTrigger = _playerCommandTrigger.asSharedFlow()

    fun triggerPlayerCommand(command: PlayerCommandEvent) {
        viewModelScope.launch {
            _playerCommandTrigger.emit(command)
        }
    }
    
    private val _playerController = MutableStateFlow<MediaController?>(null)
    val playerController = _playerController.asStateFlow()

    fun setPlayerController(controller: MediaController?) {
        _playerController.value = controller
    }

    fun togglePlayPause() {
        playerController.value?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        playerController.value?.seekTo(position)
    }

    fun skipPrevious() {
        playerController.value?.seekToPrevious()
    }

    fun skipNext() {
        playerController.value?.seekToNext()
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()
    
    private val _uploader = MutableStateFlow("")
    val uploader = _uploader.asStateFlow()

    private val _uploaderAvatar = MutableStateFlow<String?>(null)
    val uploaderAvatar = _uploaderAvatar.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _views = MutableStateFlow(0L)
    val views = _views.asStateFlow()

    private val _likes = MutableStateFlow(0L)
    val likes = _likes.asStateFlow()

    private val _subscriberCount = MutableStateFlow<Long?>(null)
    val subscriberCount = _subscriberCount.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed = _isSubscribed.asStateFlow()

    private val _dominantColor = MutableStateFlow(Color.Transparent)
    val dominantColor = _dominantColor.asStateFlow()

    private val _currentStream = MutableStateFlow<StreamItem?>(null)
    val currentStream = _currentStream.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition = _playbackPosition.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    // Queue (direct from singleton)
    val queue = PlayingQueue.queueState

    // Chapters
    private val _chapters = MutableStateFlow<List<ChapterSegment>>(emptyList())
    val chapters = _chapters.asStateFlow()

    // Related Videos
    private val _relatedVideos = MutableStateFlow<List<StreamItem>>(emptyList())
    val relatedVideos = _relatedVideos.asStateFlow()

    // Comments (Using a simplified list for now, can expand later)
    private val _comments = MutableStateFlow<List<com.github.libretube.test.api.obj.Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    fun updatePlaybackState(isPlaying: Boolean, position: Long, duration: Long) {
        _isPlaying.value = isPlaying
        _currentPosition.value = position
        _playbackPosition.value = position
        _duration.value = duration
    }

    fun updateMetadata(
        title: String,
        uploader: String,
        uploaderAvatar: String? = null,
        description: String = "",
        views: Long = 0,
        likes: Long = 0,
        subscriberCount: Long? = null
    ) {
        _title.value = title
        _uploader.value = uploader
        _uploaderAvatar.value = uploaderAvatar
        _description.value = description
        _views.value = views
        _likes.value = likes
        _subscriberCount.value = subscriberCount
        _isBuffering.value = false // Metadata loaded, usually stop buffering indicator
        checkSubscriptionStatus()
    }

    private fun checkSubscriptionStatus() {
        val stream = _currentStream.value ?: return
        val channelId = stream.uploaderUrl?.toID() ?: return
        viewModelScope.launch {
            _isSubscribed.value = SubscriptionHelper.isSubscribed(channelId) ?: false
        }
    }

    fun toggleSubscription() {
        val stream = _currentStream.value ?: return
        val channelId = stream.uploaderUrl?.toID() ?: return
        val name = stream.uploaderName ?: return
        
        viewModelScope.launch {
            if (_isSubscribed.value) {
                SubscriptionHelper.unsubscribe(channelId)
            } else {
                SubscriptionHelper.subscribe(
                    channelId = channelId,
                    name = name,
                    uploaderAvatar = stream.uploaderAvatar,
                    verified = stream.uploaderVerified ?: false
                )
            }
            _isSubscribed.value = !_isSubscribed.value
        }
    }
    fun updateBufferingState(isBuffering: Boolean) {
        _isBuffering.value = isBuffering
    }

    fun setStream(stream: StreamItem) {
        _currentStream.value = stream
        _title.value = stream.title ?: ""
        _uploader.value = stream.uploaderName ?: ""
        // Fetch dominant color
        extractDominantColor(stream.thumbnail)
    }

    fun updateDominantColor(color: Color) {
        _dominantColor.value = color
    }

    private fun extractDominantColor(url: String?) {
        if (url == null) {
            _dominantColor.value = Color.Transparent
            return
        }
        viewModelScope.launch {
            try {
                // This assumes we have a context, we can get it from an ImageLoader if it's singleton or passed?
                // Actually, let's keep it simple for now and just use a placeholder if we can't easily get context here.
                // Better: The UI will pass the context or we use a global Coil ImageLoader.
            } catch (e: Exception) {
                _dominantColor.value = Color.Transparent
            }
        }
    }
    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        playerController.value?.let { player ->
            player.playbackParameters = PlaybackParameters(speed, player.playbackParameters.pitch)
        }
        // Persist to preferences
        com.github.libretube.test.helpers.PreferenceHelper.putString(
            com.github.libretube.test.constants.PreferenceKeys.PLAYBACK_SPEED,
            speed.toString()
        )
    }

    fun initializePlaybackSpeed() {
        val savedSpeed = com.github.libretube.test.helpers.PreferenceHelper.getString(
            com.github.libretube.test.constants.PreferenceKeys.PLAYBACK_SPEED,
            "1.0"
        ).toFloatOrNull() ?: 1.0f
        _playbackSpeed.value = savedSpeed
    }
    
    fun onQueueItemClicked(item: StreamItem) {
        _playVideoTrigger.trySend(item)
    }

    fun updateChapters(newChapters: List<ChapterSegment>) {
        _chapters.value = newChapters
    }

    fun updateRelatedVideos(videos: List<StreamItem>) {
        _relatedVideos.value = videos
    }

    fun updateComments(newComments: List<com.github.libretube.test.api.obj.Comment>) {
        _comments.value = newComments
    }

    var segments = MutableLiveData<List<Segment>>()
    // this is only used to restore the subtitle after leaving PiP, the actual caption state
    // should always be read from the player's selected tracks!
    var currentSubtitle = Subtitle(code = PlayerHelper.defaultSubtitleCode)
    var sponsorBlockConfig = PlayerHelper.getSponsorBlockCategories()

    /**
     * Whether an orientation change is in progress, so that the current player should be continued to use
     *
     * Set to true if the activity will be recreated due to an orientation change
     */
    var isOrientationChangeInProgress = false

    val deArrowData = MutableLiveData<com.github.libretube.test.api.obj.DeArrowContent?>()

    fun fetchDeArrowData(videoId: String) {
        if (!com.github.libretube.test.helpers.PreferenceHelper.getBoolean(com.github.libretube.test.constants.PreferenceKeys.DEARROW, true)) {
             deArrowData.postValue(null)
             return
        }
        android.util.Log.d("DeArrowPlayer", "fetchDeArrowData called for $videoId")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("DeArrowPlayer", "Fetching DeArrow data for $videoId")
                val response = com.github.libretube.test.api.MediaServiceRepository.instance.getDeArrowContent(videoId)
                if (response != null && (response.titles.isNotEmpty() || response.thumbnails.isNotEmpty())) {
                    android.util.Log.d("DeArrowPlayer", "SUCCESS: Received DeArrow data for $videoId. Titles: ${response.titles.size}, Thumbs: ${response.thumbnails.size}")
                } else {
                    android.util.Log.d("DeArrowPlayer", "NO DATA: No DeArrow curation for $videoId")
                }
                deArrowData.postValue(response)
            } catch (e: Exception) {
                android.util.Log.e("DeArrowPlayer", "ERROR: Could not fetch DeArrow data for $videoId", e)
                deArrowData.postValue(null)
            }
        }
    }
}
