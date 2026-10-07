package com.example.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SeriesPosterCard
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel
import com.example.viewmodel.Screen

@Composable
fun WatchlistScreen(
    viewModel: DipzonViewModel,
    onSeriesClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()

    var selectedTab by remember { mutableStateOf("Tümü") }
    val tabs = listOf("Tümü", "Diziler", "Kısa Filmler", "İndirilenler")

    val savedSeriesList = remember(allSeries, savedItems) {
        val savedIds = savedItems.map { it.seriesId }.toSet()
        allSeries.filter { savedIds.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DipzonBlack)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Listem",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "${savedSeriesList.size} Yapım Kaydedildi",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonPurpleLight,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DipzonPurplePrimary else DipzonCard)
                        .border(0.5.dp, if (isSelected) DipzonPurpleLight else DipzonBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else DipzonTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content or Empty State
        if (savedSeriesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DipzonPurpleSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = DipzonPurpleLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Listen Henüz Boş",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "Beğendiğin dikey dizileri '+ Listem' butonuna basarak buraya ekleyebilirsin. Cihazların arasında otomatik senkronize olur.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DipzonTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )

                    Button(
                        onClick = { viewModel.navigateTo(Screen.DISCOVER) },
                        colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_explore_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Keşfet'e Göz At",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(savedSeriesList, key = { it.id }) { s ->
                    Box {
                        SeriesPosterCard(
                            series = s,
                            width = 110.dp,
                            onClick = { onSeriesClick(s.id) }
                        )

                        // Remove icon overlay at top right
                        IconButton(
                            onClick = { viewModel.toggleSeriesSave(s.id, true) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkRemove,
                                contentDescription = "Listeden Kaldır",
                                tint = DipzonAccentRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
