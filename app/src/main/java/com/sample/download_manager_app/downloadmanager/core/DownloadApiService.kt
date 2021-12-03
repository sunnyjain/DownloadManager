package com.sample.download_manager_app.downloadmanager.core

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody


import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File

interface DownloadApiService {
    @Streaming
    @GET
    suspend fun getUrl(@Url url: String): ResponseBody
}

sealed class Download {
    data class Initialize(val filename: String) : Download()
    data class Started(val percent: Int) : Download()
    data class Progress(val percent: Int, val size: Long) : Download()
    data class Finished(val file: File) : Download()
    data class Error(val error: Exception) : Download()
}

fun ResponseBody.downloadToFileWithProgress(context: Context, filename: String,
                                            percent: Int): Flow<Download> =
    flow {

        // flag to delete file if download errors or is cancelled
        var deleteFile = true
        var isDownloadResuming = false
        var fileName = filename
        if(!fileName.split(".").contains(contentType()?.subtype())) {
            fileName = "${filename}.${contentType()?.subtype()}"
        } else {
            isDownloadResuming = true
        }
        val file = File(context.filesDir, fileName)
        emit(Download.Progress(percent, file.length()))

        if(!isDownloadResuming) {
            file.createNewFile()
            emit(Download.Initialize(file.name))
        }

        try {
            if(isDownloadResuming){
                Log.e("##DM", "skipping".plus("\t").plus(file.length()))
                source().skip(file.length())
            }
            byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val data = ByteArray(8_192)
                    var progressBytes = if(file.length() > 0L) file.length() else 0L
                    emit(Download.Started(percent))
                    Log.e("##DM", "data - ".plus(progressBytes).plus("\t").plus(totalBytes))
                    while (true) {
                        val bytes = inputStream.read(data)

                        if (bytes == -1) {
                            break
                        }

                        outputStream.channel
                        outputStream.write(data, 0, bytes)
                        progressBytes += bytes

                        emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes).toInt(),
                            size = progressBytes))
                    }

                    when {
                        progressBytes < totalBytes ->
                            emit(Download.Error(Exception("missing bytes")))
                        progressBytes > totalBytes ->
                            emit(Download.Error(Exception("too many bytes")))
                        else -> {
                            inputStream.close()
                            outputStream.close()
                            deleteFile = false
                        }
                    }
                }
            }

            emit(Download.Finished(file))
        } catch (ex: Exception) {
            Log.e("##DM", ex.message.toString())
        } finally {
            // check if download was successful
            if (deleteFile) {
                file.delete()
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()