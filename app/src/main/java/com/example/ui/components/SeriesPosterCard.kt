package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeriesEntity
import com.example.ui.theme.*

@Composable
fun SeriesPosterCard(
    series: SeriesEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 130.dp,
    showRank: Int? = null
) {
    // 9:16 Aspect Ratio calculation (e.g. 130dp width -> ~230dp height)
    val height = width * 16f / 9f

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(DipzonCard)
            .border(0.8.dp, DipzonBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("poster_${series.id}")
    ) {
        // Poster Image
        AsyncImage(
            model = series.posterUrl,
            contentDescription = series.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim for readable title
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            DipzonBlack.copy(alpha = 0.4f),
                            DipzonBlack.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // Top Badges (New, 4K, or Category)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (series.isNew) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DipzonPurplePrimary)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "YENİ",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (series.ageRating.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = series.ageRating,
                        color = DipzonTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Rank Number if in Trending
        if (showRank != null) {
            Text(
                text = "$showRank",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = DipzonPurpleLight.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 24.dp)
            )
        }

        // Title and Season Info at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(8.dp)
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
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${series.totalEpisodes} Bölüm",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextSecondary,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextMuted,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = "%${series.matchRate}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonAccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
