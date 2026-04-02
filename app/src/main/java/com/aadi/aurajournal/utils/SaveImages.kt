package com.aadi.aurajournal.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

fun copyUriToInternalStorage(context: Context,uri: Uri): String?{
    return try {
        val inputStream=context.contentResolver.openInputStream(uri)?:return null
        val fileName="journal_img_${UUID.randomUUID()}.jpg"
        val file= File(context.filesDir,fileName)
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        file.absolutePath
    }
    catch (e: Exception){
        e.printStackTrace()
        null
    }
}