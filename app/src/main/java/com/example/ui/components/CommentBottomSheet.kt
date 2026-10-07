package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CommentEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onSendComment: (text: String, isSpoiler: Boolean) -> Unit,
    onUpvoteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var isSpoilerChecked by remember { mutableStateOf(false) }

    // Map to track revealed spoilers
    val revealedSpoilers = remember { mutableStateMapOf<String, Boolean>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DipzonSurface,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DipzonBorder)
            )
        },
        modifier = modifier.fillMaxHeight(0.70f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yorumlar (${comments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = DipzonTextSecondary
                    )
                }
            }

            HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz yorum yok. İlk yorumu sen yaz!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DipzonTextMuted
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        val isRevealed = revealedSpoilers[comment.id] == true

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // User Avatar
                            AsyncImage(
                                model = comment.userAvatar.ifEmpty {
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&q=80"
                                },
                                contentDescription = comment.userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, DipzonPurpleSubtle, CircleShape)
                            )

                            // Comment Content
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = comment.userName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = comment.timestampFormatted,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DipzonTextMuted,
                                            fontSize = 11.sp
                                        )
                                    )

                                    if (comment.isSpoiler) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DipzonAccentRed.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "SPOILER",
                                                color = DipzonAccentRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Spoiler or Regular text
                                if (comment.isSpoiler && !isRevealed) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DipzonCard)
                                            .border(1.dp, DipzonAccentRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .clickable { revealedSpoilers[comment.id] = true }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = null,
                                                tint = DipzonAccentRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Spoiler içeriyor. Görmek için dokun.",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = DipzonTextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DipzonTextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Comment Actions (Like & Reply)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.clickable { onUpvoteComment(comment.id) }
                                    ) {
                                        Icon(
                                            imageVector = if (comment.likesCount > 0) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Beğen",
                                            tint = if (comment.likesCount > 0) DipzonAccentRed else DipzonTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (comment.likesCount > 0) "${comment.likesCount}" else "Beğen",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = DipzonTextMuted,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    Text(
                                        text = "Yanıtla",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DipzonTextMuted,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.clickable { /* Reply feature */ }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = DipzonBorder.copy(alpha = 0.5f))

            // Spoiler Toggle Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = isSpoilerChecked,
                        onCheckedChange = { isSpoilerChecked = it },
                        modifier = Modifier.size(36.dp, 24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DipzonAccentRed,
                            uncheckedThumbColor = DipzonTextMuted,
                            uncheckedTrackColor = DipzonCard
                        )
                    )
                    Text(
                        text = "Spoiler içeriyor",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSpoilerChecked) DipzonAccentRed else DipzonTextSecondary,
                            fontWeight = if (isSpoilerChecked) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }

                Text(
                    text = "Topluluk Kuralları",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DipzonTextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            text = if (isSpoilerChecked) "Spoiler içeren yorumunu yaz..." else "Dizi hakkında yorum yap...",
                            color = DipzonTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSpoilerChecked) DipzonAccentRed else DipzonPurplePrimary,
                        unfocusedBorderColor = DipzonBorder,
                        focusedContainerColor = DipzonCard,
                        unfocusedContainerColor = DipzonCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onSendComment(commentText.trim(), isSpoilerChecked)
                            commentText = ""
                            isSpoilerChecked = false
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (commentText.isNotBlank()) DipzonPurplePrimary else DipzonSurfaceVariant)
                        .testTag("send_comment_btn"),
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gönder",
                        tint = if (commentText.isNotBlank()) Color.White else DipzonTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
