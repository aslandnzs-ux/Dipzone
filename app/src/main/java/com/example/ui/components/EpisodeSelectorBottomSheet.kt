package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EpisodeEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeSelectorBottomSheet(
    episodes: List<EpisodeEntity>,
    currentEpisodeId: String?,
    onEpisodeSelect: (EpisodeEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DipzonSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DipzonBorder)
            )
        },
        modifier = modifier.fillMaxHeight(0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bölümler",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Sezon 1 • Toplam ${episodes.size} Bölüm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DipzonPurpleLight,
                            fontSize = 11.sp
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = DipzonTextSecondary
                    )
                }
            }

            HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

            // Episodes List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(episodes, key = { it.id }) { ep ->
                    val isCurrent = ep.id == currentEpisodeId
                    val durationMin = ep.durationSeconds / 60
                    val durationSec = ep.durationSeconds % 60
                    val durationStr = String.format("%02d:%02d", durationMin, durationSec)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) DipzonPurpleSubtle else DipzonCard)
                            .border(
                                width = if (isCurrent) 1.dp else 0.5.dp,
                                color = if (isCurrent) DipzonPurplePrimary else DipzonBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onEpisodeSelect(ep)
                                onDismiss()
                            }
                            .padding(8.dp)
                            .testTag("ep_item_${ep.episodeNumber}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thumbnail with Play / Lock badge
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = ep.thumbnailUrl,
                                contentDescription = ep.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Oynatılıyor",
                                        tint = DipzonPurpleLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else if (!ep.isFree) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Kilitli",
                                        tint = DipzonAccentGold,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        // Episode Meta
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Bölüm ${ep.episodeNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) DipzonPurpleLight else DipzonTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "•",
                                    color = DipzonTextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = durationStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DipzonTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Text(
                                text = ep.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else DipzonTextPrimary,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (ep.synopsis.isNotEmpty()) {
                                Text(
                                    text = ep.synopsis,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DipzonTextMuted,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right Status Indicator
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DipzonPurplePrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OYNATILIYOR",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
