package com.xinman939.cineanchor.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import com.xinman939.cineanchor.tv.data.ApiClient
import com.xinman939.cineanchor.tv.data.Line
import com.xinman939.cineanchor.tv.data.MediaDetail
import com.xinman939.cineanchor.tv.data.Settings
import com.xinman939.cineanchor.tv.ui.DetailScreen
import com.xinman939.cineanchor.tv.ui.HomeScreen
import com.xinman939.cineanchor.tv.ui.PlayerScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 路由: Home → Detail(type/id/season) → Player (Player 内嵌在 detail 目的地, Back 自然回详情)。
 * Back 键由 Navigation-Compose 自动处理。
 * 后端地址: DataStore 持久化, 右上角 ⚙ 可改 (默认 PC 局域网 192.168.0.18:8787)。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val settings = remember { Settings(applicationContext) }
            val scope = rememberCoroutineScope()

            var apiReady by remember { mutableStateOf(false) }
            var serverUrl by remember { mutableStateOf("") }
            var showServerDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val url = settings.serverBaseUrl.first()
                serverUrl = url
                ApiClient.init(settings)
                apiReady = true
            }

            Box(Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
                if (apiReady) {
                    NavHost(nav, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onItemClick = { item ->
                                    nav.navigate("detail/${item.type}/${item.tmdb_id}/${item.season}")
                                },
                            )
                        }
                        composable(
                            route = "detail/{type}/{id}/{season}",
                            arguments = listOf(
                                navArgument("type") { type = NavType.StringType },
                                navArgument("id") { type = NavType.IntType },
                                navArgument("season") { type = NavType.IntType },
                            ),
                        ) { backStack ->
                            val type = backStack.arguments?.getString("type") ?: "movie"
                            val id = backStack.arguments?.getInt("id") ?: 0
                            val season0 = backStack.arguments?.getInt("season") ?: 0

                            var playing by remember {
                                mutableStateOf<Pair<MediaDetail, Line>?>(null)
                            }
                            var curSeason by remember(id) { mutableStateOf(season0) }

                            if (playing == null) {
                                DetailScreen(
                                    type = type, tmdbId = id, season = curSeason,
                                    onPlay = { media, line -> playing = media to line },
                                    onSeasonChange = { curSeason = it },
                                    onBack = { nav.popBackStack() },
                                )
                            } else {
                                val (media, line) = playing!!
                                PlayerScreen(
                                    media = media, line = line, season = curSeason,
                                    onBack = { playing = null },   // Back 回详情页
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "CineAnchor TV 启动中…",
                        Modifier.align(Alignment.Center),
                        color = Color(0xFF7D8590), fontSize = 18.sp,
                    )
                }

                // 设置入口 (右上角): 打开后端地址对话框
                Button(
                    onClick = { showServerDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                ) { Text("⚙", fontSize = 18.sp) }
            }

            if (showServerDialog) {
                ServerDialog(
                    current = serverUrl,
                    onSave = { newUrl ->
                        scope.launch {
                            settings.setServerBaseUrl(newUrl)
                            val url = settings.serverBaseUrl.first()
                            serverUrl = url
                            ApiClient.rebuild(url)
                            showServerDialog = false
                            // 回首页重载数据
                            nav.navigate("home") { popUpTo("home") { inclusive = true } }
                        }
                    },
                    onDismiss = { showServerDialog = false },
                )
            }
        }
    }
}

@Composable
private fun ServerDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current) }
    Card(onClick = {}, modifier = Modifier.padding(48.dp)) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("后端服务器地址", color = Color(0xFFE6EDF3), fontSize = 16.sp)
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("http://192.168.x.x:8787/") },
                singleLine = true,
            )
            Button(onClick = { onSave(text) }) { Text("保存并重连") }
        }
    }
}
