package com.xinman939.cineanchor.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.xinman939.cineanchor.tv.data.ApiClient
import com.xinman939.cineanchor.tv.data.MediaItem

/**
 * 首页海报墙: 最新上线 (index.db 内容库) + 热门榜单 (TMDB trending)。
 * TvLazyVerticalGrid + TvCard 自带焦点缩放/辉光/D-pad OK 触发。
 */
@Composable
fun HomeScreen(
    onItemFocus: (MediaItem) -> Unit = {},
    onItemClick: (MediaItem) -> Unit,
) {
    var items by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // 最新上线优先 (有线路概率高), 空则退回 trending
            val r = ApiClient.api.latest(30).items.ifEmpty { ApiClient.api.trending().items }
            items = r
        } catch (e: Exception) {
            error = "无法连接后端: ${e.message}"
        } finally {
            loading = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        when {
            loading -> Text("加载中…", Modifier.align(Alignment.Center), color = Color(0xFF7D8590))
            error != null -> Text(error!!, Modifier.align(Alignment.Center).padding(32.dp), color = Color(0xFFF85149))
            else -> TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(5),
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(items, key = { "${it.type}:${it.tmdb_id}" }) { item ->
                    PosterCard(item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun PosterCard(item: MediaItem, onClick: () -> Unit) {
    Column {
        Card(
            onClick = onClick,
            modifier = Modifier.size(150.dp, 225.dp),
        ) {
            Box(Modifier.fillMaxSize().background(Color(0xFF161B22))) {
                AsyncImage(
                    model = item.poster,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            item.title,
            color = Color(0xFFE6EDF3),
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            maxLines = 1,
        )
        Text(
            "${item.year ?: ""} ${item.updated_at ?: ""}".trim(),
            color = Color(0xFF7D8590),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
