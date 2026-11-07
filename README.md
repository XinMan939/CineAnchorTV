# CineAnchor TV（影锚 TV 壳）

作品驱动聚合的 Android TV 端。后端 FastAPI (:8787) 干所有重活（TMDB + 5 源并发聚合 + 画质排序），TV 端只有三层：焦点导航 UI + Retrofit HTTP 客户端 + Media3 播放器。

## 技术栈

Kotlin 2.0 + Compose BOM 2025.02 + **androidx.tv.material3**（注意不是 compose.material3）+ Media3 1.5.1（HLS）+ Retrofit 2.11 + Coil + DataStore。

## 目录

```
app/src/main/java/com/xinman939/cineanchor/tv/
├── MainActivity.kt        # NavHost 路由 + 设置入口
├── data/
│   ├── Models.kt          # 与 /api/v1/ 真实返回 1:1 对齐
│   ├── Api.kt             # Retrofit 接口定义
│   ├── ApiClient.kt       # Retrofit 单例（DataStore 注入 baseUrl）
│   └── Settings.kt        # 后端地址 + 断点续播进度
├── ui/
│   ├── HomeScreen.kt      # 海报墙（latest 优先, trending 兜底）
│   ├── DetailScreen.kt    # 详情 + 线路格子 + 季切换
│   └── PlayerScreen.kt    # Media3 全屏播放 + 5 秒存进度
└── player/
    └── PlayerManager.kt   # ExoPlayer 封装（显式 M3U8 MIME）
```

## API 契约（与后端实测核实过）

| 端点 | 返回关键字段 |
|---|---|
| `GET /api/v1/latest?n=30` | `{items:[{tmdb_id,type,title,year,poster(全URL),season,updated_at}]}` |
| `GET /api/v1/trending` | 同上（无 season/updated_at） |
| `GET /api/v1/sources/{type}/{id}?season=N` | `{media:{title,seasons,...}, stats:{源:{matched,lines}}, lines:[{provider,line,episode,url,score,quality}]}` |
| `GET /api/v1/detail/{type}/{id}` | `{tmdb_id,type,title,year,poster,backdrop,overview,seasons,poster_full}` |

**注意**：`lines` 是扁平数组（不是 sources.episodes map）；`poster` 是后端拼好的完整 URL（不用再拼 w500 前缀）。

## 关键坑（写代码时已规避）

1. **`androidx.tv.material3` ≠ `androidx.compose.material3`**——组件长得像，焦点行为完全不同
2. **`Modifier.focusRequester(fr).focusable()` 顺序不能反**——反了静默失效
3. **进详情页请求焦点必须等首帧**——`withFrameNanos {}` 后再 `requestFocus()`
4. **m3u8 必须显式 `setMimeType(MimeTypes.APPLICATION_M3U8)`**——部分源 URL 无后缀
5. **`usesCleartextTraffic="true"`**——后端是局域网 http 明文
6. **PlayerView 退出必须 `manager.release()`**——盒子内存泄漏

## 开发调试

1. Android Studio 打开本目录（需要 SDK 35 + JDK 17）
2. PC 后端先跑起来：`python server.py --host 0.0.0.0 --port 8787`，防火墙放行 8787
3. TV 开 ADB：设置→关于→连按版本号 7 次→网络调试 → `adb connect TV_IP:5555`
4. 首次启动若连不上后端：右上角 ⚙ 改后端地址（默认 `http://192.168.0.18:8787/`）

## 第一周目标

TV 打开 App → 海报墙 → 点进详情 → 选线路 → 播出来。打通即闭环，之后再打磨：续播精确度、加载动画、错误兜底、设置页美化。
