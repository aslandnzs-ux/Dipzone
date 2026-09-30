package com.example.ui.player

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel
import kotlinx.coroutines.delay

@Composable
fun VerticalPlayerScreen(
    viewModel: DipzonViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val comments by viewModel.currentSeriesComments.collectAsState()

    val series = playerState.currentSeries
    val episode = playerState.currentEpisode

    BackHandler {
        onBack()
    }

    // Auto fade controls after 3.2 seconds
    LaunchedEffect(playerState.isControlsVisible, playerState.isPlaying) {
        if (playerState.isControlsVisible && playerState.isPlaying) {
            delay(3200)
            viewModel.setControlsVisibility(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.toggleControls() }
            )
    ) {
        // 9:16 Full Screen Video Surface
        if (episode != null) {
            VerticalVideoSurface(
                videoUrl = episode.videoUrl,
                thumbnailUrl = episode.thumbnailUrl,
                isPlaying = playerState.isPlaying,
                playbackSpeed = playerState.playbackSpeed,
                seekPositionSeconds = playerState.currentPositionSeconds,
                onPositionUpdate = { sec, dur ->
                    viewModel.updatePlaybackPosition(sec, dur)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtitles Overlay (if enabled)
        if (playerState.subtitlesEnabled && playerState.currentSubtitleText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (playerState.isControlsVisible) 110.dp else 40.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = playerState.currentSubtitleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        // Next Episode Auto-advance Countdown Banner
        if (playerState.autoAdvanceSeconds != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 70.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DipzonPurplePrimary.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { playerState.autoAdvanceSeconds!!.toFloat() / 4f },
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Sonraki Bölüm Başlıyor: ${playerState.autoAdvanceSeconds}s",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Right Side Social Interaction Bar (Always Accessible or during Controls)
        AnimatedVisibility(
            visible = playerState.isControlsVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                IconButton(
                    onClick = { viewModel.toggleCurrentSeriesLike() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("player_like_btn")
                ) {
                    Icon(
                        imageVector = if (playerState.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (playerState.isLiked) DipzonAccentRed else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Comment Button with Badge
                BadgedBox(
                    badge = {
                        if (comments.isNotEmpty()) {
                            Badge(
                                containerColor = DipzonPurplePrimary,
                                contentColor = Color.White
                            ) {
                                Text(text = "${comments.size}")
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { viewModel.setCommentSheetVisible(true) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("player_comment_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Yorumlar",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Save (Watchlist) Button
                IconButton(
                    onClick = { viewModel.toggleCurrentSeriesSave() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("player_save_btn")
                ) {
                    Icon(
                        imageVector = if (playerState.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Kaydet",
                        tint = if (playerState.isSaved) DipzonPurpleLight else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Dipzon'da '${series?.title}' dikey dizisini izliyorum! Sen de katıl: https://dipzon.tv/series/${series?.id}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Diziyi Paylaş"))
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("player_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Paylaş",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Animated Full Cinematic Controls HUD
        AnimatedVisibility(
            visible = playerState.isControlsVisible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("player_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = series?.title ?: "Dipzon Sinema",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "S${episode?.seasonNumber ?: 1}:B${episode?.episodeNumber ?: 1} - ${episode?.title ?: ""}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DipzonPurpleLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Top Action Badges (CC, Quality, Speed)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subtitle Toggle
                        IconButton(
                            onClick = { viewModel.toggleSubtitles() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (playerState.subtitlesEnabled) DipzonPurplePrimary else Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ClosedCaption,
                                contentDescription = "Altyazı",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Playback Speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { viewModel.setSpeedSheetVisible(true) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${playerState.playbackSpeed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Quality Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { viewModel.setQualitySheetVisible(true) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "HD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DipzonAccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // CENTER PLAYBACK CONTROLS
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { viewModel.seekRelative(-10L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("player_rewind_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Replay10,
                            contentDescription = "10s Geri",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Play / Pause Circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DipzonPurplePrimary)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("player_play_pause_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Duraklat" else "Oynat",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { viewModel.seekRelative(10L) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .testTag("player_forward_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Forward10,
                            contentDescription = "10s İleri",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // BOTTOM CONTROLS & TIMELINE
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Time and Episodes Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val curSec = playerState.currentPositionSeconds
                        val durSec = playerState.durationSeconds
                        val curText = String.format("%02d:%02d", curSec / 60, curSec % 60)
                        val durText = String.format("%02d:%02d", durSec / 60, durSec % 60)

                        Text(
                            text = "$curText / $durText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Next Episode Button
                            IconButton(
                                onClick = { viewModel.playNextEpisode() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Sonraki Bölüm",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Episode Drawer Button
                            Button(
                                onClick = { viewModel.setEpisodeSheetVisible(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("player_episodes_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.List,
                                    contentDescription = null,
                                    tint = DipzonPurpleLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bölümler",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Seek Slider
                    Slider(
                        value = playerState.currentPositionSeconds.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..playerState.durationSeconds.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = DipzonPurplePrimary,
                            inactiveTrackColor = DipzonBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("player_seek_slider")
                    )
                }
            }
        }

        // Episode List Sheet
        if (playerState.showEpisodeSheet) {
            EpisodeSelectorBottomSheet(
                episodes = playerState.episodes,
                currentEpisodeId = episode?.id,
                onEpisodeSelect = { ep ->
                    viewModel.switchEpisode(ep)
                },
                onDismiss = { viewModel.setEpisodeSheetVisible(false) }
            )
        }

        // Comment Sheet
        if (playerState.showCommentSheet) {
            CommentBottomSheet(
                comments = comments,
                onDismiss = { viewModel.setCommentSheetVisible(false) },
                onSendComment = { text, isSpoiler ->
                    viewModel.postComment(text, isSpoiler)
                },
                onUpvoteComment = { commentId ->
                    viewModel.upvoteComment(commentId)
                }
            )
        }

        // Playback Speed Dialog
        if (playerState.showSpeedSheet) {
            AlertDialog(
                onDismissRequest = { viewModel.setSpeedSheetVisible(false) },
                containerColor = DipzonSurface,
                title = {
                    Text(text = "Oynatma Hızı", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (playerState.playbackSpeed == speed) DipzonPurpleSubtle else Color.Transparent)
                                    .clickable { viewModel.setPlaybackSpeed(speed) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                    color = if (playerState.playbackSpeed == speed) DipzonPurpleLight else DipzonTextPrimary,
                                    fontWeight = if (playerState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                )
                                if (playerState.playbackSpeed == speed) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DipzonPurpleLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Quality Dialog
        if (playerState.showQualitySheet) {
            AlertDialog(
                onDismissRequest = { viewModel.setQualitySheetVisible(false) },
                containerColor = DipzonSurface,
                title = {
                    Text(text = "Video Kalitesi", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1080p FHD (Otomatik)", "720p HD", "480p SD", "Veri Tasarrufu").forEach { quality ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (playerState.quality == quality) DipzonPurpleSubtle else Color.Transparent)
                                    .clickable { viewModel.setQuality(quality) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quality,
                                    color = if (playerState.quality == quality) DipzonPurpleLight else DipzonTextPrimary,
                                    fontWeight = if (playerState.quality == quality) FontWeight.Bold else FontWeight.Normal
                                )
                                if (playerState.quality == quality) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DipzonPurpleLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}
