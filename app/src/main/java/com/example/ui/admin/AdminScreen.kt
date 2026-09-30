package com.example.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeriesEntity
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

enum class AdminTab(val label: String) {
    ANALYTICS("Analitik"),
    CONTENT("İçerik Yönetimi"),
    MODERATION("Yorum & Moderasyon")
}

@Composable
fun AdminScreen(
    viewModel: DipzonViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val adminSeries by viewModel.adminAllSeries.collectAsState()
    val comments by viewModel.currentSeriesComments.collectAsState()

    var selectedTab by remember { mutableStateOf(AdminTab.ANALYTICS) }
    var showAddSeriesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshAdminStats()
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DipzonBlack),
        containerColor = DipzonBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(DipzonSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = "DIPZON YÖNETİM MERKEZİ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "İçerik ve Platform Kontrol Konsolu",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DipzonPurpleLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    if (selectedTab == AdminTab.CONTENT) {
                        Button(
                            onClick = { showAddSeriesDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("admin_add_series_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Yeni Dizi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DipzonSurface,
                    contentColor = DipzonPurpleLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = DipzonPurplePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    AdminTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.label,
                                    color = if (selectedTab == tab) Color.White else DipzonTextSecondary,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AdminTab.ANALYTICS -> AnalyticsTabContent(adminSeries)
                AdminTab.CONTENT -> ContentManagementTabContent(
                    seriesList = adminSeries,
                    onTogglePublish = { id, status -> viewModel.toggleSeriesPublished(id, status) },
                    onDelete = { id -> viewModel.deleteSeries(id) }
                )
                AdminTab.MODERATION -> ModerationTabContent(
                    comments = comments,
                    onDeleteComment = { id -> viewModel.repository.deleteComment(id) }
                )
            }
        }

        // Add Series Dialog
        if (showAddSeriesDialog) {
            AddSeriesDialog(
                onDismiss = { showAddSeriesDialog = false },
                onAdd = { newSeries ->
                    viewModel.addSeries(newSeries)
                    showAddSeriesDialog = false
                }
            )
        }
    }
}

@Composable
private fun AnalyticsTabContent(seriesList: List<SeriesEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP KPI GRID
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Günlük Aktif (DAU)",
                        value = "48.240",
                        sub = "+%14.2 bu hafta",
                        color = DipzonAccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Aylık Aktif (MAU)",
                        value = "284.100",
                        sub = "+%28.6 bu ay",
                        color = DipzonPurpleLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Toplam İzlenme",
                        value = "1.42M Saat",
                        sub = "Ort. 2m 45s / bölüm",
                        color = DipzonAccentGold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Tutma Oranı",
                        value = "%78.4",
                        sub = "Bölüm tamamlama",
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // WEEKLY WATCHTIME BAR CHART
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DipzonCard)
                    .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Haftalık İzlenme Saatleri (bin saat)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Pazartesi - Pazar dikey video akışı",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextMuted,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                val weeklyData = listOf(140f, 180f, 210f, 260f, 340f, 420f, 390f)
                val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = 24.dp.toPx()
                    val spacing = (canvasWidth - (barWidth * weeklyData.size)) / (weeklyData.size + 1)
                    val maxVal = 450f

                    weeklyData.forEachIndexed { i, value ->
                        val left = spacing + i * (barWidth + spacing)
                        val barHeight = (value / maxVal) * canvasHeight
                        val top = canvasHeight - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(DipzonPurpleLight, DipzonPurplePrimary)
                            ),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            color = DipzonTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // TOP PERFORMING SERIES TABLE
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DipzonCard)
                    .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "En Çok İzlenen Yapımlar",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                seriesList.take(4).forEachIndexed { idx, s ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${idx + 1}",
                                color = DipzonPurpleLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Column {
                                Text(
                                    text = s.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${s.category} • %${s.matchRate} beğeni",
                                    color = DipzonTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "${s.viewsCount / 1000}B İzlenme",
                            color = DipzonAccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    if (idx < 3) HorizontalDivider(color = DipzonBorder.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DipzonCard)
            .border(0.5.dp, DipzonBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = DipzonTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, color = DipzonTextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ContentManagementTabContent(
    seriesList: List<SeriesEntity>,
    onTogglePublish: (id: String, currentStatus: Boolean) -> Unit,
    onDelete: (id: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(seriesList, key = { it.id }) { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DipzonCard)
                    .border(0.5.dp, DipzonBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Poster thumbnail
                AsyncImage(
                    model = s.posterUrl,
                    contentDescription = s.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp, 76.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                // Meta Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = s.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (s.isPublished) DipzonAccentGreen.copy(alpha = 0.2f) else DipzonAccentRed.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (s.isPublished) "YAYINDA" else "TASLAK",
                                color = if (s.isPublished) DipzonAccentGreen else DipzonAccentRed,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${s.category} • ${s.totalEpisodes} Bölüm • ${s.year}",
                        color = DipzonTextMuted,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "Yönetmen: ${s.director}",
                        color = DipzonTextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Actions: Publish Toggle & Delete
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = s.isPublished,
                        onCheckedChange = { onTogglePublish(s.id, s.isPublished) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DipzonPurplePrimary,
                            uncheckedThumbColor = DipzonTextMuted,
                            uncheckedTrackColor = DipzonBorder
                        ),
                        modifier = Modifier.size(36.dp, 24.dp)
                    )

                    IconButton(
                        onClick = { onDelete(s.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Sil",
                            tint = DipzonAccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModerationTabContent(
    comments: List<com.example.data.model.CommentEntity>,
    onDeleteComment: suspend (id: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (comments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bekleyen şüpheli yorum veya şikayet bulunmuyor.",
                        color = DipzonTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(comments, key = { it.id }) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DipzonCard)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = c.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (c.isSpoiler) {
                                Text(text = "[SPOILER]", color = DipzonAccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(text = c.text, color = DipzonTextSecondary, fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                onDeleteComment(c.id)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Kaldır", tint = DipzonAccentRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSeriesDialog(
    onDismiss: () -> Unit,
    onAdd: (SeriesEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Gerilim") }
    var description by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var cast by remember { mutableStateOf("") }
    var episodesCount by remember { mutableStateOf("6") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DipzonSurface,
        title = {
            Text(text = "Yeni 9:16 Dizi Ekle", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Dizi Adı") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (Gerilim, Bilim Kurgu...)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Sinopsis") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = director,
                    onValueChange = { director = it },
                    label = { Text("Yönetmen") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = cast,
                    onValueChange = { cast = it },
                    label = { Text("Oyuncular") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newId = "ser_${System.currentTimeMillis()}"
                        val newEntity = SeriesEntity(
                            id = newId,
                            title = title.trim(),
                            description = description.ifBlank { "Dipzon özel dikey dizi yapımı." },
                            category = category.trim(),
                            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                            year = 2025,
                            ageRating = "16+",
                            totalEpisodes = episodesCount.toIntOrNull() ?: 6,
                            totalSeasons = 1,
                            director = director.ifBlank { "Dipzon Stüdyoları" },
                            cast = cast.ifBlank { "Oyuncu Kadrosu" },
                            isPublished = true
                        )
                        onAdd(newEntity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                enabled = title.isNotBlank()
            ) {
                Text(text = "Kaydet ve Yayınla", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal", color = Color.White)
            }
        }
    )
}
