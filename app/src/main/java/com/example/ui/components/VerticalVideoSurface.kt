package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.*
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
    var isVideoReady by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Backup animated simulation position if media player fails in headless emulator
    var simulatedPosition by remember(videoUrl) { mutableLongStateOf(seekPositionSeconds) }

    // Media Player reference
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Manage Media Player Lifecycle
    DisposableEffect(videoUrl) {
        val player = MediaPlayer().apply {
            try {
                setDataSource(context, Uri.parse(videoUrl))
                setOnPreparedListener { mp ->
                    isVideoReady = true
                    hasError = false
                    mp.isLooping = false
                    try {
                        if (playbackSpeed != 1.0f && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                        }
                    } catch (_: Exception) {}
                    if (seekPositionSeconds > 0) {
                        mp.seekTo((seekPositionSeconds * 1000).toInt())
                    }
                    if (isPlaying) {
                        mp.start()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    hasError = true
                    isVideoReady = false
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                hasError = true
            }
        }
        mediaPlayer = player

        onDispose {
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: Exception) {}
            mediaPlayer = null
        }
    }

    // React to isPlaying changes
    LaunchedEffect(isPlaying, isVideoReady) {
        mediaPlayer?.let { mp ->
            try {
                if (isVideoReady) {
                    if (isPlaying && !mp.isPlaying) {
                        mp.start()
                    } else if (!isPlaying && mp.isPlaying) {
                        mp.pause()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // React to speed change
    LaunchedEffect(playbackSpeed, isVideoReady) {
        mediaPlayer?.let { mp ->
            try {
                if (isVideoReady && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                }
            } catch (_: Exception) {}
        }
    }

    // React to explicit seeks
    LaunchedEffect(seekPositionSeconds) {
        mediaPlayer?.let { mp ->
            try {
                if (isVideoReady) {
                    val targetMs = (seekPositionSeconds * 1000).toInt()
                    val currentMs = mp.currentPosition
                    if (kotlin.math.abs(targetMs - currentMs) > 1500) {
                        mp.seekTo(targetMs)
                    }
                }
            } catch (_: Exception) {}
        }
        simulatedPosition = seekPositionSeconds
    }

    // Polling progress ticker (every 500ms)
    LaunchedEffect(isPlaying, isVideoReady, hasError) {
        while (isActive) {
            if (isPlaying) {
                if (mediaPlayer != null && isVideoReady && !hasError) {
                    try {
                        val currentMs = mediaPlayer?.currentPosition ?: 0
                        val durationMs = mediaPlayer?.duration ?: 0
                        onPositionUpdate(
                            (currentMs / 1000).toLong(),
                            if (durationMs > 0) (durationMs / 1000).toLong() else 180L
                        )
                    } catch (_: Exception) {}
                } else {
                    // Fallback simulated progress progression
                    simulatedPosition += 1
                    onPositionUpdate(simulatedPosition, 180L)
                }
            }
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasError) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                                val surface = Surface(surfaceTexture)
                                mediaPlayer?.setSurface(surface)
                            }

                            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                mediaPlayer?.setSurface(null)
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Atmospheric Cinematic Fallback & Shimmer Scrim if loading or offline
        if (!isVideoReady || hasError) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric cinematic pulse overlay
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.55f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    DipzonPurpleDark.copy(alpha = pulseAlpha),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Subtle audio waveform visualizer lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = 24
                    val barWidth = canvasWidth / (barCount * 2)

                    for (i in 0 until barCount) {
                        val x = barWidth * 2 * i + barWidth
                        val barHeight = (kotlin.math.sin(simulatedPosition.toFloat() + i) + 1.2f) * 18.dp.toPx()
                        drawLine(
                            color = DipzonPurpleLight.copy(alpha = 0.35f),
                            start = Offset(x, canvasHeight * 0.78f - barHeight),
                            end = Offset(x, canvasHeight * 0.78f + barHeight),
                            strokeWidth = barWidth * 0.7f
                        )
                    }
                }
            }
        }
    }
}
