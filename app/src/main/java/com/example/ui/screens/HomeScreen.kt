package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.model.LinkedAccount
import com.example.data.model.Transaction
import com.example.ui.components.FeeSavingsBanner
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.TransactionItem
import com.example.ui.components.WaveCard
import com.example.ui.theme.FutaBackground
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    val account by viewModel.account.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val linkedAccounts by viewModel.linkedAccounts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedFilter by remember { mutableStateOf("Toutes") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "Transferts" -> transactions.filter {
                it.title.startsWith("Envoi", true) || it.title.startsWith("Reçu", true)
            }
            "Factures" -> transactions.filter { it.title.contains("Facture", true) || it.title.contains("CashPower", true) }
            "Banques" -> transactions.filter { it.title.contains("Rawbank", true) || it.title.contains("Recharge", true) || it.title.contains("Virement", true) }
            else -> transactions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Congolese Flag Accent Pill
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(FutaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🇨🇩",
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FutaPay",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FutaPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = FutaGold,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "100% RDC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Kinshasa • Lubumbashi • Goma",
                                fontSize = 10.sp,
                                color = FutaTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setNotification("Toutes vos notifications sont à jour.") },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = FutaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FutaBackground)
            )
        },
        containerColor = FutaBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_scroll"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Wave signature Dual Currency Card
            item {
                WaveCard(
                    account = account,
                    activeCurrency = uiState.activeCurrency,
                    onCurrencyChange = { viewModel.setActiveCurrency(it) },
                    onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                    onOpenQr = { viewModel.openQrCardModal() },
                    onOpenExchange = { viewModel.openExchangeModal() }
                )
            }

            // Quick Actions 6-Grid
            item {
                QuickActionsGrid(
                    onSendClick = { viewModel.navigateTo(FutaScreen.SEND) },
                    onScanQrClick = { viewModel.navigateTo(FutaScreen.SCAN_QR) },
                    onBankClick = { viewModel.navigateTo(FutaScreen.BANKS) },
                    onBillsClick = { viewModel.navigateTo(FutaScreen.BILLS) },
                    onAgentsClick = { viewModel.navigateTo(FutaScreen.AGENTS) },
                    onTontineClick = { viewModel.navigateTo(FutaScreen.TONTINE) }
                )
            }

            // Fee Savings Banner (1% vs 3.5%)
            item {
                FeeSavingsBanner(
                    onCalculateClick = { viewModel.openSavingsCalculator() }
                )
            }

            // Passerelle Banques Locales & Mobile Money shortcuts
            item {
                BankGatewaysRow(
                    linkedAccounts = linkedAccounts,
                    onOpenBankScreen = { viewModel.navigateTo(FutaScreen.BANKS) },
                    onAccountClick = { viewModel.navigateTo(FutaScreen.BANKS) }
                )
            }

            // Transactions Header with Filter chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historique Récent",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "${transactions.size} opérations",
                            fontSize = 12.sp,
                            color = FutaTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("Toutes", "Transferts", "Factures", "Banques")
                        items(filters) { f ->
                            FilterChip(
                                selected = selectedFilter == f,
                                onClick = { selectedFilter = f },
                                label = { Text(f, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FutaPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("filter_chip_$f")
                            )
                        }
                    }
                }
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aucune transaction dans cette catégorie",
                                fontSize = 13.sp,
                                color = FutaTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 1.dp
                    ) {
                        TransactionItem(
                            transaction = tx,
                            onClick = { viewModel.showTransactionReceipt(tx) }
                        )
                    }
                }
            }

            // Bottom space
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BankGatewaysRow(
    linkedAccounts: List<LinkedAccount>,
    onOpenBankScreen: () -> Unit,
    onAccountClick: (LinkedAccount) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bank_gateways_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Banques",
                        tint = FutaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Passerelle Bancaire & Télécom RDC",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                }

                Text(
                    text = "Gérer",
                    fontSize = 12.sp,
                    color = FutaPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onOpenBankScreen() }
                        .testTag("manage_banks_button")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(linkedAccounts) { item ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAccountClick(item) },
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(item.accentColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.logoText.take(2),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = item.providerName.split(" ").first(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FutaTextPrimary
                                )
                                Text(
                                    text = "Lié • 0 FC dépôt",
                                    fontSize = 9.sp,
                                    color = FutaGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
