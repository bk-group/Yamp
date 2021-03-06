package com.prush.justanotherplayer.ui.trackslibrary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Util
import com.prush.justanotherplayer.base.BaseRecyclerFragment
import com.prush.justanotherplayer.base.ListPresenter
import com.prush.justanotherplayer.base.RecyclerAdapter
import com.prush.justanotherplayer.di.Injection
import com.prush.justanotherplayer.model.Track
import com.prush.justanotherplayer.services.AudioPlayerService
import com.prush.justanotherplayer.utils.*
import kotlinx.android.synthetic.main.base_recylerview_layout.*

private val TAG = TracksLibraryFragment::class.java.name

class TracksLibraryFragment : BaseRecyclerFragment(), TracksContract.View,
    RecyclerAdapter.OnItemInteractedListener {

    private lateinit var tracksPresenter: TracksContract.Presenter
    private lateinit var listPresenter: ListPresenter<Track>

    private lateinit var adapter: RecyclerAdapter<Track>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Inject these guys
        tracksPresenter = TracksPresenter(Injection.provideTrackRepository(), this)
        listPresenter = TracksListPresenter()

        adapter = RecyclerAdapter(listPresenter, this)
        baseRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        tracksPresenter.loadLibraryTracks()
    }

    override fun onItemClick(selectedTrackPosition: Int) {
        if (selectedTrackPosition == -1) {
            tracksPresenter.shuffleTrackPlayback()
        } else {
            tracksPresenter.prepareTrackPlayback(selectedTrackPosition)
        }
    }

    override fun onContextMenuClicked(position: Int) {
        super.onContextMenuClicked(position)
        tracksPresenter.prepareTrackContextMenu(position)
    }

    override fun displayLibraryTracks(trackList: MutableList<Track>) {
        Log.d(TAG, "Loaded some tracks")
        listPresenter.setItemsList(trackList, adapter)
    }

    override fun startTrackPlayback(selectedTrackPosition: Int, tracksList: MutableList<Track>) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(PLAY_CONTEXT_TYPE, PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
    }

    override fun startShufflePlayback(tracksList: MutableList<Track>) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(SHUFFLE_TRACKS, true)
        intent.putExtra(PLAY_CONTEXT_TYPE, PLAY_CONTEXT.LIBRARY_TRACKS)
        Util.startForegroundService(getViewActivity(), intent)
    }

    override fun displayEmptyLibrary() {
        showEmptyLibrary()
    }

    override fun onDestroy() {
        super.onDestroy()
        tracksPresenter.onCleanup()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    companion object {
        fun newInstance() = TracksLibraryFragment()
    }
}