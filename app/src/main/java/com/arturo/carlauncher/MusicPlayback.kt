package com.arturo.carlauncher

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

object MusicPlayback {

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val mainExecutor = Executor { command ->
        Handler(Looper.getMainLooper()).post(command)
    }

    fun connect(
        context: Context,
        onConnected: (() -> Unit)? = null
    ) {

        if (controller != null) {
            onConnected?.invoke()
            return
        }

        if (controllerFuture != null) {
            controllerFuture?.addListener({
                onConnected?.invoke()
            }, mainExecutor)
            return
        }

        val token = SessionToken(
            context.applicationContext,
            ComponentName(
                context.applicationContext,
                CarMusicService::class.java
            )
        )

        val future = MediaController.Builder(
            context.applicationContext,
            token
        ).buildAsync()

        controllerFuture = future

        future.addListener({

            try {
                controller = future.get()
                onConnected?.invoke()

            } catch (e: Exception) {
                controllerFuture = null
                controller = null
            }

        }, mainExecutor)
    }

    fun playSongs(
        context: Context,
        songs: List<Song>,
        selectedIndex: Int
    ) {

        if (songs.isEmpty()) return

        connect(context) {

            val items = songs.map { song ->

                MediaItem.Builder()
                    .setUri(song.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .build()
                    )
                    .build()
            }

            val safeIndex = selectedIndex.coerceIn(
                0,
                items.lastIndex
            )

            controller?.apply {
                setMediaItems(
                    items,
                    safeIndex,
                    C.TIME_UNSET
                )
                prepare()
                play()
            }
        }
    }

    fun toggle() {
        controller?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun isPlaying(): Boolean {
        return controller?.isPlaying == true
    }

    fun currentTitle(): String {
        return controller?.mediaMetadata?.title?.toString()
            ?: "Sin canción"
    }

    fun currentArtist(): String {
        return controller?.mediaMetadata?.artist?.toString()
            ?: "Artista desconocido"
    }

    fun isConnected(): Boolean {
        return controller != null
    }
}
