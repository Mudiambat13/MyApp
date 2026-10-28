package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.FutaBorder
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onShare: () -> Unit
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
                .testTag("receipt_bottom_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_receipt_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Success Checkmark
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1FAE5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Succès",
                    tint = FutaGreenDark,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Transaction Réussie",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FutaTextPrimary
            )

            Text(
                text = transaction.title,
                fontSize = 14.sp,
                color = FutaTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Display
            Text(
                text = transaction.formattedAmount(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FutaPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ticket Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReceiptRow(label = "Référence", value = transaction.reference)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = FutaBorder)
                    ReceiptRow(label = "Date & Heure", value = transaction.formattedDate())
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = FutaBorder)
                    ReceiptRow(label = "Frais Futa", value = transaction.formattedFee(), isHighlighted = true)

                    transaction.partnerName?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = FutaBorder)
                        ReceiptRow(label = "Passerelle / Partenaire", value = it)
                    }

                    transaction.recipientPhone?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = FutaBorder)
                        ReceiptRow(label = "Numéro destinataire", value = it)
                    }

                    transaction.notes?.let { note ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = FutaBorder)
                        ReceiptRow(label = "Détails / Code", value = note, isHighlighted = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BCC Stamp
            Text(
                text = "FutaPay RDC • Conforme aux règlements de la Banque Centrale du Congo",
                fontSize = 10.sp,
                color = FutaTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_receipt_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Partager")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dismiss_receipt_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
                ) {
                    Text(text = "Terminer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = FutaTextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) FutaPrimary else FutaTextPrimary,
            textAlign = TextAlign.End
        )
    }
}
