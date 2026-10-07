package com.example.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.components.CommentBottomSheet
import com.example.ui.theme.*
import com.example.viewmodel.DipzonViewModel

@Composable
fun DiscoverScreen(
    viewModel: DipzonViewModel,
    onWatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()

    var showCommentsForSeriesId by remember { mutableStateOf<String?>(null) }
    val comments by viewModel.currentSeriesComments.collectAsState()

    if (allSeries.isEmpty()) {
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

    val pagerState = rememberPagerState(pageCount = { allSeries.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val series = allSeries[page]
            val isSaved = savedItems.any { it.seriesId == series.id }

            DiscoverItem(
                series = series,
                isSaved = isSaved,
                onWatchClick = { onWatchClick(series.id) },
                onToggleSave = { viewModel.toggleSeriesSave(series.id, isSaved) },
                onCommentClick = {
                    showCommentsForSeriesId = series.id
                }
            )
        }

        // Top Discover Header Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(0.8.dp, DipzonPurpleSubtle, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DipzonPurpleLight)
                )
                Text(
                    text = "DIPZON KEŞFET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Comment Sheet if triggered from Discover
        if (showCommentsForSeriesId != null) {
            CommentBottomSheet(
                comments = comments,
                onDismiss = { showCommentsForSeriesId = null },
                onSendComment = { text, isSpoiler ->
                    viewModel.postComment(text, isSpoiler)
                },
                onUpvoteComment = { commentId ->
                    viewModel.upvoteComment(commentId)
                }
            )
        }
    }
}

@Composable
private fun DiscoverItem(
    series: SeriesEntity,
    isSaved: Boolean,
    onWatchClick: () -> Unit,
    onToggleSave: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableLongStateOf(series.likesCount) }

    Box(modifier = modifier.fillMaxSize()) {
        // 9:16 Full Screen Vertical Poster / Backdrop
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
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Right Action Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) DipzonAccentRed else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${likeCount / 1000}B",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Comments
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Yorumlar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Yorum",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Save / Watchlist
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Listeme Ekle",
                        tint = if (isSaved) DipzonPurpleLight else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isSaved) "Eklendi" else "Listem",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Left Bottom Information & CTA
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 100.dp)
        ) {
            // Category & Match Pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DipzonPurplePrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = series.category,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "%${series.matchRate} Eşleşme",
                        color = DipzonAccentGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${series.totalEpisodes} Bölüm",
                    color = DipzonTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = series.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Synopsis
            Text(
                text = series.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DipzonTextPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Big CTA: "İzlemeye Başla"
            Button(
                onClick = onWatchClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DipzonPurplePrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("discover_watch_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "İzlemeye Başla (1. Bölüm)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
