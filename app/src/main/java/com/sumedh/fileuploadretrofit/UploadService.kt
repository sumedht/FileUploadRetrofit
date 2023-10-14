package com.sumedh.fileuploadretrofit


import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UploadService {
    @Multipart
    @POST("api/uploadingResume")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("fileName") fileName: String
    ): Call<String>
}