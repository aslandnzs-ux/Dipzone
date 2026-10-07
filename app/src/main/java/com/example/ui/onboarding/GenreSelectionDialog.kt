package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GenreSelectionDialog(
    initialSelected: List<String>,
    onComplete: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableGenres = listOf(
        "Gerilim",
        "Bilim Kurgu",
        "Aksiyon",
        "Romantik",
        "Fantastik",
        "Dram",
        "Korku",
        "Gizem"
    )

    val selectedGenres = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }

    AlertDialog(
        onDismissRequest = { onComplete(selectedGenres) },
        containerColor = DipzonSurface,
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dipzon stylized emblem
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(DipzonPurplePrimary, DipzonPurpleDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Dipzon'a Hoş Geldiniz",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "Dikey sinema deneyimini kişiselleştirmek için en sevdiğin 2 veya daha fazla türü seç:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DipzonTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chips Grid
                val chunked = availableGenres.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { genre ->
                            val isSelected = selectedGenres.contains(genre)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DipzonPurplePrimary else DipzonCard)
                                    .border(
                                        width = 0.8.dp,
                                        color = if (isSelected) DipzonPurpleLight else DipzonBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isSelected) selectedGenres.remove(genre)
                                        else selectedGenres.add(genre)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = genre,
                                        color = if (isSelected) Color.White else DipzonTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onComplete(selectedGenres) },
                colors = ButtonDefaults.buttonColors(containerColor = DipzonPurplePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Dikey Sinemayı Başlat",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}
