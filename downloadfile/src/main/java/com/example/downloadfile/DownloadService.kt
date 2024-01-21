package com.example.downloadfile

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DownloadFileService {
    @Streaming
    @GET("uc")
    fun downloadFile(@Query("export") export: String, @Query("id") id: String): Call<ResponseBody>
}
