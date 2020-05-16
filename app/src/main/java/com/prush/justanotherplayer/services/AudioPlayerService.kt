package com.prush.justanotherplayer.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.prush.justanotherplayer.audioplayer.AudioPlayer
import com.prush.justanotherplayer.audioplayer.ExoPlayer
import com.prush.justanotherplayer.di.Injection
import com.prush.justanotherplayer.mediautils.NotificationManager
import com.prush.justanotherplayer.model.Track
import com.prush.justanotherplayer.queue.NowPlayingInfo
import com.prush.justanotherplayer.queue.NowPlayingQueue
import com.prush.justanotherplayer.repositories.ITrackRepository
import com.prush.justanotherplayer.utils.SELECTED_TRACK
import com.prush.justanotherplayer.utils.SELECTED_TRACK_POSITION
import com.prush.justanotherplayer.utils.SHUFFLE_TRACKS
import com.prush.justanotherplayer.utils.TRACKS_LIST

private val TAG: String = AudioPlayerService::class.java.name

class AudioPlayerService : Service(), NotificationManager.OnNotificationPostedListener, Player.EventListener {

    private lateinit var audioPlayer: AudioPlayer
    private lateinit var tracksRepository: ITrackRepository

    override fun onBind(intent: Intent?): IBinder? {
        return AudioServiceBinder()
    }

    inner class AudioServiceBinder : Binder() {

        fun getPlayerInstance(): SimpleExoPlayer {
            return (audioPlayer as ExoPlayer).simpleExoPlayer
        }

        fun getNowPlayingQueue(): NowPlayingQueue {
            return (audioPlayer as ExoPlayer).nowPlayingQueue
        }
    }

    override fun onCreate() {
        super.onCreate()

        audioPlayer = Injection.provideAudioPlayer()
        tracksRepository = Injection.provideTrackRepository()

        audioPlayer.apply {
            init(this@AudioPlayerService)
            setNotificationPostedListener(this@AudioPlayerService)
            setPlayerEventListener(this@AudioPlayerService)
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "Received ${intent?.action}")

        if (intent == null || intent.action == null)
            return START_STICKY

        when (intent.action) {
            PlaybackControls.PLAY.name -> {

                val selectedTrackPosition = intent.getIntExtra(SELECTED_TRACK_POSITION, -1)
                val shuffle = intent.getBooleanExtra(SHUFFLE_TRACKS, false)
                val tracksList: ArrayList<Track> =
                    intent.getSerializableExtra(TRACKS_LIST) as ArrayList<Track>

                if (shuffle) {
                    audioPlayer.shufflePlayTracks(this, tracksList)
                } else {
                    audioPlayer.playTracks(this, tracksList, selectedTrackPosition)
                }
            }

            PlaybackControls.SHUFFLE_OFF.name -> {

                audioPlayer.setShuffleMode(this, false)
            }

            PlaybackControls.SHUFFLE_ON.name -> {

                audioPlayer.setShuffleMode(this, true)
            }

            PlaybackControls.RE_ORDER.name -> {

                val tracksList: ArrayList<Track> = ArrayList()

                @Suppress("UNCHECKED_CAST")
                tracksList.addAll(intent.getSerializableExtra(TRACKS_LIST) as ArrayList<Track>)

                audioPlayer.updateTracks(this, tracksList)
            }
            PlaybackControls.ADD_TO_QUEUE.name,
            PlaybackControls.PLAY_NEXT.name -> {

                val track = intent.getSerializableExtra(SELECTED_TRACK) as Track
                val playbackStarted =
                    (audioPlayer as ExoPlayer).nowPlayingQueue.nowPlayingTracksList.isNotEmpty()

                if (playbackStarted) {

                    if (intent.action == PlaybackControls.ADD_TO_QUEUE.name)
                        audioPlayer.addTrackToQueue(this, track)
                    else
                        audioPlayer.playNext(this, track)

                } else {

                    //playback is not initiated

                    val tracksList: ArrayList<Track> = ArrayList()

                    @Suppress("UNCHECKED_CAST")
                    tracksList.add(track)
                    audioPlayer.playTracks(this, tracksList, 0)
                }
            }
        }

        return START_STICKY
    }

    override fun onNotificationPosted(notificationId: Int, notification: Notification?) {
        startForeground(notificationId, notification)
    }

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        stopSelf()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            PlaybackState.STATE_PLAYING, PlaybackState.STATE_PAUSED -> {

                updateNowPlaying()
            }
            PlaybackState.STATE_STOPPED ->{

                stopForeground(true)
                stopSelf()
            }
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {

        updateNowPlaying()
    }

    private fun updateNowPlaying() {
            if ((audioPlayer as ExoPlayer).simpleExoPlayer.currentTag != null) {
                val nowPlayingInfo: NowPlayingInfo = (audioPlayer as ExoPlayer).simpleExoPlayer.currentTag as NowPlayingInfo

                (audioPlayer as ExoPlayer).nowPlayingQueue.apply {
                    currentPlayingTrackId = nowPlayingInfo.id
                    nowPlayingTracksList.forEachIndexed { index, track ->
                        if (track.id == currentPlayingTrackId) {
                            currentPlayingTrackIndex = index
                            return@forEachIndexed
                        }
                    }
                }

        }

    }

    override fun onDestroy() {
        super.onDestroy()

        audioPlayer.cleanup()
    }

    enum class PlaybackControls {
        PLAY,
        SHUFFLE_ON,
        SHUFFLE_OFF,
        RE_ORDER,
        ADD_TO_QUEUE,
        PLAY_NEXT
    }

}