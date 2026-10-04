package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.DipzonPurpleLight
import com.example.ui.theme.DipzonTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun VerticalVideoSurface(
    videoUrl: String,
    thumbnailUrl: String,
    isPlaying: Boolean,
    playbackSpeed: Float,
    seekPositionSeconds: Long,
    onPositionUpdate: (seconds: Long, duration: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ready by remember(videoUrl) { mutableStateOf(false) }
    var error by remember(videoUrl) { mutableStateOf<String?>(null) }
    var mediaPlayer by remember(videoUrl) { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(videoUrl) {
        if (videoUrl.isBlank()) {
            error = "Bu bölüm için video eklenmemiş."
            onDispose { }
        } else {
            val player = MediaPlayer()
            mediaPlayer = player
            try {
                player.setDataSource(context, Uri.parse(videoUrl))
                player.setOnPreparedListener { mp ->
                    ready = true
                    error = null
                    if (seekPositionSeconds > 0) mp.seekTo((seekPositionSeconds * 1000).toInt())
                    try { if (android.os.Build.VERSION.SDK_INT >= 23) mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed) } catch (_: Exception) {}
                    if (isPlaying) mp.start()
                }
                player.setOnErrorListener { _, _, _ ->
                    ready = false
                    error = "Video oynatılamadı. Bağlantıyı veya dosyayı kontrol et."
                    true
                }
                player.prepareAsync()
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Video açılamadı."
            }
            onDispose {
                try { player.reset(); player.release() } catch (_: Exception) {}
                mediaPlayer = null
            }
        }
    }

    LaunchedEffect(isPlaying, ready) {
        val mp = mediaPlayer ?: return@LaunchedEffect
        try {
            if (ready && isPlaying && !mp.isPlaying) mp.start()
            if (ready && !isPlaying && mp.isPlaying) mp.pause()
        } catch (_: Exception) {}
    }

    LaunchedEffect(playbackSpeed, ready) {
        try {
            val mp = mediaPlayer
            if (ready && mp != null && android.os.Build.VERSION.SDK_INT >= 23) mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
        } catch (_: Exception) {}
    }

    LaunchedEffect(seekPositionSeconds, ready) {
        val mp = mediaPlayer ?: return@LaunchedEffect
        try {
            if (ready && kotlin.math.abs(mp.currentPosition - seekPositionSeconds * 1000) > 1500) mp.seekTo((seekPositionSeconds * 1000).toInt())
        } catch (_: Exception) {}
    }

    LaunchedEffect(isPlaying, ready) {
        while (isActive) {
            val mp = mediaPlayer
            if (isPlaying && ready && mp != null) {
                try {
                    val duration = mp.duration
                    onPositionUpdate((mp.currentPosition / 1000).toLong(), if (duration > 0) (duration / 1000).toLong() else 0L)
                } catch (_: Exception) {}
            }
            delay(500)
        }
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) { mediaPlayer?.setSurface(Surface(texture)) }
                        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = Unit
                        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean { mediaPlayer?.setSurface(null); return true }
                        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!ready) {
            AsyncImage(model = thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)), contentAlignment = Alignment.Center) {
                if (error == null) CircularProgressIndicator(color = DipzonPurpleLight)
                else Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = DipzonPurpleLight, modifier = Modifier.size(34.dp))
                    Text(error!!, color = DipzonTextSecondary)
                }
            }
        }
    }
}
