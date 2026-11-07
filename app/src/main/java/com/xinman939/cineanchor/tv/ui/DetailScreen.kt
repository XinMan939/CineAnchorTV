package com.xinman939.cineanchor.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.xinman939.cineanchor.tv.data.ApiClient
import com.xinman939.cineanchor.tv.data.Line
import com.xinman939.cineanchor.tv.data.MediaDetail
import com.xinman939.cineanchor.tv.data.SourcesResponse
import com.xinman939.cineanchor.tv.data.qualityLabel

/**
 * 详情页: 左侧海报+简介, 右侧线路/分集格子。
 * 焦点链: 进入后自动落到第一条线路 (FocusRequester 必须在 focusable 之前!)
 */
@Composable
fun DetailScreen(
    type: String,
    tmdbId: Int,
    season: Int = 0,
    onPlay: (MediaDetail, Line) -> Unit,
    onSeasonChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var data by remember { mutableStateOf<SourcesResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val firstFocus = remember { FocusRequester() }

    LaunchedEffect(type, tmdbId, season) {
        try {
            data = ApiClient.api.sources(type, tmdbId, season)
        } catch (e: Exception) {
            error = "聚合失败: ${e.message}"
        }
    }

    val d = data
    Box(Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        when {
            error != null -> Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(error!!, color = Color(0xFFF85149))
                Card(onClick = onBack, Modifier.padding(top = 16.dp)) { Text("返回", Modifier.padding(12.dp)) }
            }
            d == null -> Text("线路聚合中…", Modifier.align(Alignment.Center), color = Color(0xFF7D8590), fontSize = 18.sp)
            else -> Row(Modifier.fillMaxSize().padding(24.dp)) {
                // 左栏: 海报 + 信息 + 简介
                Column(Modifier.width(340.dp).fillMaxHeight()) {
                    AsyncImage(
                        model = d.media.poster_full,
                        contentDescription = d.media.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                    Text(
                        "${d.media.title} (${d.media.year})",
                        color = Color(0xFFE6EDF3), fontSize = 22.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        "${if (d.media.type == "tv") "剧集" else "电影"} · ${d.lines.size} 条线路" +
                            (d.media.seasons?.takeIf { it > 1 }?.let { " · 共${it}季" } ?: ""),
                        color = Color(0xFF7D8590), fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    )
                    Text(
                        d.media.overview ?: "暂无简介",
                        color = Color(0xFFC9D1D9), fontSize = 13.sp, lineHeight = 20.sp,
                        modifier = Modifier.weight(1f),
                    )
                    // 季切换 (多季剧集)
                    if ((d.media.seasons ?: 0) > 1) {
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (s in 1..(d.media.seasons ?: 1)) {
                                Card(onClick = { onSeasonChange(s) }) {
                                    Text(
                                        "第${s}季",
                                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = if (s == season) Color(0xFF1F6FEB) else Color(0xFFE6EDF3),
                                    )
                                }
                            }
                        }
                    }
                }
                // 右栏: 线路格子 (后端已按画质+分数排序)
                TvLazyVerticalGrid(
                    columns = TvGridCells.Fixed(3),
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 24.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(d.lines) { idx, line ->
                        Card(
                            onClick = { onPlay(d.media, line) },
                            modifier = if (idx == 0) Modifier.focusRequester(firstFocus) else Modifier,
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(line.episode, color = Color(0xFFE6EDF3), fontSize = 14.sp, maxLines = 1)
                                Text(
                                    buildString {
                                        append(line.provider)
                                        val q = line.qualityLabel
                                        if (q.isNotEmpty()) append(" · $q")
                                    },
                                    color = if (line.quality >= 30) Color(0xFF3FB950) else Color(0xFF7D8590),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        // 首帧布局完成后请求焦点 (必须 withFrameNanos 等一帧, 否则无效)
                        if (idx == 0) {
                            LaunchedEffect(d) {
                                withFrameNanos { }
                                firstFocus.requestFocus()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun withFrameNanos(block: () -> Unit) {
    // androidx.compose.runtime.withFrameNanos 的包装, 保持 import 简洁
    androidx.compose.runtime.withFrameNanos { block() }
}
