package com.example.ui.admin

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CommentEntity
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.example.data.repository.AdminSnapshot
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

private enum class AdminTab(val label: String) { OVERVIEW("Durum"), CONTENT("İçerik"), MODERATION("Yorumlar") }

@Composable
fun AdminScreen(viewModel: DipzonViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val auth by viewModel.adminAuthState.collectAsState()
    BackHandler(onBack = onBack)
    if (!auth.isAuthenticated) {
        AdminLoginScreen(viewModel, onBack, modifier)
        return
    }

    val series by viewModel.adminAllSeries.collectAsState()
    val stats by viewModel.adminStats.collectAsState()
    val comments by viewModel.adminComments.collectAsState()
    var tab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    var editSeries by remember { mutableStateOf<SeriesEntity?>(null) }
    var showNewSeries by remember { mutableStateOf(false) }
    var episodeSeries by remember { mutableStateOf<SeriesEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.refreshAdminStats() }

    Scaffold(
        modifier = modifier.fillMaxSize(), containerColor = DipzonBlack,
        topBar = {
            Column(Modifier.fillMaxWidth().statusBarsPadding().background(DipzonSurface)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) }
                        Column {
                            Text("DIPZON YÖNETİM", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(auth.email, color = DipzonPurpleLight, fontSize = 10.sp)
                        }
                    }
                    Row {
                        if (tab == AdminTab.CONTENT) IconButton(onClick = { showNewSeries = true }) { Icon(Icons.Default.AddCircle, "Yeni dizi", tint = DipzonPurpleLight) }
                        IconButton(onClick = viewModel::signOutAdmin) { Icon(Icons.Outlined.Logout, "Çıkış", tint = DipzonTextSecondary) }
                    }
                }
                TabRow(
                    selectedTabIndex = tab.ordinal, containerColor = DipzonSurface,
                    indicator = { positions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[tab.ordinal]), color = DipzonPurplePrimary) }
                ) {
                    AdminTab.entries.forEach { item ->
                        Tab(selected = tab == item, onClick = { tab = item }, text = { Text(item.label, color = if (tab == item) Color.White else DipzonTextMuted) })
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                AdminTab.OVERVIEW -> RealAnalytics(stats)
                AdminTab.CONTENT -> ContentManager(
                    series = series,
                    onNew = { showNewSeries = true },
                    onEdit = { editSeries = it },
                    onEpisodes = { episodeSeries = it },
                    onPublish = { viewModel.toggleSeriesPublished(it.id, it.isPublished) },
                    onDelete = { viewModel.deleteSeries(it.id) }
                )
                AdminTab.MODERATION -> Moderation(comments, viewModel::deleteAdminComment)
            }
        }
    }

    if (showNewSeries) SeriesEditorDialog(null, viewModel, { showNewSeries = false }) { viewModel.addSeries(it); showNewSeries = false }
    editSeries?.let { current -> SeriesEditorDialog(current, viewModel, { editSeries = null }) { viewModel.addSeries(it); editSeries = null } }
    episodeSeries?.let { current -> EpisodeManagerDialog(current, viewModel, onDismiss = { episodeSeries = null }) }
}

