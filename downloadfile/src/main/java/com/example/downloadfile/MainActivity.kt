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
import android.os.storage.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    val TAG = "Download Main"
    val downloadLink = "https://drive.google.com/uc?export=download&id=1XSTImggtvJ8oZYIB8sEByMuZipiabCaS" //file size ~23MB
    val publicDownloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) as File
    val fileName = "photo.zip"
    val subDirectory = "testDownload"
    lateinit var networkRequestScope: CoroutineScope
    val downloadLinkId = "1XSTImggtvJ8oZYIB8sEByMuZipiabCaS"
    val downloadLineExport ="download"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkRequestScope      = CoroutineScope(Job() + Dispatchers.Default)

        val downloadButton: Button = findViewById(R.id.downloadManagerButton) as Button
        downloadButton.setOnClickListener {
            Log.d(TAG, "start download manager")
            startDownloadWithDownloadManager()
        }

        val retrofitDownloadbutton: Button = findViewById(R.id.retrofitDownloadButton)
        retrofitDownloadbutton.setOnClickListener {
            Log.d(TAG, "click Retrofit download button")
            startDownloadWithRetrofit()
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


    override fun onDestroy() {
        super.onDestroy()
        networkRequestScope.cancel()
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

    fun startDownloadWithRetrofit(){

        val downloadFileService: DownloadFileService = RetrofitServiceCreatorToHomeWebsite.create(DownloadFileService::class.java)
        val call = downloadFileService.downloadFile(downloadLineExport, downloadLinkId)


        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                if(response.isSuccessful){
                    Log.d(TAG, "The file is found on server")
                    //indicate the download location
                    val customDirectory = File(applicationContext.cacheDir, "custom")
                    if( !customDirectory.exists())
                        customDirectory.mkdir()
                    val filePath = File(customDirectory, fileName)

                    getAllocatableBytes(filePath)

                    networkRequestScope.launch {
                        writeResponseBodyToStorage(response.body()!!, filePath)

                        Log.d(TAG, "end download. File is saved on ${filePath.absolutePath}")
                    }
                    Log.d(TAG, "Callback.onResponse end")
                }else{
                    Log.e(TAG, "fail to connect server: " + response.toString())
                }
            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "Request Download Error", t)
            }
        })
    }




    suspend fun writeResponseBodyToStorage(body: ResponseBody, path: File){

        //withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            val fileSize = body.contentLength()
            var byteDownloaded = 0


            Log.d(TAG, "file will save to ${path.absoluteFile} with size $fileSize")
            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(path)
                val buffer = ByteArray(4 * 1024) //allocate 4k Byte buffer
                while (true) {
                    val byteRead = inputStream.read(buffer)
                    if (byteRead < 0)
                        break
                    outputStream.write(buffer, 0, byteRead)
                    byteDownloaded += byteRead
                    Log.d(TAG, "download progress $byteDownloaded of $fileSize")
                }
                outputStream.flush()


            } catch (ex: IOException) {
                Log.e(TAG, "fail to save file", ex)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        //}


    }


    fun getAllocatableBytes(fileDirectory: File): Long {
        val storageManager: StorageManager = applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val apSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(fileDirectory)
        val availableBytes: Long = storageManager.getAllocatableBytes(apSpecificInternalDirUuid)
        when(availableBytes){
            in 0..5000 -> Log.d(TAG, "available volume is $availableBytes B")
            in 5001..5000000 -> Log.d(TAG, "available volume is " + (availableBytes/1024) +"kB")
            in 5000000..5000000000 -> Log.d(TAG, "available volume is " + (availableBytes/ (1024 * 1024)) +"MB")
            else -> Log.d(TAG, "available volume is " + (availableBytes/ (1024 * 1024 * 1024)) +"GB")
        }


        return availableBytes
    }



}