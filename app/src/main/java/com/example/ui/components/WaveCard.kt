package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.WalletAccount
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaPrimaryVariant

@Composable
fun WaveCard(
    account: WalletAccount,
    activeCurrency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onToggleVisibility: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenExchange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wave_main_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FutaPrimary,
                            FutaPrimaryVariant,
                            Color(0xFF002A6D)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Top row: User greeting & Currency Selector + QR Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Portefeuille RDC",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = account.userName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // QR Card Button (Wave signature feature)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenQr() }
                            .testTag("qr_card_button"),
                        color = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Afficher mon QR",
                                modifier = Modifier.size(20.dp),
                                tint = FutaGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mon QR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Currency Tabs: Franc Congolais (CDF) vs Dollar Américain (USD)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CurrencyTabButton(
                        title = "Franc Congolais (CDF)",
                        isSelected = activeCurrency == Currency.CDF,
                        onClick = { onCurrencyChange(Currency.CDF) },
                        modifier = Modifier.weight(1f),
                        testTag = "currency_tab_cdf"
                    )
                    CurrencyTabButton(
                        title = "Dollar US (USD)",
                        isSelected = activeCurrency == Currency.USD,
                        onClick = { onCurrencyChange(Currency.USD) },
                        modifier = Modifier.weight(1f),
                        testTag = "currency_tab_usd"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Balance display with Eye toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Solde disponible",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.size(28.dp).testTag("toggle_visibility_button")
                            ) {
                                Icon(
                                    imageVector = if (account.isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Masquer/Afficher solde",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (account.isBalanceVisible) {
                            Text(
                                text = if (activeCurrency == Currency.CDF) account.formattedCdf() else account.formattedUsd(),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.testTag("balance_text")
                            )
                        } else {
                            Text(
                                text = "••••••••",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // Taux du jour & Instant Exchange CTA
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenExchange() }
                            .testTag("exchange_rate_button"),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = "Changer devises",
                                    modifier = Modifier.size(14.dp),
                                    tint = FutaCyan
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "1$ = 2850 FC",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FutaCyan
                                )
                            }
                            Text(
                                text = "Convertir 0%",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) Color.White else Color.Transparent,
        contentColor = if (isSelected) FutaPrimary else Color.White.copy(alpha = 0.8f)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
