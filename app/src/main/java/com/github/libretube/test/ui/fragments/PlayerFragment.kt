package com.github.libretube.test.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.github.libretube.test.api.JsonHelper
import com.github.libretube.test.api.obj.Streams
import com.github.libretube.test.constants.IntentData
import com.github.libretube.test.helpers.BackgroundHelper
import com.github.libretube.test.helpers.NavigationHelper
import com.github.libretube.test.parcelable.PlayerData
import com.github.libretube.test.services.AbstractPlayerService
import com.github.libretube.test.services.OnlinePlayerService
import com.github.libretube.test.ui.models.PlayerViewModel
import com.github.libretube.test.ui.screens.PlayerScreen
import com.github.libretube.test.util.PlayingQueue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf
import com.github.libretube.test.enums.ShareObjectType
import com.github.libretube.test.extensions.toID
import com.github.libretube.test.helpers.DownloadHelper
import com.github.libretube.test.obj.ShareData
import com.github.libretube.test.ui.dialogs.AddToPlaylistDialog
import com.github.libretube.test.ui.dialogs.ShareDialog
import com.github.libretube.test.ui.models.PlayerCommandEvent

import android.view.KeyEvent
import com.github.libretube.test.ui.interfaces.OnlinePlayerOptions

class PlayerFragment : Fragment(), OnlinePlayerOptions {

    private val viewModel: PlayerViewModel by activityViewModels() // Use activityViewModels to share with Controls if needed, but plan said viewModels? Plan said bridge.
    // Actually, Controls are inside Screen which gets VM passed.
    // Use activityViewModels to ensure persistence if fragment is recreated? Or viewModels?
    // PlayerFragment is retained? No.
    // Let's stick to viewModels() as per original file, but logic says activity might be better if we want to share with other fragments?
    // Original had: private val viewModel: PlayerViewModel by viewModels()
    // I'll stick to original.
    // Edit: Wait, I modified PlayerViewModel to hold 'playerController'. If PlayerFragment dies and comes back, we lose controller reference if it's in VM?
    // VM survives config changes.
    // PlayerFragment instance changes.
    // 'playerController' in VM is a var.
    // It's safer to manage controller in Fragment and update VM.
    
    // Actually, I'll use the same delegation as before.
    // private val viewModel: PlayerViewModel by viewModels()
    // But since I changed PlayerViewModel to use StateFlows, let's just use it.

