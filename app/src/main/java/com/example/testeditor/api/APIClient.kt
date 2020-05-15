package com.example.testeditor.api

import okhttp3.ResponseBody
import retrofit2.Retrofit

object APIClient {

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://incompetech.com/")
            .build() }

    fun getAPIInterface() = retrofit.create(APIInterface::class.java)
    fun getAudioDownload(path: String) = getAPIInterface().getAudioDownload(path)
}