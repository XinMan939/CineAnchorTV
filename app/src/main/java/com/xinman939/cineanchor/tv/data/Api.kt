package com.xinman939.cineanchor.tv.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** 后端 FastAPI :8787 的 /api/v1/ 契约 */
interface CineAnchorApi {

    @GET("api/v1/trending")
    suspend fun trending(): MediaListResponse

    @GET("api/v1/latest")
    suspend fun latest(@Query("n") n: Int = 24): MediaListResponse

    @GET("api/v1/search")
    suspend fun search(@Query("q") q: String): MediaListResponse

    @GET("api/v1/detail/{type}/{id}")
    suspend fun detail(
        @Path("type") type: String,   // "movie" | "tv"
        @Path("id") tmdbId: Int,
    ): MediaDetail

    /** 聚合线路池 (并发 5 源, 后端重活) */
    @GET("api/v1/sources/{type}/{id}")
    suspend fun sources(
        @Path("type") type: String,
        @Path("id") tmdbId: Int,
        @Query("season") season: Int = 0,
    ): SourcesResponse
}
