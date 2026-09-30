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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel
import com.example.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: DipzonViewModel,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val watchProgressList by viewModel.watchProgressList.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

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
            Text(
                text = "Profilim",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 22.sp
                )
            )

            // VIP Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(DipzonAccentGold, DipzonPurplePrimary))
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "VIP ULTRA",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // USER CARD
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DipzonCard)
                        .border(1.dp, DipzonBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = profile?.avatarUrl?.ifEmpty {
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&q=80"
                        },
                        contentDescription = "Profil Resmi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, DipzonPurplePrimary, CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.username ?: "Deniz Sinemasever",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = profile?.email ?: "deniz@dipzon.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DipzonTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = DipzonAccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${profile?.coins ?: 150} Jeton Bakiyesi",
                                color = DipzonAccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // STATS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "İzlenen",
                        value = "${watchProgressList.size * 3 + 4} Bölüm",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Listemde",
                        value = "${savedItems.size} Dizi",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Toplam Süre",
                        value = "52 Dk",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ADMIN & ANALYTICS CONSOLE (HIGHLIGHT FEATURE)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DipzonPurpleDark, DipzonSurfaceVariant)
                            )
                        )
                        .border(1.dp, DipzonPurplePrimary.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .clickable { onAdminClick() }
                        .padding(14.dp)
                        .testTag("admin_panel_entry_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DipzonPurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "İçerik Yönetim & Analitik Paneli",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Dizi ekle, bölümleri düzenle ve canlı metrikleri gör",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DipzonPurpleLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // SUBSCRIPTION / JETON BANNER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DipzonCard)
                        .border(0.8.dp, DipzonBorder, RoundedCornerShape(12.dp))
                        .clickable { showSubscriptionDialog = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dipzon VIP Ultra & Jetonlar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sınırsız 1080p dikey izleme, reklamsız ve erken erişim",
                            color = DipzonTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showSubscriptionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Yönet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SETTINGS SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DipzonCard)
                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                ) {
                    SectionRowTitle(title = "İzleme ve Oynatma Ayarları")

                    SettingsToggleRow(
                        title = "Veri Tasarrufu",
                        subtitle = "Mobil ağda kaliteyi optimize eder",
                        icon = Icons.Outlined.DataSaverOn,
                        checked = profile?.dataSaver == true,
                        onCheckedChange = { viewModel.updateDataSaver(it) }
                    )

                    HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

                    SettingsToggleRow(
                        title = "Bildirimler",
                        subtitle = "Yeni bölümler ve sezonlar için uyarılar",
                        icon = Icons.Outlined.Notifications,
                        checked = profile?.notificationsEnabled == true,
                        onCheckedChange = { viewModel.updateNotifications(it) }
                    )

                    HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

                    SettingsValueRow(
                        title = "Varsayılan Video Kalitesi",
                        value = profile?.videoQuality ?: "1080p FHD",
                        icon = Icons.Outlined.HighQuality
                    )

                    HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

                    SettingsValueRow(
                        title = "Altyazı Dili",
                        value = profile?.subtitleLanguage ?: "Türkçe",
                        icon = Icons.Outlined.ClosedCaption
                    )
                }
            }

            // HISTORY & CACHE SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DipzonCard)
                        .border(0.5.dp, DipzonBorder, RoundedCornerShape(14.dp))
                ) {
                    SectionRowTitle(title = "Geçmiş ve Depolama")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearHistoryDialog = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = null,
                                tint = DipzonAccentRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "İzleme Geçmişini Temizle",
                                color = DipzonAccentRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = DipzonTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

                    SettingsValueRow(
                        title = "İndirilenler (Çevrimdışı)",
                        value = "2 Bölüm (140 MB)",
                        icon = Icons.Outlined.Download
                    )
                }
            }

            // APP INFO & LOGOUT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Dipzon v1.0.0 (Dikey Sinema Motoru)",
                        color = DipzonTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Gizlilik Politikası • Kullanım Şartları",
                        color = DipzonPurpleLight,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = DipzonSurface,
            title = {
                Text(text = "İzleme Geçmişi Temizlensin mi?", color = Color.White)
            },
            text = {
                Text(
                    text = "'İzlemeye Devam Et' listesi ve kaydedilen izleme konumlarınız sıfırlanacak.",
                    color = DipzonTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(text = "Evet, Temizle", color = DipzonAccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(text = "Vazgeç", color = Color.White)
                }
            }
        )
    }

    // Subscription & Coins Sheet
    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            containerColor = DipzonSurface,
            title = {
                Text(text = "Dipzon Üyelik & Jetonlar", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Mevcut Planınız: Dipzon VIP Ultra (Aktif)",
                        color = DipzonAccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Tüm 9:16 dizilere sınırsız erişim\n• Reklamsız akıcı dikey izleme\n• Erken final bölüm kilitlerini açma hakkı\n• 150 VIP Jeton yüklendi",
                        color = DipzonTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSubscriptionDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary)
                ) {
                    Text(text = "Tamam", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DipzonCard)
            .border(0.5.dp, DipzonBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = DipzonTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SectionRowTitle(title: String) {
    Text(
        text = title,
        color = DipzonPurpleLight,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DipzonPurpleLight,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = DipzonTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DipzonPurplePrimary,
                uncheckedThumbColor = DipzonTextMuted,
                uncheckedTrackColor = DipzonBorder
            )
        )
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DipzonPurpleLight,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp
            )
        }

        Text(
            text = value,
            color = DipzonTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
