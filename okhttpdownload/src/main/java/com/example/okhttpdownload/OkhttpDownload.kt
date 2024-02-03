package com.example.okhttpdownload

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Okio
import okio.Source
import okhttp3.Interceptor
import okhttp3.Response
import okio.buffer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.jvm.Throws

class OkhttpDownload(val scope: CoroutineScope, val progressListener: ProgressListener?) {

    val TAG = "DownloadWithProgress"

    @Throws(Exception::class)
    fun download(downloadLink: String, savePath: File) {
        val request = Request.Builder()
            .url(downloadLink)
            .build()

        val client = OkHttpClient.Builder()
            .build()

        scope.launch {
            client.newCall(request).execute().use {
                if (it.isSuccessful) {
                    writeResponseBodyToStorage(it.body!!, savePath)
                } else
                    throw IOException("unexpected code " + it)
            }
        }
    }


    suspend fun writeResponseBodyToStorage(body: ResponseBody, path: File){

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val fileSize = body.contentLength()
        var byteDownloaded: Long = 0


        // Log.d(TAG, "file will save to ${path.absoluteFile} with size $fileSize")
        try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(path)
            val buffer = ByteArray(4 * 1024)
            while(true){
                val byteRead = inputStream.read(buffer)
                if(byteRead < 0) {
                    progressListener?.update(byteDownloaded, fileSize, true)
                    break
                }else {
                    outputStream.write(buffer, 0, byteRead)
                    byteDownloaded += byteRead
                    progressListener?.update(byteDownloaded, fileSize, byteRead.equals(-1))
                }

            }
            outputStream.flush()
            Log.d(TAG, "download completed. file is saved to ${path.absoluteFile}")
        } catch (ex: IOException) {
            Log.e(TAG, "fail to save file", ex)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }



    }

    interface ProgressListener{
        fun update(bytesRead: Long, totalSize: Long, done: Boolean)
    }


}