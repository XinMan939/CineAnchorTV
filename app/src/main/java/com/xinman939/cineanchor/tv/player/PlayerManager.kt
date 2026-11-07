package com.xinman939.cineanchor.tv.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
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

    fun play(url: String, startPositionMs: Long = 0) {
        release()
        _player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)   // m3u8 必须
                .build()
            setMediaItem(mediaItem, startPositionMs)       // 断点续播位置
            prepare()
            playWhenReady = true
        }
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
        player = null
    }
}
