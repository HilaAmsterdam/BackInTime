package com.example.backintime.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.example.backintime.R

class CloudinaryHelper(private val context: Context) {

    companion object {
        private var isInitialized = false
    }

    init {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to context.getString(R.string.cloud_name),
                "api_key" to context.getString(R.string.api_key),
                "api_secret" to context.getString(R.string.api_secret)
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val filePath = getRealPathFromURI(context, imageUri)
            ?: return onFailure("Error obtaining image file path")

        MediaManager.get().upload(imageUri)
            .option("folder", "BackInTime_Images")
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("url").toString()
                    onSuccess(imageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onFailure(error?.description ?: "Unknown error uploading image")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    onFailure("Image upload rescheduled: ${error?.description ?: "Unknown error"}")
                }
            })
            .dispatch()
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        // Try to get the real file path from the contentUri
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(android.provider.MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    return it.getString(columnIndex)
                }
            }
        }
        return contentUri.path
    }
}
