package com.xinman939.cineanchor.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * 设置存储: 后端地址 + 断点续播进度
 * 进度 key 规则: "progress:{type}:{tmdbId}:{episode}" → 毫秒
 */
class Settings(private val context: Context) {

    private val serverKey = stringPreferencesKey("server_base_url")

    /** 后端地址, 默认电脑局域网 IP (首次启动可在设置页改) */
    val serverBaseUrl: Flow<String> = context.dataStore.data.map { it[serverKey] ?: "http://192.168.0.18:8787/" }

    suspend fun setServerBaseUrl(url: String) {
        context.dataStore.edit { it[serverKey] = url.trimEnd('/') + "/" }
    }

    // ---- 断点续播 ----
    private fun progressKey(episodeKey: String) = stringPreferencesKey("progress:$episodeKey")

    suspend fun saveProgress(episodeKey: String, positionMs: Long) {
        context.dataStore.edit { it[progressKey(episodeKey)] = positionMs.toString() }
    }

    suspend fun loadProgress(episodeKey: String): Long =
        context.dataStore.data.map { it[progressKey(episodeKey)]?.toLongOrNull() ?: 0L }
            .firstOrNull() ?: 0L

    companion object {
        /** 续播 key: movie 用 "movie", 剧集用 "S1E01" */
        fun episodeKey(type: String, tmdbId: Int, season: Int, episode: String): String =
            "$type:$tmdbId:S${season}E$episode"
    }
}
