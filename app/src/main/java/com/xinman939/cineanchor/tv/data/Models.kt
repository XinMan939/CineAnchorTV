package com.xinman939.cineanchor.tv.data

/**
 * 数据类 —— 字段名与后端 /api/v1/ 真实返回 1:1 对齐 (2026-11-07 实测核实):
 *
 * trending/latest: { items: [ {tmdb_id, type, title, year, poster(全URL), rating, popularity} ] }
 * sources: { media:{...}, stats:{...}, lines: [ {provider, line, episode, url, score, quality} ] }
 * detail:  { tmdb_id, type, title, original_title, year, poster, backdrop, overview, seasons, poster_full }
 */
data class MediaItem(
    val tmdb_id: Int,
    val type: String,          // "movie" | "tv"
    val title: String,
    val year: String? = null,
    val poster: String? = null, // 后端已拼全的 URL, 无需再拼
    val rating: Double? = null,
    val season: Int = 0,        // latest 返回, 0=电影/无季
    val updated_at: String? = null,
)

data class MediaListResponse(val items: List<MediaItem>, val count: Int = 0)

/** 一条播放线路 (扁平结构, 不是 sources.episodes map) */
data class Line(
    val provider: String,   // "量子" / "非凡" / "暴风" / "360" / "电影天堂"
    val line: String,       // 线路名, 通常是 "默认线路"
    val episode: String,    // "HD" / "第01集" / "第02集"
    val url: String,        // m3u8 直链
    val score: Int = 0,
    val quality: Int = 0,   // 40=4K 30=1080P 20=720P 10=无标注 -10=枪版
)

data class SourceStats(val matched: Int = 0, val lines: Int = 0, val error: String? = null)

data class SourcesResponse(
    val from_cache: Boolean = false,
    val media: MediaDetail,
    val stats: Map<String, SourceStats> = emptyMap(),
    val lines: List<Line> = emptyList(),
)

data class MediaDetail(
    val tmdb_id: Int,
    val type: String,
    val title: String,
    val original_title: String? = null,
    val year: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val overview: String? = null,
    val seasons: Int? = null,      // 剧集季数, 电影为 null
    val poster_full: String? = null,
)

/** 线路 quality 数值 → 展示标签 */
val Line.qualityLabel: String
    get() = when {
        quality >= 40 -> "4K"
        quality >= 30 -> "1080P"
        quality >= 20 -> "720P"
        quality <= -10 -> "枪版"
        else -> ""
    }
