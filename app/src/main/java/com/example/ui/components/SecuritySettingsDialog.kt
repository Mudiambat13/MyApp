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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAccount
import com.example.ui.theme.FutaBorder
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.util.BiometricAuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsDialog(
    account: WalletAccount,
    onToggleBiometric: () -> Unit,
    onTestBiometric: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val biometricAvailability = BiometricAuthManager.checkBiometricAvailability(context)

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
                .testTag("security_settings_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FutaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = FutaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sécurité & Biométrie",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "Protection des opérations financières",
                            fontSize = 11.sp,
                            color = FutaTextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_security_button")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Biometric Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0ECFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biométrie",
                                    tint = FutaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Empreinte digitale / Face Unlock",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FutaTextPrimary
                                )
                                Text(
                                    text = if (account.isBiometricEnabled) "Actif pour tous les paiements" else "Désactivé (Code PIN seul)",
                                    fontSize = 11.sp,
                                    color = FutaTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = account.isBiometricEnabled,
                            onCheckedChange = { onToggleBiometric() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FutaPrimary,
                                checkedTrackColor = Color(0xFFD6E4FF)
                            ),
                            modifier = Modifier.testTag("biometric_enable_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = FutaBorder)

                    // Hardware status badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = when (biometricAvailability) {
                                BiometricAuthManager.BiometricAvailability.AVAILABLE -> FutaGreenDark
                                else -> Color(0xFFD97706)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (biometricAvailability) {
                                BiometricAuthManager.BiometricAvailability.AVAILABLE -> "Capteur biométrique prêt et configuré"
                                BiometricAuthManager.BiometricAvailability.NOT_ENROLLED -> "Aucune empreinte configurée sur cet appareil Android"
                                BiometricAuthManager.BiometricAvailability.NO_HARDWARE -> "Capteur biométrique non détecté (Mode PIN actif)"
                                else -> "Biométrie temporairement indisponible"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FutaTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Protected Operations List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Opérations sensibles protégées :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Envois d'argent et transferts P2P\n• Virements sortants vers Rawbank, Equity, TMB, M-Pesa\n• Révélation des soldes cachés CDF et USD\n• Déverrouillage de la carte Futa et affichage QR code",
                        fontSize = 11.sp,
                        color = FutaTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onTestBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_biometric_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tester l'authentification biométrique")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
            ) {
                Text(text = "Enregistrer & Fermer", fontWeight = FontWeight.Bold)
            }
        }
    }
}
