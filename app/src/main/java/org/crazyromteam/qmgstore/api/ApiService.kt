package org.crazyromteam.qmgstore.api

import retrofit2.http.GET

interface ApiService {
    @GET("themes")
    suspend fun getThemes(): List<ThemeItem>
}
