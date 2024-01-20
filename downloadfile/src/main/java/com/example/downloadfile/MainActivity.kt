package com.example.downloadfile

import android.app.DownloadManager

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log


import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import java.io.File
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class MainActivity : AppCompatActivity() {
    val TAG = "Download Main"
    val downloadLink = "https://drive.google.com/uc?export=download&id=1XSTImggtvJ8oZYIB8sEByMuZipiabCaS" //file size ~23MB
    val publicDownloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) as File
    val fileName = "photo.zip"
    val subDirectory = "testDownload"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val downloadButton: Button = findViewById(R.id.downloadManagerButton) as Button
        downloadButton.setOnClickListener {
            Log.d(TAG, "start download manager")
            startDownloadWithDownloadManager()
        }
    }


    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        intentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            registerReceiver(downloadListener, intentFilter, Context.RECEIVER_EXPORTED)
        else
            registerReceiver(downloadListener, intentFilter)
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadListener)
    }


    fun startDownloadWithDownloadManager(){
        val saveFileName = File(publicDownloadsDirectory, subDirectory + "/" + fileName)
        val downloadRequest = DownloadManager.Request(Uri.parse(downloadLink))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) //set whether Download Notification is visible
            .setTitle(fileName)             // Title of the DownloadNotifications
            .setDescription("Downloading")  // Description of the Download Notification
            .setRequiresCharging(false)     //set if the device is required to be charging to begin download
            .setAllowedOverMetered(false)    //set whether download can proceed over Metered network
            .setAllowedOverRoaming(false)    //set whether download can proceed over Roaming connection
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)   //set download can proceed over which network type
            //.setDestinationUri(Uri.fromFile(saveFileName))  //set the save location of the downloaded file, included filename
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/testFolder/photo.zip")

        Log.d(TAG, "start download file and save as " + saveFileName.absolutePath)
        val downloadManager:DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(downloadRequest)

    }

    val downloadListener = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    Log.d(TAG, "ACTION_DOWNLOAD_COMPLETE")
                }
                DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                    Log.d(TAG, "ACTION_NOTIFICATION_CLICKED")
                }
                DownloadManager.ACTION_VIEW_DOWNLOADS -> {
                    Log.d(TAG, "ACTION_VIEW_DOWNLOADS")
                }

            }

            if(intent != null){
                val extra:Bundle? = intent.extras
                val status: String? = extra?.getString(DownloadManager.COLUMN_STATUS)
                val reason: Long? = extra?.getLong(DownloadManager.COLUMN_REASON)
                val totalByte: Long? = extra?.getLong(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val title: String? = extra?.getString(DownloadManager.COLUMN_TITLE)
                val byteSoFar: Long? = extra?.getLong(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val id: Long? = extra?.getLong(DownloadManager.COLUMN_ID)
                val extraId:Long? = extra?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)

                if(id != null)
                    Log.d(TAG, "DownloadManager ID: $id")
                if(extraId != null)
                    Log.d(TAG, "DownloadManager extra ID: $extraId")
                if(title != null)
                    Log.d(TAG, "DownloadManager title: $title")
                if(status != null)
                    Log.d(TAG, "DownloadManager status: $status")
                if(reason != null)
                    Log.d(TAG, "DownloadManager reason: $reason")
                if(totalByte != null)
                    Log.d(TAG, "DownloadManager total size bytes: $totalByte")
                if(byteSoFar != null)
                    Log.d(TAG, "DownloadManager bytes download so far: $byteSoFar")


            }


        }

    }



}