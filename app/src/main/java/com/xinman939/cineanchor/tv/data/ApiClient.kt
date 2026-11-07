package com.xinman939.cineanchor.tv.data

import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Retrofit 单例, baseUrl 从 DataStore 读 (Settings 注入) */
object ApiClient {

    @Volatile
    private var apiInstance: CineAnchorApi? = null

    /** 当前 baseUrl (图片代理等用途), init/rebuild 时更新 */
    @Volatile
    var baseUrl: String = ""
        private set

    /** 必须在协程里先调用一次 (读 DataStore 是挂起操作) */
    suspend fun init(settings: Settings): CineAnchorApi {
        apiInstance?.let { return it }
        val baseUrl = settings.serverBaseUrl.first()
        synchronized(this) {
            apiInstance?.let { return it }
            return rebuild(baseUrl).also { apiInstance = it }
        }
    }

    /** 设置页改完地址后调用, 重建 Retrofit */
    fun rebuild(baseUrl: String): CineAnchorApi {
        this.baseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return build(baseUrl).also { apiInstance = it }
    }

    val api: CineAnchorApi
        get() = apiInstance ?: error("ApiClient.init() not called — 在 Application/首个 LaunchedEffect 里调用")

    private fun build(url: String): CineAnchorApi =
        Retrofit.Builder()
            .baseUrl(if (url.endsWith("/")) url else "$url/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)   // sources 聚合可能慢 (5源并发+慢源)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CineAnchorApi::class.java)
}