@Composable
private fun AdminLoginScreen(viewModel: DipzonViewModel, onBack: () -> Unit, modifier: Modifier) {
    val auth by viewModel.adminAuthState.collectAsState()
    var email by remember { mutableStateOf(auth.email) }
    var password by remember { mutableStateOf("") }
    Box(modifier.fillMaxSize().background(DipzonBlack).statusBarsPadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).clip(RoundedCornerShape(18.dp)).background(DipzonCard)
                .border(1.dp, DipzonBorder, RoundedCornerShape(18.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White) }
            Icon(Icons.Outlined.AdminPanelSettings, null, tint = DipzonPurpleLight, modifier = Modifier.size(40.dp))
            Text("Yönetici Girişi", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text("Yalnızca Firebase'de admin olarak yetkilendirilmiş hesaplar giriş yapabilir.", color = DipzonTextSecondary, fontSize = 12.sp)
            OutlinedTextField(email, { email = it }, label = { Text("E-posta") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
            OutlinedTextField(password, { password = it }, label = { Text("Şifre") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
            auth.error?.let { Text(it, color = DipzonAccentRed, fontSize = 11.sp) }
            Button(
                onClick = { viewModel.signInAdmin(email, password) }, enabled = !auth.isChecking,
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)
            ) {
                if (auth.isChecking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White) else Text("Giriş Yap")
            }
        }
    }
}

@Composable
private fun RealAnalytics(stats: AdminSnapshot) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            Text("Gerçek platform verileri", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Bu ekranda sabit veya uydurma sayı yoktur. Veriler Room olay kayıtlarından gelir.", color = DipzonTextMuted, fontSize = 11.sp)
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("Dizi", stats.seriesCount.toString(), Modifier.weight(1f)); Metric("Bölüm", stats.episodeCount.toString(), Modifier.weight(1f)) } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("Oynatma", stats.playCount.toString(), Modifier.weight(1f)); Metric("Tamamlama", stats.completionCount.toString(), Modifier.weight(1f)) } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("Arama", stats.searchCount.toString(), Modifier.weight(1f)); Metric("Yorum", stats.commentCount.toString(), Modifier.weight(1f)) } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("Beğeni olayı", stats.likeCount.toString(), Modifier.weight(1f)); Metric("Kayıtlı izleme", "${stats.savedWatchSeconds / 60} dk", Modifier.weight(1f)) } }
    }
}

@Composable private fun Metric(title: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(DipzonCard).border(.5.dp, DipzonBorder, RoundedCornerShape(12.dp)).padding(14.dp)) {
        Text(title, color = DipzonTextMuted, fontSize = 11.sp); Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
private fun ContentManager(
    series: List<SeriesEntity>, onNew: () -> Unit, onEdit: (SeriesEntity) -> Unit,
    onEpisodes: (SeriesEntity) -> Unit, onPublish: (SeriesEntity) -> Unit, onDelete: (SeriesEntity) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 50.dp)) {
        item {
            Button(onClick = onNew, colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Yeni Dizi")
            }
        }
        if (series.isEmpty()) item { Text("Henüz içerik yok.", color = DipzonTextMuted) }
        items(series, key = { it.id }) { item ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DipzonCard).border(.5.dp, DipzonBorder, RoundedCornerShape(12.dp)).padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(item.posterUrl, item.title, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp, 74.dp).clip(RoundedCornerShape(6.dp)))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${item.category} • ${item.totalEpisodes} bölüm", color = DipzonTextMuted, fontSize = 11.sp)
                        Text(if (item.isPublished) "Yayında" else "Taslak", color = if (item.isPublished) DipzonAccentGreen else DipzonAccentGold, fontSize = 10.sp)
                    }
                    Switch(checked = item.isPublished, onCheckedChange = { onPublish(item) })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onEpisodes(item) }) { Text("Bölümler", color = DipzonPurpleLight) }
                    TextButton(onClick = { onEdit(item) }) { Text("Düzenle", color = DipzonTextSecondary) }
                    IconButton(onClick = { onDelete(item) }) { Icon(Icons.Outlined.Delete, "Sil", tint = DipzonAccentRed) }
                }
            }
        }
    }
}