    private lateinit var playerController: MediaController
    private var videoId: String = ""
    private var streams: Streams? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateVMState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateVMState()
            viewModel.updateBufferingState(playbackState == Player.STATE_BUFFERING)
            if (playbackState == Player.STATE_ENDED) {
                // Handle autoplay or end
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
            val maybeStreams: Streams? = mediaMetadata.extras?.getString(IntentData.streams)?.let {
                JsonHelper.json.decodeFromString(it)
            }
            maybeStreams?.let { 
                streams = it
                viewModel.updateMetadata(
                    title = it.title ?: "",
                    uploader = it.uploader ?: "",
                    uploaderAvatar = it.uploaderAvatar,
                    description = it.description ?: "",
                    views = it.views,
                    likes = it.likes,
                    subscriberCount = it.uploaderSubscriberCount
                )
                viewModel.updateChapters(it.chapters)
                viewModel.updateRelatedVideos(it.relatedStreams)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PlayerScreen(
                    playerViewModel = viewModel,
                    onClose = {
                        // Kill fragment
                        parentFragmentManager.beginTransaction().remove(this@PlayerFragment).commitAllowingStateLoss()
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val playerData = requireArguments().getParcelable<PlayerData>(IntentData.playerData)!!
        videoId = playerData.videoId
        
        // Start Service Binding
        val createNewSession = !requireArguments().getBoolean(IntentData.alreadyStarted)
        requireArguments().putBoolean(IntentData.alreadyStarted, true)
        
        attachToPlayerService(playerData, createNewSession)

        // Polling for position update
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (::playerController.isInitialized && playerController.isPlaying) {
                    updateVMState()
                }
                delay(1000)
            }
        }
        
        // Observe Play Video Trigger
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playVideoTrigger.collect { streamItem ->
                streamItem.url?.let { playNextVideo(it) }
            }
        }

        // Observe Player Commands
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playerCommandTrigger.collect { command ->
                val currentStream = viewModel.currentStream.value ?: return@collect
                val videoId = currentStream.url?.toID() ?: return@collect
                
                when (command) {
                    PlayerCommandEvent.Share -> {
                        val bundle = bundleOf(
                            IntentData.id to videoId,
                            IntentData.shareObjectType to ShareObjectType.VIDEO,
                            IntentData.shareData to ShareData(currentVideo = currentStream.title)
                        )
                        ShareDialog().apply { arguments = bundle }.show(parentFragmentManager, ShareDialog::class.java.name)
                    }
                    PlayerCommandEvent.Download -> {
                        DownloadHelper.startDownloadDialog(requireContext(), parentFragmentManager, videoId)
                    }
                    PlayerCommandEvent.SaveToPlaylist -> {
                        AddToPlaylistDialog().apply {
                            arguments = bundleOf(IntentData.videoInfo to currentStream)
                        }.show(parentFragmentManager, AddToPlaylistDialog::class.java.name)
                    }
                    PlayerCommandEvent.Subscribe -> {
                        // TODO: Implement Subscribe logic (usually requires uploaderUrl or similar)
                    }
                }
            }
        }
    }

    private fun attachToPlayerService(playerData: PlayerData, startNewSession: Boolean, audioOnly: Boolean = false) {
        BackgroundHelper.startMediaService(
            requireContext(),
            OnlinePlayerService::class.java,
            if (startNewSession) bundleOf(
                IntentData.playerData to playerData,
                IntentData.audioOnly to audioOnly
            ) else Bundle.EMPTY,
        ) { controller ->
            playerController = controller
            viewModel.setPlayerController(controller) // Inject into VM
            
            playerController.addListener(playerListener)
            
            // Initial State Update
            updateVMState()

            if (!startNewSession) {
                val streamsJson = playerController.mediaMetadata.extras?.getString(IntentData.streams)
                val restoredStreams = streamsJson?.let { JsonHelper.json.decodeFromString<Streams>(it) }
                if (restoredStreams != null) {
                    streams = restoredStreams
                    viewModel.updateMetadata(restoredStreams.title ?: "", restoredStreams.uploader ?: "")
                    viewModel.updateChapters(restoredStreams.chapters)
                }
            }
        }
    }
    
    private fun updateVMState() {
        if (::playerController.isInitialized) {
            viewModel.updatePlaybackState(
                isPlaying = playerController.isPlaying,
                position = playerController.currentPosition,
                duration = playerController.duration.coerceAtMost(Long.MAX_VALUE).coerceAtLeast(0) // Fix potential negative duration from ExoPlayer
            )
            viewModel.updateBufferingState(playerController.playbackState == Player.STATE_BUFFERING)
        }
    }
    
    override fun onDestroy() {
        if (::playerController.isInitialized) {
            playerController.removeListener(playerListener)
            playerController.release()
        }
        viewModel.setPlayerController(null)
        super.onDestroy()
    }

    fun onKeyUp(keyCode: Int, event: KeyEvent? = null): Boolean {
        return false
    }

    fun toggleFullscreen(isFullscreen: Boolean) {
        // TODO: Implement Fullscreen toggle
    }

    override fun exitFullscreen() {
        // TODO: Exit fullscreen
    }

    override fun onCaptionsClicked() {
        // TODO: Show captions dialog
    }

    override fun onQualityClicked() {
        // TODO: Show quality dialog
    }

    override fun onAudioStreamClicked() {
        // TODO: Show audio tracks
    }

    override fun onStatsClicked() {
        // TODO: Show stats
    }

    fun playNextVideo(videoId: String) {
        val playerData = PlayerData(videoId = videoId)
        attachToPlayerService(playerData, startNewSession = true)
    }

    fun switchToAudioMode() {
        if (videoId.isNotEmpty()) {
            val playerData = PlayerData(videoId = videoId)
            attachToPlayerService(playerData, startNewSession = true, audioOnly = true)
        }
    }

    fun maximize() {
        viewModel.triggerPlayerExpansion()
    }

    fun minimize() {
        viewModel.triggerPlayerCollapse()
    }
}
