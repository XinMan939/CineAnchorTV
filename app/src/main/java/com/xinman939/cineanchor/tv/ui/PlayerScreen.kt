package com.xinman939.cineanchor.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.xinman939.cineanchor.tv.data.Line
import com.xinman939.cineanchor.tv.data.MediaDetail
import com.xinman939.cineanchor.tv.data.Settings
import com.xinman939.cineanchor.tv.player.PlayerManager
import kotlinx.coroutines.delay

/**
 * 播放页: Media3 PlayerView 全屏 + 断点续播 (每5秒存进度)。
 * D-pad 左右 = 快退/快进 10s, Center = 播放/暂停, Back = 退出 (自动存进度)。
 *
 * 关键: player 实例由 PlayerManager.play() 创建 (先 release 旧的再建新的)。
 * 这里用 gen 状态捕获"新实例已创建"事件 → 重组 → AndroidView.update 绑定新实例。
 * 绑不上 surface 就是"有声音没画面"的黑屏典型坑。
 */
@Composable
fun PlayerScreen(
    media: MediaDetail,
    line: Line,
    season: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val manager = remember { PlayerManager(context) }
    val settings = remember { Settings(context) }
    val episodeKey = Settings.episodeKey(media.type, media.tmdb_id, season, line.episode)

    var gen by remember { mutableIntStateOf(0) }
    LaunchedEffect(line.url) {
        val saved = settings.loadProgress(episodeKey)
        manager.play(line.url, startPositionMs = if (saved > 30_000) saved else 0L)  // <30s 从头播
        gen = manager.generation   // 状态更新 → 重组 → update 重跑 → 新 player 绑上 surface
    }

    // 断点续播: 每 5 秒存一次
    LaunchedEffect(episodeKey) {
        while (true) {
            delay(5000)
            if (manager.isPlaying) settings.saveProgress(episodeKey, manager.currentPosition)
        }
    }

    DisposableEffect(Unit) {
        onDispose { manager.release() }   // 退出页面必释放, 否则盒子内存泄漏
    }

    BackHandler { onBack() }

    // 读 gen: 变化触发重组, 让下面的 AndroidView.update 重新执行绑定
    val currentGen = gen

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = true      // D-pad 唤起系统控制条
                    setShowSubtitleButton(true)
                }
            },
            update = { view ->
                view.player = manager.player  // 每次重组都同步最新实例
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
