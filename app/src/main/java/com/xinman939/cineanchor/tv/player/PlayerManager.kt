package com.xinman939.cineanchor.tv.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Media3 封装。
 * 关键点: 很多 CMS 源 URL 不带 .m3u8 后缀 (如 /video/xx/HD/index.m3u8 之外的变体),
 * 必须显式 setMimeType(MimeTypes.APPLICATION_M3U8), 否则 ExoPlayer 按普通文件解析直接失败。
 */
class PlayerManager(private val context: Context) {

    private var _player: ExoPlayer? = null

    /** 供 PlayerView 绑定 (创建后才有值) */
    val player: ExoPlayer? get() = _player

    /** 每次新 player 实例创建时自增, Compose 侧观察它触发 PlayerView 重新绑定 */
    @Volatile
    var generation: Int = 0
        private set

    fun play(url: String, startPositionMs: Long = 0) {
        release()
        _player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.i("CineAnchor", "player state=$playbackState (1=buffer 2=ready 3=ended 4=idle) playing=$isPlaying")
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("CineAnchor", "player error: ${error.errorCodeName} cause=${error.cause}", error)
                }
            })
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)   // m3u8 必须
                .build()
            Log.i("CineAnchor", "player play: url=$url pos=$startPositionMs")
            setMediaItem(mediaItem, startPositionMs)       // 断点续播位置
            prepare()
            playWhenReady = true
        }
        generation++
    }

    val currentPosition: Long get() = _player?.currentPosition ?: 0L
    val isPlaying: Boolean get() = _player?.isPlaying ?: false
    val duration: Long get() = _player?.duration ?: 0L

    fun pause() { _player?.pause() }
    fun resume() { _player?.play() }

    /** 跳转到指定位置 (遥控器快进/快退用) */
    fun seekTo(positionMs: Long) { _player?.seekTo(positionMs) }

    /** 快进/快退 10 秒 (D-pad 左右长按) */
    fun seekBy(deltaMs: Long) {
        _player?.let { p -> p.seekTo((p.currentPosition + deltaMs).coerceIn(0, p.duration.coerceAtLeast(0))) }
    }

    fun release() {
        _player?.release()
        _player = null
    }
}
