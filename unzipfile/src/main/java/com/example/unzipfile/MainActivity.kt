package com.example.unzipfile

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.io.inputstream.ZipInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    lateinit var job: Job
    lateinit var networkRequestScope: CoroutineScope
    lateinit var downloadProgressBar: ProgressBar
    lateinit var downloadProgressText: TextView
    val downloadLink = "https://drive.google.com/uc?export=download&id=1XDMUGuvOPkTR2TH_Ikb5-2SIR7nXQ6yk"
    val savedFileName = "photo2password_aB3d.zip"
    val READ_BUFFER_SIZE = 4096
    val unzipPassword = "aB3d"
    lateinit var unzipProgressBar: ProgressBar
    lateinit var unzipProgressText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()
        networkRequestScope = CoroutineScope(job)


        downloadProgressBar = findViewById(R.id.downloadProgressBar)
        downloadProgressText = findViewById(R.id.downloadProgressText)

        unzipProgressBar = findViewById(R.id.unzipProgressBar)
        unzipProgressText = findViewById(R.id.unzipProgressText)

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

        val unzip1Button = findViewById(R.id.unzip1Button) as Button
        unzip1Button.setOnClickListener{
            Log.d(TAG, "unzip1 " + savePath.absolutePath)
            unzip1(savePath)
        }

        val unzip2Button = findViewById(R.id.unzip2Button) as Button
        unzip2Button.setOnClickListener{
            Log.d(TAG, "unzip2 " + savePath.absolutePath)
            unzip2(savePath)
        }

        val unzip3Button = findViewById(R.id.unzip3Button) as Button
        unzip3Button.setOnClickListener {
            Log.d(TAG, "unzip3 " + savePath.absolutePath)
            unzip3(savePath)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    //it use library net.lingala.zip4j to unzip with password
    //extract all file from the zipped file
    fun unzip1(file: File){
        val unzipedFolder = File(applicationContext.cacheDir, "folder1")
        if(!unzipedFolder.exists())
            unzipedFolder.mkdir()

        val zipFile = ZipFile(file) //ZipFile class from Zip4j library
        zipFile.setPassword(unzipPassword.toCharArray()) //unzip password
        networkRequestScope.launch {
            zipFile.extractAll(unzipedFolder.absolutePath)

        }

    }

    //calculate the uncompressed size of all file. Then extract the files one by one and show the progress
    fun unzip2(file: File) {
        val unzipedFolder = File(applicationContext.cacheDir, "folder2")
        if (!unzipedFolder.exists())
            unzipedFolder.mkdir()

        val zipFile = ZipFile(file) //this ZipFile from Zip4j library
        zipFile.setPassword(unzipPassword.toCharArray())
        val fileHeaders = zipFile.fileHeaders
        var uncompressedSize = 0L


        networkRequestScope.launch {
            fileHeaders.forEach {
                uncompressedSize += it.uncompressedSize // can use to check if the device has enough free space
            }

            val totalSize = uncompressedSize
            uncompressedSize = 0L

            fileHeaders.forEach {
                Log.d(TAG, "Entry name: ${it.fileName} size: ${it.uncompressedSize}")

                if(!it.isDirectory){
                    zipFile.extractFile(it, unzipedFolder.absolutePath)
                    uncompressedSize += it.uncompressedSize
                    val percentage = (((uncompressedSize * 100) / totalSize))
                    runOnUiThread{
                        unzipProgressBar.progress = percentage.toInt()
                        unzipProgressText.text = "${percentage.toInt()}%"
                    }
                }

                /*
                val temp = File(unzipedFolder, it.fileName)
                if (it.isDirectory) {
                    temp.mkdir()
                } else {
                    extractFile(zipFile.getInputStream(it), it.uncompressedSize, temp)
                    uncompressedSize += it.uncompressedSize
                    val percentage = (((uncompressedSize * 100) / totalSize))
                    runOnUiThread{
                        unzipProgressBar.progress = percentage.toInt()
                        unzipProgressText.text = "${percentage.toInt()}%"
                    }
                }
                */
            }
        }
    }

    //use ZipInputStream from Zip4j library
    //Exception is not handle to shorten the code for understanding easily. Please handle exceptions for real implementation
    fun unzip3(file: File){
        val buffer = ByteArray(READ_BUFFER_SIZE)
        var byteRead = 0
        var fileByteRead = 0L

        val unzipedFolder = File(applicationContext.cacheDir, "folder3")
        if(!unzipedFolder.exists())
            unzipedFolder.mkdir()

        networkRequestScope.launch {
            //first calculate the total uncompressed size of all files
            var uncompressedSize = 0L
            val zipIS = ZipInputStream(FileInputStream(file), unzipPassword.toCharArray())
            while(true){
                val entry = zipIS.nextEntry
                if (entry == null)
                    break
                uncompressedSize += entry.uncompressedSize
            }
            val totalSize = uncompressedSize
            uncompressedSize = 0L
            Log.d(TAG, "total size: ${totalSize}")


            val zipInputStream = ZipInputStream(FileInputStream(file), unzipPassword.toCharArray())
            while (true) {
                val entry = zipInputStream.nextEntry
                if (entry == null)
                    break
                val temp = File(unzipedFolder, entry.fileName)
                Log.d(TAG, "unzip3 entry: ${entry.fileName}")
                if (entry.isDirectory) {
                    temp.mkdir()
                } else {
                    val fileSize = entry.uncompressedSize
                    fileByteRead = 0L
                    val bos = BufferedOutputStream(FileOutputStream(temp))
                    while (true) {
                        byteRead = zipInputStream.read(buffer)
                        if (byteRead == -1)
                            break
                        bos.write(buffer, 0, byteRead)
                        fileByteRead += byteRead
                        Log.d(TAG, "${entry.fileName} ${fileByteRead} of ${fileSize} has been read")
                    }
                    bos.flush()
                    bos.close()

                    uncompressedSize += fileSize
                    val percentage = (((uncompressedSize * 100) / totalSize))
                    runOnUiThread{
                        unzipProgressBar.progress = percentage.toInt()
                        unzipProgressText.text = "${percentage.toInt()}%"
                    }


                }
            }
        }
    }


    @Throws(IOException::class)
    fun extractFile(inputStream: InputStream, fileSize: Long, destFile: File){
        val bos = BufferedOutputStream(FileOutputStream(destFile))
        val buffer = ByteArray(READ_BUFFER_SIZE)
        var byteRead = 0
        while(true){
            byteRead = inputStream.read(buffer)
            if(byteRead == -1)
                break
            bos.write(buffer, 0, byteRead)


        }
        bos.flush()
        bos.close()
    }



}
