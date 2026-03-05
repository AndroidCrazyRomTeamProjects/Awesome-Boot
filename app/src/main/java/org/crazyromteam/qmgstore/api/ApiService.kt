package org.crazyromteam.qmgstore.api

import retrofit2.http.GET

interface ApiService {
    @GET("themes.json")
    suspend fun getThemes(): Map<String, List<ThemeItem>>
}
