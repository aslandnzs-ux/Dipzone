package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeriesEntity
import com.example.data.model.WatchProgressEntity
import com.example.ui.theme.*

@Composable
fun ContinueWatchingCard(
    series: SeriesEntity,
    progress: WatchProgressEntity,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (progress.durationSeconds > 0) {
        (progress.positionSeconds.toFloat() / progress.durationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val remainingSec = (progress.durationSeconds - progress.positionSeconds).coerceAtLeast(0L)
    val remainingText = "${remainingSec / 60}:${(remainingSec % 60).toString().padStart(2, '0')} kaldı"

    Box(
        modifier = modifier
            .width(220.dp)
            .height(136.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DipzonCard)
            .border(0.8.dp, DipzonPurpleSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onResumeClick)
            .testTag("continue_${series.id}")
    ) {
        // Backdrop Image
        AsyncImage(
            model = series.backdropUrl.ifEmpty { series.posterUrl },
            contentDescription = series.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Center Play Circle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(38.dp)
                .clip(CircleShape)
                .background(DipzonPurplePrimary.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Oynat",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Info at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = series.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S${progress.seasonNumber}:B${progress.episodeNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonPurpleLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )

                Text(
                    text = remainingText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextSecondary,
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = DipzonPurplePrimary,
                trackColor = DipzonBorder
            )
        }
    }
}
