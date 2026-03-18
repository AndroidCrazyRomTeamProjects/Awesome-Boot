package org.crazyromteam.qmgstore.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {
    @GET("api/themes.json")
    suspend fun getThemes(): Map<String, List<ThemeItem>>

    @GET("themes/{id}/{file}")
    suspend fun checkFileExists(
        @Path("id") id: String,
        @Path("file") file: String
    ): Response<ResponseBody>

    @Streaming
    @GET("themes/{id}/{file}")
    suspend fun downloadFile(
        @Path("id") id: String,
        @Path("file") file: String
    ): Response<ResponseBody>
}
