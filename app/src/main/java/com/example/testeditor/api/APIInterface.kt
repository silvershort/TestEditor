package com.example.testeditor.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface APIInterface {

    @GET
    fun getAudioDownload(@Url url: String): Call<ResponseBody>

}