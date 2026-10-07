package com.example.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TrendingUp
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

@Composable
fun SearchScreen(
    viewModel: DipzonViewModel,
    onSeriesClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val trendingSearches by viewModel.trendingSearches.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().background(DipzonBlack).statusBarsPadding()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Dizi, oyuncu, yönetmen veya tür ara...", color = DipzonTextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Ara", tint = DipzonPurpleLight) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, "Temizle", tint = DipzonTextSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("search_text_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DipzonPurplePrimary, unfocusedBorderColor = DipzonBorder,
                focusedContainerColor = DipzonCard, unfocusedContainerColor = DipzonCard,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                if (recentSearches.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.History, null, tint = DipzonPurpleLight, modifier = Modifier.size(18.dp))
                                Text("Son Aramalar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = viewModel::clearAllRecentSearches) { Text("Temizle", color = DipzonTextSecondary, fontSize = 11.sp) }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentSearches, key = { it }) { tag ->
                                Row(
                                    modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(DipzonCard)
                                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(18.dp))
                                        .clickable { viewModel.onSearchQueryChanged(tag) }
                                        .padding(start = 12.dp, end = 6.dp, top = 7.dp, bottom = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tag, color = DipzonTextSecondary, fontSize = 11.sp)
                                    IconButton(onClick = { viewModel.clearRecentSearch(tag) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, "Sil", tint = DipzonTextMuted, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (trendingSearches.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.TrendingUp, null, tint = DipzonAccentGold, modifier = Modifier.size(18.dp))
                            Text("Gerçek Arama Eğilimleri", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        trendingSearches.forEachIndexed { index, tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onSearchQueryChanged(tag); viewModel.addRecentSearch(tag) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("${index + 1}", color = if (index < 3) DipzonPurpleLight else DipzonTextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                Text(tag, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (recentSearches.isEmpty() && trendingSearches.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 72.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Search, null, tint = DipzonPurpleLight, modifier = Modifier.size(34.dp))
                            Text("Henüz arama geçmişi yok", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Yaptığın gerçek aramalar burada görünecek.", color = DipzonTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sonuç Bulunamadı", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("'$searchQuery' ile eşleşen içerik bulunamadı.", color = DipzonTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Text("${searchResults.size} sonuç", color = DipzonTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                gridItems(searchResults, key = { it.id }) { series ->
                    SeriesPosterCard(series = series, width = 110.dp, onClick = {
                        viewModel.addRecentSearch(searchQuery)
                        onSeriesClick(series.id)
                    })
                }
            }
        }
    }
}