@Composable
private fun SeriesEditorDialog(initial: SeriesEntity?, viewModel: DipzonViewModel, onDismiss: () -> Unit, onSave: (SeriesEntity) -> Unit) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "Gerilim") }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var director by remember(initial) { mutableStateOf(initial?.director.orEmpty()) }
    var cast by remember(initial) { mutableStateOf(initial?.cast.orEmpty()) }
    var posterUrl by remember(initial) { mutableStateOf(initial?.posterUrl.orEmpty()) }
    var backdropUrl by remember(initial) { mutableStateOf(initial?.backdropUrl.orEmpty()) }
    var year by remember(initial) { mutableStateOf((initial?.year ?: 2026).toString()) }
    var rating by remember(initial) { mutableStateOf(initial?.ageRating ?: "16+") }
    var publish by remember(initial) { mutableStateOf(initial?.isPublished ?: false) }
    var posterUri by remember { mutableStateOf<Uri?>(null) }
    var backdropUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val upload by viewModel.uploadState.collectAsState()
    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { posterUri = it }
    val backdropPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { backdropUri = it }

    fun saveWithMedia(finalPoster: String, finalBackdrop: String) {
        val id = initial?.id ?: "ser_${System.currentTimeMillis()}"
        val base = initial ?: SeriesEntity(
            id = id, title = "", description = "", category = "", posterUrl = "", backdropUrl = "", trailerUrl = "",
            year = 2026, ageRating = "16+", totalEpisodes = 0, totalSeasons = 1, director = "", cast = ""
        )
        onSave(base.copy(
            title = title.trim(), description = description.trim(), category = category.trim(), posterUrl = finalPoster,
            backdropUrl = finalBackdrop, year = year.toIntOrNull() ?: 2026, ageRating = rating.trim(),
            director = director.trim(), cast = cast.trim(), isPublished = publish
        ))
        viewModel.resetUploadState()
    }

    fun uploadAndSave() {
        error = null
        val basePath = "${initial?.id ?: "series_${System.currentTimeMillis()}"}/${System.currentTimeMillis()}"
        val p = posterUri
        val b = backdropUri
        if (p != null) {
            viewModel.uploadMedia(p, "thumbnails/$basePath-poster.jpg") { pResult ->
                pResult.onSuccess { uploadedPoster ->
                    if (b != null) {
                        viewModel.uploadMedia(b, "thumbnails/$basePath-backdrop.jpg") { bResult ->
                            bResult.onSuccess { saveWithMedia(uploadedPoster, it) }.onFailure { error = it.localizedMessage }
                        }
                    } else saveWithMedia(uploadedPoster, backdropUrl)
                }.onFailure { error = it.localizedMessage }
            }
        } else if (b != null) {
            viewModel.uploadMedia(b, "thumbnails/$basePath-backdrop.jpg") { result ->
                result.onSuccess { saveWithMedia(posterUrl, it) }.onFailure { error = it.localizedMessage }
            }
        } else saveWithMedia(posterUrl, backdropUrl)
    }

    AlertDialog(
        onDismissRequest = { if (!upload.isUploading) onDismiss() }, containerColor = DipzonSurface,
        title = { Text(if (initial == null) "Yeni Dizi" else "Diziyi Düzenle", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                item { AdminField(title, { title = it }, "Dizi Adı") }
                item { AdminField(category, { category = it }, "Kategori") }
                item { AdminField(description, { description = it }, "Açıklama", singleLine = false) }
                item { AdminField(director, { director = it }, "Yönetmen") }
                item { AdminField(cast, { cast = it }, "Oyuncular") }
                item { OutlinedButton(onClick = { posterPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Image, null); Text(if (posterUri == null) " Poster Seç" else " Poster Seçildi ✓") } }
                item { AdminField(posterUrl, { posterUrl = it }, "Poster URL (isteğe bağlı)") }
                item { OutlinedButton(onClick = { backdropPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Image, null); Text(if (backdropUri == null) " Arka Plan Seç" else " Arka Plan Seçildi ✓") } }
                item { AdminField(backdropUrl, { backdropUrl = it }, "Arka Plan URL (isteğe bağlı)") }
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.weight(1f)) { AdminField(year, { year = it }, "Yıl") }; Box(Modifier.weight(1f)) { AdminField(rating, { rating = it }, "Yaş") } } }
                item { Row(verticalAlignment = Alignment.CenterVertically) { Switch(publish, { publish = it }); Spacer(Modifier.width(8.dp)); Text(if (publish) "Yayında" else "Taslak", color = Color.White) } }
                if (upload.isUploading) item { Column { LinearProgressIndicator(progress = upload.progress / 100f, modifier = Modifier.fillMaxWidth()); Text("Yükleniyor: %${upload.progress}", color = DipzonTextSecondary, fontSize = 11.sp) } }
                error?.let { message -> item { Text(message, color = DipzonAccentRed, fontSize = 11.sp) } }
            }
        },
        confirmButton = { Button(onClick = ::uploadAndSave, enabled = title.isNotBlank() && !upload.isUploading, colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)) { Text(if (upload.isUploading) "Yükleniyor" else "Kaydet") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !upload.isUploading) { Text("İptal", color = Color.White) } }
    )
}

