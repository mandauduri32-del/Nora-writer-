package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    fun saveImagePermanently(
        context: Context,
        uri: Uri,
        subDirName: String,
        fileName: String
    ): String? {
        return try {
            val dir = File(context.filesDir, subDirName)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val destinationFile = File(dir, fileName)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                destinationFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFileIfExists(path: String): Boolean {
        return try {
            if (path.isNotBlank()) {
                val f = File(path)
                if (f.exists()) f.delete() else true
            } else true
        } catch (e: Exception) {
            false
        }
    }
}
