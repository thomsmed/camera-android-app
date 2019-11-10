package com.thomsmed.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class ContentUriService(private val context: Context) {

    private val authority = "com.thomsmed.camera.FileProvider"

    var contentAbsolutePath: String? = null
        private set

    fun createExternalMediaStoreContentUri(title: String, mimeType: String): Uri? {
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.ImageColumns.TITLE, title)
            put(MediaStore.Images.ImageColumns.MIME_TYPE, mimeType)
        })
    }

    fun createExternalAppStorageContentUri(filePrefix: String, fileSuffix: String): Uri? {
        val imagesDir = File(context.getExternalFilesDir(null), "images").apply {
            if (!exists()) {
                mkdir()
            }
        }
        val newFile = File.createTempFile(filePrefix, fileSuffix, imagesDir).also { contentAbsolutePath = it.absolutePath }
        return createContentUri(newFile)
    }

    fun createInternalAppStorageContentUri(filePrefix: String, fileSuffix: String): Uri? {
        val imagesDir = File(context.filesDir, "images").apply {
            if (!exists()) {
                mkdir()
            }
        }
        val newFile = File.createTempFile(filePrefix, fileSuffix, imagesDir).also { contentAbsolutePath = it.absolutePath }
        return createContentUri(newFile)
    }

    fun createExternalCacheContentUri(filePrefix: String, fileSuffix: String): Uri? {
        val newFile = File.createTempFile(filePrefix, fileSuffix, context.externalCacheDir).also { contentAbsolutePath = it.absolutePath }
        return createContentUri(newFile)
    }

    fun createInternalCacheContentUri(filePrefix: String, fileSuffix: String): Uri? {
        val newFile = File.createTempFile(filePrefix, fileSuffix, context.cacheDir).also { contentAbsolutePath = it.absolutePath }
        return createContentUri(newFile)
    }

    private fun createContentUri(file: File): Uri? {
        return try {
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}