@Composable
private fun EpisodeManagerDialog(series: SeriesEntity, viewModel: DipzonViewModel, onDismiss: () -> Unit) {
    val episodes by viewModel.episodesForSeries(series.id).collectAsState(initial = emptyList())
    var editing by remember { mutableStateOf<EpisodeEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = DipzonSurface,
        title = { Text("${series.title} • Bölümler", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 500.dp)) {
                item { Button(onClick = { adding = true }, colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)) { Icon(Icons.Default.Add, null); Text(" Bölüm Ekle") } }
                if (episodes.isEmpty()) item { Text("Henüz bölüm eklenmedi.", color = DipzonTextMuted) }
                items(episodes, key = { it.id }) { ep ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DipzonCard).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("S${ep.seasonNumber} B${ep.episodeNumber} • ${ep.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(if (ep.videoUrl.isBlank()) "Video yok" else "Video hazır", color = if (ep.videoUrl.isBlank()) DipzonAccentRed else DipzonAccentGreen, fontSize = 10.sp)
                        }
                        IconButton(onClick = { editing = ep }) { Icon(Icons.Outlined.Edit, "Düzenle", tint = DipzonPurpleLight) }
                        IconButton(onClick = { viewModel.deleteEpisode(ep) }) { Icon(Icons.Outlined.Delete, "Sil", tint = DipzonAccentRed) }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat", color = DipzonPurpleLight) } }
    )
    if (adding) EpisodeEditorDialog(series, null, viewModel, { adding = false }) { viewModel.saveEpisode(it); adding = false }
    editing?.let { ep -> EpisodeEditorDialog(series, ep, viewModel, { editing = null }) { viewModel.saveEpisode(it); editing = null } }
}

