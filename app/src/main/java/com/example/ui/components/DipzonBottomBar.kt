package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val testTag: String
)

@Composable
fun DipzonBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            screen = Screen.HOME,
            label = "Ana Sayfa",
            activeIcon = Icons.Filled.Home,
            inactiveIcon = Icons.Outlined.Home,
            testTag = "nav_home"
        ),
        NavItem(
            screen = Screen.DISCOVER,
            label = "Keşfet",
            activeIcon = Icons.Filled.VideoLibrary,
            inactiveIcon = Icons.Outlined.VideoLibrary,
            testTag = "nav_discover"
        ),
        NavItem(
            screen = Screen.SEARCH,
            label = "Ara",
            activeIcon = Icons.Filled.Search,
            inactiveIcon = Icons.Outlined.Search,
            testTag = "nav_search"
        ),
        NavItem(
            screen = Screen.WATCHLIST,
            label = "Listem",
            activeIcon = Icons.Filled.Bookmark,
            inactiveIcon = Icons.Outlined.BookmarkBorder,
            testTag = "nav_watchlist"
        ),
        NavItem(
            screen = Screen.PROFILE,
            label = "Profil",
            activeIcon = Icons.Filled.Person,
            inactiveIcon = Icons.Outlined.Person,
            testTag = "nav_profile"
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = DipzonBlack.copy(alpha = 0.96f),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) DipzonPurpleLight else DipzonTextMuted,
                    label = "nav_icon_color"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else DipzonTextMuted,
                    label = "nav_text_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.screen) }
                        )
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Active Pill or Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 28.dp)
                                    .clip(CircleShape)
                                    .background(DipzonPurpleSubtle)
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}
