package com.example.okhttpdownload

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var job: Job
    lateinit var networkRequestScope: CoroutineScope
    lateinit var downloadProgressBar: ProgressBar
    lateinit var downloadProgressText: TextView
    val downloadLink = "https://drive.google.com/uc?export=download&id=1XSTImggtvJ8oZYIB8sEByMuZipiabCaS"
    val savedFileName = "test_picture1.zip"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job = Job()
        networkRequestScope = CoroutineScope(job)


    downloadProgressBar = findViewById(R.id.downloadProgressBar)
    downloadProgressText = findViewById(R.id.downloadProgressText)


    val progressListener = object: OkhttpDownload.ProgressListener{
        override fun update(bytesRead: Long, totalSize: Long, done: Boolean){
            runOnUiThread {
                if(done){
                    downloadProgressBar.progress = 100
                    downloadProgressText.text = "100%"
                }else {
                    val percentage = (((bytesRead * 100) / totalSize))
                    downloadProgressBar.progress = percentage.toInt()
                    downloadProgressText.text = "${percentage.toInt()}%"
                }
            }
        }
    }


    val mapDirectory = File(applicationContext.cacheDir, "picture")
    if( !mapDirectory.exists())
    mapDirectory.mkdir()
    val savePath = File(mapDirectory, savedFileName)


    val okHttpDownloadButton = findViewById(R.id.downloadButton) as Button
    okHttpDownloadButton.setOnClickListener{
        Log.d(TAG, "click okHttpDownload")
        val downloadwithProgress = OkhttpDownload(networkRequestScope, progressListener)
        try{
            downloadwithProgress.download(downloadLink, savePath)
        }catch (ex: Exception){
            Log.d(TAG, "fail to download", ex)
        }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


}