package com.prush.justanotherplayer.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileNotFoundException
import com.bumptech.glide.request.RequestOptions

fun getAlbumArtUri(context: Context, albumId: Long): Uri {

    val contentUri =
        ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)

    val cursor = context.contentResolver
        .query(
            contentUri,
            arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
            null,
            null,
            null
        )

    if (cursor != null) {
        try {
            if (cursor.moveToFirst()) {
                val file =
                    File(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)))
                if (file.exists()) {
                    try {
                        return Uri.fromFile(file)
                    } catch (ignored: FileNotFoundException) {
                    }
                }
            }
        } catch (ignored: NullPointerException) {
        } finally {
            cursor.close()
        }
    }

    return Uri.EMPTY
}

interface OnBitmapLoadedListener {
    fun onBitmapLoaded(resource: Bitmap)
    fun onBitmapLoadingFailed()
}

fun loadAlbumArt(context: Context, contentId: Long, imageView: ImageView, errorResource: Int = -1) {

    GlideApp.with(context)
        .asBitmap()
        .load(getAlbumArtUri(context, contentId))
//        .override(200, 200)
        .error(errorResource)
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
                // called when imageView is cleared. If you are referencing the bitmap
                // somewhere else too other than this imageView clear it here
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                imageView.setImageBitmap(resource)
            }

        })
}

fun loadAlbumArt(
    context: Context,
    contentId: Long,
    onBitmapLoadedListener: OnBitmapLoadedListener
) {

    GlideApp.with(context)
        .asBitmap()
        .load(getAlbumArtUri(context, contentId))
        .apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                onBitmapLoadedListener.onBitmapLoaded(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                onBitmapLoadedListener.onBitmapLoadingFailed()
            }

        })
}

fun loadAlbumArt(
    context: Context,
    resourceId: Int,
    onBitmapLoadedListener: OnBitmapLoadedListener
) {
    GlideApp.with(context)
        .asBitmap()
        .load(resourceId)
        .apply(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                onBitmapLoadedListener.onBitmapLoaded(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                onBitmapLoadedListener.onBitmapLoadingFailed()
            }

        })
}