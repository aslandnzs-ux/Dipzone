package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

@Composable
fun ProfileScreen(
    viewModel: DipzonViewModel,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val watchProgress by viewModel.watchProgressList.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    var showClearHistory by remember { mutableStateOf(false) }
    var showQuality by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showPurchases by remember { mutableStateOf(false) }

    val watchedSeconds = watchProgress.sumOf { it.positionSeconds }
    val watchedMinutes = watchedSeconds / 60

    Column(modifier = modifier.fillMaxSize().background(DipzonBlack).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Profilim", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            if (profile?.isPremium == true) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(DipzonAccentGold, DipzonPurplePrimary)))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text(profile?.premiumTier ?: "VIP", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp) }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DipzonCard)
                        .border(1.dp, DipzonBorder, RoundedCornerShape(16.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(DipzonSurfaceVariant)
                            .border(2.dp, DipzonPurplePrimary, CircleShape), contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Person, null, tint = DipzonPurpleLight, modifier = Modifier.size(32.dp)) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile?.username ?: "Dipzon Kullanıcısı", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(profile?.email?.takeIf { it.isNotBlank() } ?: "Yerel profil", color = DipzonTextSecondary, fontSize = 12.sp)
                        if ((profile?.coins ?: 0) > 0) Text("${profile?.coins} Jeton", color = DipzonAccentGold, fontSize = 11.sp)
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("İzlenen", "${watchProgress.size} Bölüm", Modifier.weight(1f))
                    StatCard("Listemde", "${savedItems.size} Dizi", Modifier.weight(1f))
                    StatCard("İzleme", "${watchedMinutes} dk", Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(DipzonPurpleDark, DipzonSurfaceVariant)))
                        .border(1.dp, DipzonPurplePrimary, RoundedCornerShape(14.dp)).clickable(onClick = onAdminClick)
                        .padding(14.dp).testTag("admin_panel_entry_btn"),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Column {
                            Text("Yönetici Paneli", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Giriş yap, dizi ve bölümleri yönet", color = DipzonPurpleLight, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DipzonCard)
                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Üyelik & Jetonlar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Satın alma sistemi hazır olduğunda buradan yönetilecek", color = DipzonTextSecondary, fontSize = 11.sp)
                    }
                    TextButton(onClick = { showPurchases = true }) { Text("Bilgi", color = DipzonPurpleLight) }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DipzonCard)
                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                ) {
                    SectionTitle("İzleme ve Oynatma")
                    ToggleRow("Veri Tasarrufu", "Mobil ağda daha düşük veri kullan", Icons.Outlined.DataSaverOn, profile?.dataSaver == true, viewModel::updateDataSaver)
                    HorizontalDivider(color = DipzonBorder.copy(alpha = .5f))
                    ToggleRow("Bildirimler", "Yeni içerik bildirim tercihi", Icons.Outlined.Notifications, profile?.notificationsEnabled == true, viewModel::updateNotifications)
                    HorizontalDivider(color = DipzonBorder.copy(alpha = .5f))
                    ValueRow("Varsayılan Video Kalitesi", profile?.videoQuality ?: "Otomatik", Icons.Outlined.HighQuality) { showQuality = true }
                    HorizontalDivider(color = DipzonBorder.copy(alpha = .5f))
                    ValueRow("Altyazı Dili", profile?.subtitleLanguage ?: "Türkçe", Icons.Outlined.ClosedCaption) { showSubtitle = true }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DipzonCard)
                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                ) {
                    SectionTitle("Geçmiş")
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showClearHistory = true }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.DeleteSweep, null, tint = DipzonAccentRed)
                            Text("İzleme Geçmişini Temizle", color = DipzonAccentRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = DipzonTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            item {
                Text("Dipzon v1.0 • Gerçek veriler dışında istatistik gösterilmez", color = DipzonTextMuted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            }
        }
    }

    if (showClearHistory) {
        AlertDialog(
            onDismissRequest = { showClearHistory = false }, containerColor = DipzonSurface,
            title = { Text("İzleme geçmişi temizlensin mi?", color = Color.White) },
            text = { Text("Kaydedilen izleme konumları silinecek.", color = DipzonTextSecondary) },
            confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClearHistory = false }) { Text("Temizle", color = DipzonAccentRed) } },
            dismissButton = { TextButton(onClick = { showClearHistory = false }) { Text("Vazgeç", color = Color.White) } }
        )
    }

    if (showQuality) ChoiceDialog("Video Kalitesi", listOf("Otomatik", "1080p FHD", "720p HD", "480p"), profile?.videoQuality ?: "Otomatik", { showQuality = false }) {
        viewModel.updateVideoQuality(it); showQuality = false
    }
    if (showSubtitle) ChoiceDialog("Altyazı Dili", listOf("Türkçe", "İngilizce", "Kapalı"), profile?.subtitleLanguage ?: "Türkçe", { showSubtitle = false }) {
        viewModel.updateSubtitleLanguage(it); showSubtitle = false
    }
    if (showPurchases) {
        AlertDialog(
            onDismissRequest = { showPurchases = false }, containerColor = DipzonSurface,
            title = { Text("Üyelik sistemi", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Ödeme altyapısı henüz bağlanmadığı için uygulama ücretli plan veya jeton bakiyesi uydurmuyor. Satın alma entegrasyonu geldiğinde bu bölüm aktif olacak.", color = DipzonTextSecondary) },
            confirmButton = { TextButton(onClick = { showPurchases = false }) { Text("Tamam", color = DipzonPurpleLight) } }
        )
    }
}

@Composable
private fun ChoiceDialog(title: String, options: List<String>, selected: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = DipzonSurface, title = { Text(title, color = Color.White) },
        text = { Column { options.forEach { option ->
            Row(Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = option == selected, onClick = { onSelect(option) })
                Text(option, color = Color.White)
            }
        } } }, confirmButton = {}
    )
}

@Composable private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(DipzonCard).border(.5.dp, DipzonBorder, RoundedCornerShape(12.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(title, color = DipzonTextMuted, fontSize = 11.sp) }
    }
}

@Composable private fun SectionTitle(title: String) { Text(title, color = DipzonPurpleLight, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(14.dp)) }

@Composable private fun ToggleRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = DipzonPurpleLight)
            Column { Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = DipzonTextMuted, fontSize = 11.sp) }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable private fun ValueRow(title: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(icon, null, tint = DipzonPurpleLight); Text(title, color = Color.White, fontSize = 13.sp) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(value, color = DipzonTextSecondary, fontSize = 12.sp); Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = DipzonTextMuted, modifier = Modifier.size(12.dp)) }
    }
}
