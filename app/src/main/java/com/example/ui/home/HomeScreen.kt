package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
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
import com.example.data.model.SeriesEntity
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.DipzonTopBar
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

@Composable
fun HomeScreen(
    viewModel: DipzonViewModel,
    onSeriesClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsState()
    val trendingSeries by viewModel.trendingSeries.collectAsState()
    val newSeries by viewModel.newSeries.collectAsState()
    val watchProgressList by viewModel.watchProgressList.collectAsState()
    val selectedGenre by viewModel.selectedHomeGenre.collectAsState()

    val genres = listOf("Tümü", "Gerilim", "Bilim Kurgu", "Aksiyon", "Romantik", "Fantastik", "Dram")

    // Filtered series list if a specific genre chip is active
    val filteredSeries = remember(selectedGenre, allSeries) {
        if (selectedGenre == "Tümü") allSeries
        else allSeries.filter { it.category.equals(selectedGenre, ignoreCase = true) }
    }

    // Hero Featured Item (e.g. first trending)
    val featuredHero = remember(allSeries) {
        allSeries.firstOrNull { it.isEditorChoice } ?: allSeries.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DipzonBlack)
    ) {
        // Sticky Top Branding Bar
        DipzonTopBar(
            onSearchClick = onSearchClick,
            onNotificationClick = onNotificationClick
        )

        // Genre Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                val isSelected = selectedGenre == genre
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DipzonPurplePrimary else DipzonCard)
                        .border(
                            width = 0.8.dp,
                            color = if (isSelected) DipzonPurpleLight else DipzonBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setHomeGenre(genre) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_$genre")
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else DipzonTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Main Feed Body
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // HERO SPOTLIGHT BANNER (Only on "Tümü" or matches filter)
            if (featuredHero != null && (selectedGenre == "Tümü" || featuredHero.category == selectedGenre)) {
                item {
                    FeaturedHeroBanner(
                        series = featuredHero,
                        onPlayClick = { viewModel.startPlaying(featuredHero.id) },
                        onDetailClick = { onSeriesClick(featuredHero.id) }
                    )
                }
            }

            // İZLEMEYE DEVAM ET (Continue Watching)
            if (watchProgressList.isNotEmpty() && selectedGenre == "Tümü") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "İzlemeye Devam Et",
                            badgeText = "${watchProgressList.size}"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(watchProgressList, key = { it.seriesId }) { prog ->
                                val s = allSeries.firstOrNull { it.id == prog.seriesId }
                                if (s != null) {
                                    ContinueWatchingCard(
                                        series = s,
                                        progress = prog,
                                        onResumeClick = {
                                            viewModel.startPlaying(prog.seriesId, prog.episodeId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SENİN İÇİN (For You)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = if (selectedGenre == "Tümü") "Senin İçin Seçtiklerimiz" else "$selectedGenre Yapımları",
                        subtitle = "Tercihlerinize göre özel hazırlandı"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSeries, key = { it.id }) { s ->
                            SeriesPosterCard(
                                series = s,
                                onClick = { onSeriesClick(s.id) }
                            )
                        }
                    }
                }
            }

            // DIPZON'DA POPÜLER (Trending with rank badges)
            if (trendingSeries.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Dipzon'da Popüler",
                            subtitle = "Bugün Türkiye'de en çok izlenenler"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(trendingSeries, key = { _, s -> s.id }) { index, s ->
                                SeriesPosterCard(
                                    series = s,
                                    showRank = index + 1,
                                    onClick = { onSeriesClick(s.id) }
                                )
                            }
                        }
                    }
                }
            }

            // YENİ DİZİLER & BÖLÜMLER
            if (newSeries.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Yeni Diziler & Sezonlar",
                            subtitle = "Haftanın en taze dikey prömiyerleri"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(newSeries, key = { it.id }) { s ->
                                SeriesPosterCard(
                                    series = s,
                                    onClick = { onSeriesClick(s.id) }
                                )
                            }
                        }
                    }
                }
            }

            // GENRE SPECIFIC SECTION: GERİLİM
            val thrillerSeries = allSeries.filter { it.category == "Gerilim" }
            if (thrillerSeries.isNotEmpty() && selectedGenre == "Tümü") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Soluksuz Gerilim",
                            subtitle = "Kısa ve nefes kesen gizemler"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(thrillerSeries, key = { it.id }) { s ->
                                SeriesPosterCard(
                                    series = s,
                                    onClick = { onSeriesClick(s.id) }
                                )
                            }
                        }
                    }
                }
            }

            // GENRE SPECIFIC SECTION: BİLİM KURGU & FANTASTİK
            val sciFiSeries = allSeries.filter { it.category in listOf("Bilim Kurgu", "Fantastik") }
            if (sciFiSeries.isNotEmpty() && selectedGenre == "Tümü") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Bilim Kurgu & Fantastik",
                            subtitle = "Geleceğin dikey evrenleri"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sciFiSeries, key = { it.id }) { s ->
                                SeriesPosterCard(
                                    series = s,
                                    onClick = { onSeriesClick(s.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DipzonPurplePrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun FeaturedHeroBanner(
    series: SeriesEntity,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DipzonCard)
            .border(1.dp, DipzonPurpleSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onDetailClick)
            .testTag("hero_banner")
    ) {
        // Hero Backdrop
        AsyncImage(
            model = series.backdropUrl.ifEmpty { series.posterUrl },
            contentDescription = series.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            DipzonBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Badge at Top Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DipzonPurplePrimary)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "EDİTÖRÜN SEÇİMİ",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Bottom Info & Dual CTAs
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = series.category,
                    color = DipzonPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(text = "•", color = DipzonTextMuted, fontSize = 11.sp)
                Text(
                    text = "%${series.matchRate} Eşleşme",
                    color = DipzonAccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(text = "•", color = DipzonTextMuted, fontSize = 11.sp)
                Text(
                    text = "${series.totalEpisodes} Bölüm",
                    color = DipzonTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = series.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 22.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = series.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DipzonTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DipzonPurplePrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("hero_play_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "İzlemeye Başla",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onDetailClick,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(DipzonBorder, DipzonPurpleSubtle))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("hero_detail_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Detaylar",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
