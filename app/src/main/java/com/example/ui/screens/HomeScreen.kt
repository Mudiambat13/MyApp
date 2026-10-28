package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.compose.material.icons.filled.Close
import com.example.data.model.LinkedAccount
import com.example.data.model.SpendingCategory
import com.example.data.model.Transaction
import com.example.data.model.getSpendingCategory
import com.example.ui.components.FeeSavingsBanner
import com.example.ui.components.IllicoWalletCard
import com.example.ui.components.MonthlySpendingPieChartCard
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.TransactionItem
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel
import com.example.util.BiometricAuthManager

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
    val spendingBreakdown by viewModel.spendingBreakdown.collectAsState()
    val selectedSpendingMonth by viewModel.selectedSpendingMonth.collectAsState()
    val selectedSpendingCategory by viewModel.selectedSpendingCategory.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("Toutes") }

    val filteredTransactions = remember(transactions, selectedFilter, selectedSpendingCategory) {
        if (selectedSpendingCategory != null) {
            transactions.filter { it.getSpendingCategory() == selectedSpendingCategory }
        } else {
            when (selectedFilter) {
                "Airtime" -> transactions.filter { it.getSpendingCategory() == SpendingCategory.AIRTIME }
                "Transferts" -> transactions.filter {
                    it.title.startsWith("Envoi", true) || it.title.startsWith("Reçu", true) || it.type.name.contains("TRANSFER") || it.type.name.contains("TONTINE")
                }
                "Factures" -> transactions.filter {
                    it.title.contains("Facture", true) || it.title.contains("CashPower", true) || it.title.contains("SNEL", true) || it.title.contains("REGIDESO", true) || it.title.contains("CANAL", true)
                }
                "Banques" -> transactions.filter {
                    it.title.contains("Rawbank", true) || it.title.contains("Recharge", true) || it.title.contains("Virement", true) || it.title.contains("Retrait", true)
                }
                else -> transactions
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Illicocash style Header: Avatar + User Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(FutaScreen.PROFILE) }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                            .testTag("home_header_profile_click")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FutaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Bonjour, Jonathan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "FutaPay Illico",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Theme Toggle Button (Mode Clair / Sombre)
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("toggle_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Changer thème",
                            tint = if (uiState.isDarkMode) Color(0xFFFDB913) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Security & Biometrics
                    IconButton(
                        onClick = { viewModel.openSecuritySettings() },
                        modifier = Modifier.testTag("security_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Sécurité & Biométrie",
                            tint = FutaPrimary
                        )
                    }

                    // Notifications
                    IconButton(
                        onClick = { viewModel.setNotification("Toutes vos notifications sont à jour.") },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_scroll"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Illicocash Wallet Card Presentation
            item {
                IllicoWalletCard(
                    account = account,
                    activeCurrency = uiState.activeCurrency,
                    onCurrencyChange = { viewModel.setActiveCurrency(it) },
                    onToggleVisibility = {
                        val activity = context as? FragmentActivity
                        if (!account.isBalanceVisible && account.isBiometricEnabled && activity != null) {
                            BiometricAuthManager.authenticate(
                                activity = activity,
                                title = "Déverrouiller les soldes",
                                subtitle = "Vérification biométrique requise pour afficher vos soldes",
                                negativeButtonText = "Afficher sans biométrie",
                                onSuccess = { viewModel.toggleBalanceVisibility() },
                                onNegativeButtonOrFallback = { viewModel.toggleBalanceVisibility() }
                            )
                        } else {
                            viewModel.toggleBalanceVisibility()
                        }
                    },
                    onOpenQr = { viewModel.openQrCardModal() },
                    onOpenExchange = { viewModel.openExchangeModal() },
                    onSendClick = { viewModel.navigateTo(FutaScreen.SEND) },
                    onTopupClick = { viewModel.navigateTo(FutaScreen.BANKS) }
                )
            }

            // Clean Quick Actions Grid (Icons are distinct, spacious, not overcrowded)
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

            // Fee Savings Banner (1% transparent model)
            item {
                FeeSavingsBanner(
                    onCalculateClick = { viewModel.openSavingsCalculator() }
                )
            }

            // Pie Chart visualizing monthly spending categories (Airtime, Utility Bills, Transfers, etc.)
            item {
                MonthlySpendingPieChartCard(
                    breakdown = spendingBreakdown,
                    activeCurrency = uiState.activeCurrency,
                    selectedCategory = selectedSpendingCategory,
                    selectedMonthIndex = selectedSpendingMonth,
                    onSelectMonth = { viewModel.setSpendingMonth(it) },
                    onSelectCategory = { viewModel.setSelectedSpendingCategory(it) },
                    onToggleCurrency = { viewModel.setActiveCurrency(it) }
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
                            text = if (selectedSpendingCategory != null) {
                                "Opérations : ${selectedSpendingCategory?.shortName}"
                            } else {
                                "Dernières Opérations"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredTransactions.size} opération(s)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Active Category Filter Notification Pill
                    if (selectedSpendingCategory != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(selectedSpendingCategory!!.colorHex).copy(alpha = 0.15f),
                            modifier = Modifier.testTag("active_category_filter_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Filtre graphique actif : ${selectedSpendingCategory!!.shortName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(selectedSpendingCategory!!.colorHex)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Effacer filtre catégorie",
                                    tint = Color(selectedSpendingCategory!!.colorHex),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.clearSelectedSpendingCategory() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("Toutes", "Airtime", "Factures", "Transferts", "Banques")
                        items(filters) { f ->
                            val isSelected = selectedFilter == f && selectedSpendingCategory == null
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.clearSelectedSpendingCategory()
                                    selectedFilter = f
                                },
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aucune opération dans cette catégorie",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        text = "Passerelle Bancaire & Mobile",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "0 FC dépôt",
                                    fontSize = 9.sp,
                                    color = FutaGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
