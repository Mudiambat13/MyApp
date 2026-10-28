package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAccount
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaDarkCard
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCardModal(
    account: WalletAccount,
    onToggleLock: () -> Unit,
    onDismiss: () -> Unit,
    onCopyId: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("qr_card_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ma Carte & QR Futa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FutaTextPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_qr_modal_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wave-Style Badge / Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("futa_physical_card_representation"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FutaDarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F2042),
                                    Color(0xFF081326),
                                    Color(0xFF020713)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(FutaGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "F",
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FutaPay RDC",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "Sans contact",
                                    tint = FutaCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sans contact",
                                    color = FutaCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Styled QR Matrix Canvas
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QrCodePatternCanvas(
                                isFrozen = account.isCardFrozen,
                                modifier = Modifier.size(146.dp)
                            )

                            // Center Emblem
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FutaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "F",
                                    color = FutaGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = account.userName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "ID: ${account.futaId} • ${account.userPhone}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (account.isCardFrozen) {
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Verrouillée",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CARTE ET QR VERROUILLÉS",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = FutaGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "PRÉSENTER CHEZ UN MARCHAND OU UN AGENT",
                                    color = FutaGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Freeze Card Safety Control
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (account.isCardFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Verrouiller",
                            tint = if (account.isCardFrozen) Color(0xFFEF4444) else FutaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sécurité de la carte",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FutaTextPrimary
                            )
                            Text(
                                text = if (account.isCardFrozen) "Carte bloquée (aucun débit possible)" else "Carte active pour paiements",
                                fontSize = 11.sp,
                                color = FutaTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = account.isCardFrozen,
                        onCheckedChange = { onToggleLock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFEF4444),
                            checkedTrackColor = Color(0xFFFEE2E2),
                            uncheckedThumbColor = FutaPrimary,
                            uncheckedTrackColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.testTag("freeze_card_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onCopyId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_futa_id_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copier",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Copier mon ID Futa (${account.futaId})")
            }
        }
    }
}

@Composable
private fun QrCodePatternCanvas(
    isFrozen: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cellSize = size.width / 9f
        val color = if (isFrozen) Color.Gray.copy(alpha = 0.5f) else Color(0xFF0F172A)

        // Draw 3 Corner finder squares
        // Top-left
        drawRect(color, Offset(0f, 0f), Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(cellSize * 0.7f, cellSize * 0.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
        drawRect(color, Offset(cellSize * 1.1f, cellSize * 1.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

        // Top-right
        val trX = size.width - cellSize * 3
        drawRect(color, Offset(trX, 0f), Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(trX + cellSize * 0.7f, cellSize * 0.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
        drawRect(color, Offset(trX + cellSize * 1.1f, cellSize * 1.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

        // Bottom-left
        val blY = size.height - cellSize * 3
        drawRect(color, Offset(0f, blY), Size(cellSize * 3, cellSize * 3))
        drawRect(Color.White, Offset(cellSize * 0.7f, blY + cellSize * 0.7f), Size(cellSize * 1.6f, cellSize * 1.6f))
        drawRect(color, Offset(cellSize * 1.1f, blY + cellSize * 1.1f), Size(cellSize * 0.8f, cellSize * 0.8f))

        // Decorative dot matrix
        val points = listOf(
            Pair(4, 0), Pair(5, 1), Pair(3, 2), Pair(4, 3), Pair(5, 4),
            Pair(0, 4), Pair(1, 5), Pair(2, 4), Pair(6, 4), Pair(7, 5),
            Pair(8, 4), Pair(4, 5), Pair(3, 6), Pair(5, 6), Pair(4, 7),
            Pair(7, 7), Pair(8, 8), Pair(6, 8), Pair(4, 8)
        )

        for (pt in points) {
            drawCircle(
                color = color,
                radius = cellSize * 0.42f,
                center = Offset(pt.first * cellSize + cellSize / 2f, pt.second * cellSize + cellSize / 2f)
            )
        }
    }
}
