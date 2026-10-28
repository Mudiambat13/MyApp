package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary

data class ActionItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsGrid(
    onSendClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onBankClick: () -> Unit,
    onBillsClick: () -> Unit,
    onAgentsClick: () -> Unit,
    onTontineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        ActionItem(
            title = "Envoyer",
            icon = Icons.Default.Send,
            iconColor = FutaPrimary,
            bgColor = FutaPrimary.copy(alpha = 0.12f),
            testTag = "action_send",
            onClick = onSendClick
        ),
        ActionItem(
            title = "Paiement QR",
            icon = Icons.Default.QrCodeScanner,
            iconColor = Color(0xFF0284C7),
            bgColor = Color(0xFF0284C7).copy(alpha = 0.12f),
            testTag = "action_merchant",
            onClick = onScanQrClick
        ),
        ActionItem(
            title = "Banques",
            icon = Icons.Default.AccountBalance,
            iconColor = Color(0xFF8B5CF6),
            bgColor = Color(0xFF8B5CF6).copy(alpha = 0.12f),
            testTag = "action_banks",
            onClick = onBankClick
        ),
        ActionItem(
            title = "Factures",
            icon = Icons.Default.ReceiptLong,
            iconColor = Color(0xFFEA580C),
            bgColor = Color(0xFFEA580C).copy(alpha = 0.12f),
            testTag = "action_bills",
            onClick = onBillsClick
        ),
        ActionItem(
            title = "Agents",
            icon = Icons.Default.LocationOn,
            bgColor = FutaGreen.copy(alpha = 0.14f),
            iconColor = Color(0xFF059669),
            testTag = "action_agents",
            onClick = onAgentsClick
        ),
        ActionItem(
            title = "Kitemo",
            icon = Icons.Default.Diversity3,
            iconColor = Color(0xFFD97706),
            bgColor = Color(0xFFD97706).copy(alpha = 0.14f),
            testTag = "action_tontine",
            onClick = onTontineClick
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_grid"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Services Rapides",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Opérations instantanées",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean 3x2 Grid without cluttered badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..2) {
                    CleanActionTile(
                        item = actions[i],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 3..5) {
                    CleanActionTile(
                        item = actions[i],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanActionTile(
    item: ActionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { item.onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(item.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