@Composable
private fun EpisodeEditorDialog(series: SeriesEntity, initial: EpisodeEntity?, viewModel: DipzonViewModel, onDismiss: () -> Unit, onSave: (EpisodeEntity) -> Unit) {
    var season by remember(initial) { mutableStateOf((initial?.seasonNumber ?: 1).toString()) }
    var number by remember(initial) { mutableStateOf((initial?.episodeNumber ?: (series.totalEpisodes + 1)).toString()) }
    var title by remember(initial) { mutableStateOf(initial?.title ?: "Yeni Bölüm") }
    var synopsis by remember(initial) { mutableStateOf(initial?.synopsis.orEmpty()) }
    var duration by remember(initial) { mutableStateOf((initial?.durationSeconds ?: 180).toString()) }
    var videoUrl by remember(initial) { mutableStateOf(initial?.videoUrl.orEmpty()) }
    var thumbUrl by remember(initial) { mutableStateOf(initial?.thumbnailUrl.orEmpty()) }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }
    var selectedThumb by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val upload by viewModel.uploadState.collectAsState()

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> selectedVideo = uri }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> selectedThumb = uri }

    fun finishSave(finalVideo: String, finalThumb: String) {
        val s = season.toIntOrNull() ?: 1
        val n = number.toIntOrNull() ?: 1
        val id = initial?.id ?: "${series.id}_s${s}_e${n}_${System.currentTimeMillis()}"
        onSave(
            EpisodeEntity(
                id = id, seriesId = series.id, seasonNumber = s, episodeNumber = n, title = title.trim(),
                durationSeconds = duration.toIntOrNull() ?: 180, videoUrl = finalVideo, thumbnailUrl = finalThumb,
                synopsis = synopsis.trim(), isFree = initial?.isFree ?: true, publishedAt = initial?.publishedAt ?: System.currentTimeMillis()
            )
        )
        viewModel.resetUploadState()
    }

    fun uploadAndSave() {
        localError = null
        val video = selectedVideo
        val thumb = selectedThumb
        if (video == null && videoUrl.isBlank()) { localError = "Bir video seç veya video URL'si gir."; return }
        val safeBase = "${series.id}/${System.currentTimeMillis()}"
        if (video != null) {
            viewModel.uploadMedia(video, "videos/$safeBase.mp4") { videoResult ->
                videoResult.onSuccess { uploadedVideo ->
                    if (thumb != null) {
                        viewModel.uploadMedia(thumb, "thumbnails/$safeBase.jpg") { thumbResult ->
                            thumbResult.onSuccess { finishSave(uploadedVideo, it) }.onFailure { localError = it.localizedMessage }
                        }
                    } else finishSave(uploadedVideo, thumbUrl)
                }.onFailure { localError = it.localizedMessage }
            }
        } else if (thumb != null) {
            viewModel.uploadMedia(thumb, "thumbnails/$safeBase.jpg") { result ->
                result.onSuccess { finishSave(videoUrl, it) }.onFailure { localError = it.localizedMessage }
            }
        } else finishSave(videoUrl, thumbUrl)
    }

    AlertDialog(
        onDismissRequest = { if (!upload.isUploading) onDismiss() }, containerColor = DipzonSurface,
        title = { Text(if (initial == null) "Yeni Bölüm" else "Bölümü Düzenle", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.weight(1f)) { AdminField(season, { season = it }, "Sezon") }; Box(Modifier.weight(1f)) { AdminField(number, { number = it }, "Bölüm") } } }
                item { AdminField(title, { title = it }, "Başlık") }
                item { AdminField(synopsis, { synopsis = it }, "Özet", false) }
                item { AdminField(duration, { duration = it }, "Süre (saniye)") }
                item { OutlinedButton(onClick = { videoPicker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.VideoFile, null); Text(if (selectedVideo == null) " Telefondan Video Seç" else " Video Seçildi ✓") } }
                item { AdminField(videoUrl, { videoUrl = it }, "Video URL (isteğe bağlı)") }
                item { OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Image, null); Text(if (selectedThumb == null) " Kapak Görseli Seç" else " Görsel Seçildi ✓") } }
                item { AdminField(thumbUrl, { thumbUrl = it }, "Kapak URL (isteğe bağlı)") }
                if (upload.isUploading) item { Column { LinearProgressIndicator(progress = upload.progress / 100f, modifier = Modifier.fillMaxWidth()); Text("Yükleniyor: %${upload.progress}", color = DipzonTextSecondary, fontSize = 11.sp) } }
                localError?.let { err -> item { Text(err, color = DipzonAccentRed, fontSize = 11.sp) } }
            }
        },
        confirmButton = { Button(onClick = ::uploadAndSave, enabled = !upload.isUploading && title.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)) { Text(if (upload.isUploading) "Yükleniyor" else "Kaydet ve Yükle") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !upload.isUploading) { Text("İptal", color = Color.White) } }
    )
}

@Composable
private fun Moderation(comments: List<CommentEntity>, onDelete: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
        if (comments.isEmpty()) item { Text("Henüz yorum yok.", color = DipzonTextMuted) }
        items(comments, key = { it.id }) { comment ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DipzonCard).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(comment.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(comment.text, color = DipzonTextSecondary, fontSize = 11.sp)
                    Text("${comment.likesCount} beğeni${if (comment.isSpoiler) " • SPOILER" else ""}", color = DipzonTextMuted, fontSize = 9.sp)
                }
                IconButton(onClick = { onDelete(comment.id) }) { Icon(Icons.Outlined.Delete, "Yorumu sil", tint = DipzonAccentRed) }
            }
        }
    }
}

@Composable
private fun AdminField(value: String, onValueChange: (String) -> Unit, label: String, singleLine: Boolean = true) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, singleLine = singleLine, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = DipzonPurplePrimary, unfocusedBorderColor = DipzonBorder,
    focusedLabelColor = DipzonPurpleLight, unfocusedLabelColor = DipzonTextMuted
)
