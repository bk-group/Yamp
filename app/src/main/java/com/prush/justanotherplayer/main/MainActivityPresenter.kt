package com.prush.justanotherplayer.main

import android.util.Log
import com.prush.justanotherplayer.repositories.ITrackRepository
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private val TAG = MainActivityPresenter::class.java.name

class MainActivityPresenter(
    private val view: MainContract.View,
    private val trackRepository: ITrackRepository
) : MainContract.Presenter, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = (Dispatchers.IO + job)


    override fun fetchTrackMetadata(trackId: Long) {

        Log.d(TAG, "fetchTrackMetadata $trackId")

        // fetch metadata of currently playing track
        launch {
            try {
                val track = trackRepository.getTrackById(view.getContext(), trackId)
                withContext(Dispatchers.Main) {
                    view.showNowPlayingTrackMetadata(track)
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
                Log.d(TAG, "Exception: ${e.message}")
                view.displayError()
            }
        }
    }

    override fun onCleanup() {
        job.cancel()
    }
}
