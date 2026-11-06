package com.arturo.carlauncher

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val duration: Long,
    val albumId: Long,
    val folder: String
)

object MusicLibrary {

    fun getSongs(context: Context): List<Song> {

        val songs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.RELATIVE_PATH
        )

        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder =
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {

            val volumes = if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.getExternalVolumeNames(context)
            } else {
                setOf("external")
            }

            for (volume in volumes) {

                val collection = if (Build.VERSION.SDK_INT >= 29) {
                    MediaStore.Audio.Media.getContentUri(volume)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->

                    val idColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media._ID
                    )

                    val titleColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.TITLE
                    )

                    val artistColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.ARTIST
                    )

                    val albumColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.ALBUM
                    )

                    val durationColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.DURATION
                    )

                    val albumIdColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.ALBUM_ID
                    )

                    val folderColumn = cursor.getColumnIndex(
                        MediaStore.Audio.Media.RELATIVE_PATH
                    )

                    while (cursor.moveToNext()) {

                        val id = cursor.getLong(idColumn)

                        val title = cursor.getString(titleColumn)
                            ?: "Canción desconocida"

                        val artist = cursor.getString(artistColumn)
                            ?: "Artista desconocido"

                        val album = cursor.getString(albumColumn)
                            ?: "Álbum desconocido"

                        val duration = cursor.getLong(durationColumn)

                        val albumId = cursor.getLong(albumIdColumn)

                        val folder = if (folderColumn >= 0) {
                            cursor.getString(folderColumn) ?: "Música"
                        } else {
                            "Música"
                        }

                        val songUri = ContentUris.withAppendedId(
                            collection,
                            id
                        )

                        songs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                album = album,
                                uri = songUri,
                                duration = duration,
                                albumId = albumId,
                                folder = folder
                            )
                        )
                    }
                }
            }

        } catch (e: SecurityException) {
            return emptyList()
        } catch (e: Exception) {
            return emptyList()
        }

        return songs.distinctBy { it.uri }
            .sortedBy { it.title.lowercase() }
    }
}
