package com.example.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
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
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

enum class DetailTab(val label: String) {
    EPISODES("Bölümler"),
    TRAILER("Fragman"),
    CAST("Oyuncular"),
    SIMILAR("Benzer Yapımlar")
}

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    viewModel: DipzonViewModel,
    onBack: () -> Unit,
    onPlayClick: (String, String?) -> Unit,
    onSimilarSeriesClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    val watchProgressList by viewModel.watchProgressList.collectAsState()

    val series = remember(seriesId, allSeries) {
        allSeries.firstOrNull { it.id == seriesId }
    }

    val episodes = remember(seriesId) {
        // Collect episodes from repository directly or via sample
        mutableStateOf<List<EpisodeEntity>>(emptyList())
    }

    LaunchedEffect(seriesId) {
        viewModel.repository.getEpisodesForSeries(seriesId).collect {
            episodes.value = it
        }
    }

    val isSaved = savedItems.any { it.seriesId == seriesId }
    val progress = watchProgressList.firstOrNull { it.seriesId == seriesId }

    var selectedTab by remember { mutableStateOf(DetailTab.EPISODES) }

    BackHandler {
        onBack()
    }

    if (series == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DipzonBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DipzonPurplePrimary)
        }
        return
    }

    val similarSeries = remember(series, allSeries) {
        allSeries.filter { it.id != series.id && (it.category == series.category || it.isTrending) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DipzonBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // HERO BACKDROP HEADER (320dp height)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
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
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        DipzonBlack.copy(alpha = 0.7f),
                                        DipzonBlack
                                    )
                                )
                            )
                    )

                    // Top Back Button & Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Paylaş",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // SERIES METADATA & CTAs
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Title
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 26.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Meta Row: Match Rate, Year, Age, Seasons, Episodes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "%${series.matchRate} Eşleşme",
                            color = DipzonAccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "${series.year}",
                            color = DipzonTextSecondary,
                            fontSize = 12.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DipzonCard)
                                .border(0.5.dp, DipzonBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = series.ageRating,
                                color = DipzonTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${series.totalSeasons} Sezon • ${series.totalEpisodes} Bölüm",
                            color = DipzonTextSecondary,
                            fontSize = 12.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DipzonPurplePrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "9:16 DİKEY",
                                color = DipzonPurpleLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PRIMARY ACTIONS: Play & My List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Play Button
                        Button(
                            onClick = {
                                onPlayClick(series.id, progress?.episodeId)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DipzonPurplePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("detail_play_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (progress != null) "Kaldığın Yerden (B${progress.episodeNumber})" else "İzlemeye Başla (1. Bölüm)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // My List Button
                        OutlinedButton(
                            onClick = { viewModel.toggleSeriesSave(series.id, isSaved) },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(DipzonBorder, DipzonPurpleSubtle))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = DipzonCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("detail_save_btn")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Check else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isSaved) DipzonPurpleLight else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSaved) "Listemde" else "Listem",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Synopsis
                    Text(
                        text = series.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DipzonTextPrimary,
                            lineHeight = 20.sp,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Director & Cast summary
                    Text(
                        text = "Yönetmen: ${series.director}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DipzonTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Oyuncular: ${series.cast}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DipzonTextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // TABS ROW (Bölümler, Fragman, Oyuncular, Benzer Yapımlar)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DipzonBlack,
                    contentColor = DipzonPurpleLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = DipzonPurplePrimary,
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(color = DipzonBorder)
                    }
                ) {
                    DetailTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == tab) Color.White else DipzonTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // TAB 1: BÖLÜMLER (Episodes List)
            if (selectedTab == DetailTab.EPISODES) {
                if (episodes.value.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DipzonPurplePrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    items(episodes.value, key = { it.id }) { ep ->
                        val durationMin = ep.durationSeconds / 60
                        val durationSec = ep.durationSeconds % 60
                        val durationStr = String.format("%02d:%02d", durationMin, durationSec)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DipzonCard)
                                .border(0.5.dp, DipzonBorder, RoundedCornerShape(10.dp))
                                .clickable { onPlayClick(series.id, ep.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = ep.thumbnailUrl,
                                    contentDescription = ep.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${ep.episodeNumber}. Bölüm: ${ep.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = durationStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DipzonPurpleLight,
                                        fontSize = 11.sp
                                    )
                                )

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
                    }
                }
            }

            // TAB 2: FRAGMAN (Trailer)
            if (selectedTab == DetailTab.TRAILER) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Resmi Tanıtım Fragmanı",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .clickable { onPlayClick(series.id, episodes.value.firstOrNull()?.id) }
                        ) {
                            AsyncImage(
                                model = series.backdropUrl.ifEmpty { series.posterUrl },
                                contentDescription = "Fragman",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(DipzonPurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Fragmanı Oynat",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // TAB 3: OYUNCULAR (Cast & Crew)
            if (selectedTab == DetailTab.CAST) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Başrol ve Ekip",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        val castList = series.cast.split(",").map { it.trim() }
                        castList.forEach { actor ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DipzonCard)
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DipzonPurpleSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = actor.take(1),
                                        color = DipzonPurpleLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = actor,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Oyuncu",
                                        color = DipzonTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Director
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DipzonCard)
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DipzonPurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = series.director.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(
                                    text = series.director,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Yönetmen",
                                    color = DipzonPurpleLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // TAB 4: BENZER YAPIMLAR (Similar Series)
            if (selectedTab == DetailTab.SIMILAR) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Bu Diziyi Beğenenler Bunları da İzledi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarSeries, key = { it.id }) { item ->
                                SeriesPosterCard(
                                    series = item,
                                    onClick = { onSimilarSeriesClick(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
