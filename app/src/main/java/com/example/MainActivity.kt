package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.admin.AdminScreen
import com.example.ui.components.DipzonBottomBar
import com.example.ui.detail.SeriesDetailScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.GenreSelectionDialog
import com.example.ui.player.VerticalPlayerScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.search.SearchScreen
import com.example.ui.theme.*
import com.example.ui.watchlist.WatchlistScreen
import com.example.viewmodel.DipzonViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DipzonApp()
            }
        }
    }
}

@Composable
fun DipzonApp(viewModel: DipzonViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeSeriesId by viewModel.activeSeriesId.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showGenreDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Check if first-time genre selection is needed
    LaunchedEffect(userProfile) {
        if (userProfile != null && userProfile!!.preferredGenres.isEmpty()) {
            showGenreDialog = true
        }
    }

    // Custom back handler for root
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.navigateBack()
    }

    // Full screen Scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DipzonBlack,
        bottomBar = {
            // Show bottom navigation only on primary root tabs
            val isRootTab = currentScreen in listOf(
                Screen.HOME,
                Screen.DISCOVER,
                Screen.SEARCH,
                Screen.WATCHLIST,
                Screen.PROFILE
            )

            AnimatedVisibility(
                visible = isRootTab,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                DipzonBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (currentScreen in listOf(Screen.HOME, Screen.DISCOVER, Screen.SEARCH, Screen.WATCHLIST, Screen.PROFILE)) {
                        innerPadding.calculateBottomPadding()
                    } else 0.dp
                )
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onSeriesClick = { seriesId ->
                        viewModel.openSeriesDetail(seriesId)
                    },
                    onSearchClick = {
                        viewModel.navigateTo(Screen.SEARCH)
                    },
                    onNotificationClick = {
                        showNotificationDialog = true
                    }
                )

                Screen.DISCOVER -> DiscoverScreen(
                    viewModel = viewModel,
                    onWatchClick = { seriesId ->
                        viewModel.startPlaying(seriesId)
                    }
                )

                Screen.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onSeriesClick = { seriesId ->
                        viewModel.openSeriesDetail(seriesId)
                    }
                )

                Screen.WATCHLIST -> WatchlistScreen(
                    viewModel = viewModel,
                    onSeriesClick = { seriesId ->
                        viewModel.openSeriesDetail(seriesId)
                    }
                )

                Screen.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onAdminClick = {
                        viewModel.navigateTo(Screen.ADMIN)
                    }
                )

                Screen.DETAIL -> {
                    val id = activeSeriesId ?: "ser_karanlik_safak"
                    SeriesDetailScreen(
                        seriesId = id,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() },
                        onPlayClick = { sId, epId ->
                            viewModel.startPlaying(sId, epId)
                        },
                        onSimilarSeriesClick = { sId ->
                            viewModel.openSeriesDetail(sId)
                        }
                    )
                }

                Screen.PLAYER -> VerticalPlayerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.ADMIN -> AdminScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }

        // Onboarding Genre Selection Dialog
        if (showGenreDialog) {
            GenreSelectionDialog(
                initialSelected = listOf("Gerilim", "Bilim Kurgu"),
                onComplete = { selected ->
                    viewModel.updatePreferredGenres(selected.joinToString(","))
                    showGenreDialog = false
                }
            )
        }

        // Notification Modal
        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                containerColor = DipzonSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = DipzonPurpleLight
                        )
                        Text(text = "Dipzon Bildirimleri", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NotificationItem(
                            title = "Yeni Bölüm Yayında! 🎬",
                            desc = "Takip ettiğin 'Karanlık Şafak' dizisinin 2. bölümü yayınlandı.",
                            time = "10 dk önce"
                        )
                        HorizontalDivider(color = DipzonBorder)
                        NotificationItem(
                            title = "Senin İçin Yeni Öneri",
                            desc = "'Paralel Bağlantı' dizisi %96 eşleşme oranıyla kütüphanene eklendi.",
                            time = "2 saat önce"
                        )
                        HorizontalDivider(color = DipzonBorder)
                        NotificationItem(
                            title = "Yorumuna Beğeni Geldi",
                            desc = "Burak Yıldız ve 4 kişi yorumunu beğendi.",
                            time = "Dün"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showNotificationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Kapat", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
private fun NotificationItem(
    title: String,
    desc: String,
    time: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = time, color = DipzonTextMuted, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, color = DipzonTextSecondary, fontSize = 11.sp)
    }
}
