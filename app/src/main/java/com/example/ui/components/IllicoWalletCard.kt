package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.WalletAccount
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaPrimary
import java.text.DecimalFormat

@Composable
fun IllicoWalletCard(
    account: WalletAccount,
    activeCurrency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onToggleVisibility: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenExchange: () -> Unit,
    onSendClick: () -> Unit,
    onTopupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mockRate = account.exchangeRateUsdToCdf // Mock exchange rate: 2,850 CDF per 1 USD
    val dfCdf = DecimalFormat("#,###")
    val dfUsd = DecimalFormat("#,##0.00")

    // Mock exchange rate conversion equivalent
    val equivalentText = if (activeCurrency == Currency.CDF) {
        val eqUsd = account.cdfBalance / mockRate
        "≈ $${dfUsd.format(eqUsd)} USD (Taux mock : 1 USD = ${dfCdf.format(mockRate)} FC)"
    } else {
        val eqCdf = account.usdBalance * mockRate
        "≈ ${dfCdf.format(eqCdf)} FC (Taux mock : 1 USD = ${dfCdf.format(mockRate)} FC)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("illico_wallet_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0049B7),
                            Color(0xFF003082),
                            Color(0xFF001F5C)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header of card: Account label & Currency Toggle Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Compte Principal Futa",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = account.futaId,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Currency Pill Switcher (CDF / USD)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IllicoCurrencyTab(
                            label = "CDF",
                            isSelected = activeCurrency == Currency.CDF,
                            onClick = { onCurrencyChange(Currency.CDF) },
                            testTag = "illico_tab_cdf"
                        )
                        IllicoCurrencyTab(
                            label = "USD",
                            isSelected = activeCurrency == Currency.USD,
                            onClick = { onCurrencyChange(Currency.USD) },
                            testTag = "illico_tab_usd"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Solde label with Eye icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Solde disponible",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("illico_toggle_visibility")
                    ) {
                        Icon(
                            imageVector = if (account.isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Afficher/Masquer solde",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main Balance Display with prominent Currency Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Balance Numeric Value (Animated)
                    Column(modifier = Modifier.weight(1f)) {
                        if (account.isBalanceVisible) {
                            AnimatedContent(
                                targetState = activeCurrency,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "balance_currency_anim"
                            ) { targetCurr ->
                                val text = if (targetCurr == Currency.CDF) {
                                    account.formattedCdf()
                                } else {
                                    account.formattedUsd()
                                }
                                Text(
                                    text = text,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp,
                                    modifier = Modifier.testTag("illico_balance_text")
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = equivalentText,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("balance_mock_equivalent_text")
                            )
                        } else {
                            Text(
                                text = "••••••••",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Solde masqué • Appuyez sur l'œil pour afficher",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dedicated Toggle Button on Main Balance Display (Switches CDF ⇄ USD view with mock rate)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val nextCurrency = if (activeCurrency == Currency.CDF) Currency.USD else Currency.CDF
                                onCurrencyChange(nextCurrency)
                            }
                            .testTag("balance_currency_toggle"),
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Basculer devise",
                                tint = FutaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (activeCurrency == Currency.CDF) "Vers USD" else "Vers CDF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "1$ = 2 850 FC",
                                    fontSize = 9.sp,
                                    color = FutaCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4 Clean Quick Action Buttons inside Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardActionButton(
                        icon = Icons.Default.Add,
                        label = "Recharger",
                        onClick = onTopupClick
                    )
                    CardActionButton(
                        icon = Icons.Default.Send,
                        label = "Transférer",
                        onClick = onSendClick
                    )
                    CardActionButton(
                        icon = Icons.Default.QrCode2,
                        label = "Mon QR",
                        onClick = onOpenQr
                    )
                    CardActionButton(
                        icon = Icons.Default.CurrencyExchange,
                        label = "Convertir",
                        onClick = onOpenExchange
                    )
                }
            }
        }
    }
}

@Composable
private fun IllicoCurrencyTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) Color.White else Color.Transparent,
        contentColor = if (isSelected) FutaPrimary else Color.White.copy(alpha = 0.85f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CardActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}
