package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CurrencyExchangeDialog
import com.example.ui.components.QrCardModal
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SavingsCalculatorDialog
import com.example.ui.screens.AgentsMapScreen
import com.example.ui.screens.BankIntegrationScreen
import com.example.ui.screens.BillsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MerchantQrScreen
import com.example.ui.screens.SendMoneyScreen
import com.example.ui.screens.TontineScreen
import com.example.ui.theme.FutaBackground
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FutaApp()
            }
        }
    }
}

@Composable
fun FutaApp(
    viewModel: FutaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val account by viewModel.account.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = FutaTextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                val currentScreen = uiState.currentScreen

                NavigationBarItem(
                    selected = currentScreen == FutaScreen.HOME,
                    onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == FutaScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Accueil",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Accueil", fontSize = 11.sp, fontWeight = if (currentScreen == FutaScreen.HOME) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FutaPrimary,
                        selectedTextColor = FutaPrimary,
                        indicatorColor = FutaPrimary.copy(alpha = 0.12f),
                        unselectedIconColor = FutaTextSecondary,
                        unselectedTextColor = FutaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentScreen == FutaScreen.SEND,
                    onClick = { viewModel.navigateTo(FutaScreen.SEND) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == FutaScreen.SEND) Icons.Filled.Send else Icons.Outlined.Send,
                            contentDescription = "Envoyer",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Envoyer (1%)", fontSize = 11.sp, fontWeight = if (currentScreen == FutaScreen.SEND) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FutaPrimary,
                        selectedTextColor = FutaPrimary,
                        indicatorColor = FutaPrimary.copy(alpha = 0.12f),
                        unselectedIconColor = FutaTextSecondary,
                        unselectedTextColor = FutaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_send")
                )

                NavigationBarItem(
                    selected = currentScreen == FutaScreen.BANKS,
                    onClick = { viewModel.navigateTo(FutaScreen.BANKS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == FutaScreen.BANKS) Icons.Filled.AccountBalance else Icons.Outlined.AccountBalance,
                            contentDescription = "Banques",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Banques RDC", fontSize = 11.sp, fontWeight = if (currentScreen == FutaScreen.BANKS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FutaPrimary,
                        selectedTextColor = FutaPrimary,
                        indicatorColor = FutaPrimary.copy(alpha = 0.12f),
                        unselectedIconColor = FutaTextSecondary,
                        unselectedTextColor = FutaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_banks")
                )

                NavigationBarItem(
                    selected = currentScreen == FutaScreen.BILLS,
                    onClick = { viewModel.navigateTo(FutaScreen.BILLS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == FutaScreen.BILLS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Factures",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Factures", fontSize = 11.sp, fontWeight = if (currentScreen == FutaScreen.BILLS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FutaPrimary,
                        selectedTextColor = FutaPrimary,
                        indicatorColor = FutaPrimary.copy(alpha = 0.12f),
                        unselectedIconColor = FutaTextSecondary,
                        unselectedTextColor = FutaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_bills")
                )

                NavigationBarItem(
                    selected = currentScreen == FutaScreen.AGENTS,
                    onClick = { viewModel.navigateTo(FutaScreen.AGENTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == FutaScreen.AGENTS) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                            contentDescription = "Agents",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Agents", fontSize = 11.sp, fontWeight = if (currentScreen == FutaScreen.AGENTS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FutaPrimary,
                        selectedTextColor = FutaPrimary,
                        indicatorColor = FutaPrimary.copy(alpha = 0.12f),
                        unselectedIconColor = FutaTextSecondary,
                        unselectedTextColor = FutaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_agents")
                )
            }
        },
        containerColor = FutaBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    FutaScreen.HOME -> HomeScreen(viewModel = viewModel)
                    FutaScreen.SEND -> SendMoneyScreen(viewModel = viewModel)
                    FutaScreen.BANKS -> BankIntegrationScreen(viewModel = viewModel)
                    FutaScreen.BILLS -> BillsScreen(viewModel = viewModel)
                    FutaScreen.AGENTS -> AgentsMapScreen(viewModel = viewModel)
                    FutaScreen.SCAN_QR -> MerchantQrScreen(viewModel = viewModel)
                    FutaScreen.TONTINE -> TontineScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal: Official Receipt Dialog
    uiState.selectedTransaction?.let { tx ->
        ReceiptDialog(
            transaction = tx,
            onDismiss = { viewModel.closeTransactionReceipt() },
            onShare = {
                val shareText = "Reçu FutaPay RDC • Réf: ${tx.reference}\nMontant: ${tx.formattedAmount()}\nFrais: ${tx.formattedFee()}\nStatut: Validé BCC"
                clipboardManager.setText(AnnotatedString(shareText))
                Toast.makeText(context, "Reçu officiel copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Savings Calculator (Futa 1% vs Telecom 3.5%)
    if (uiState.showSavingsCalculator) {
        SavingsCalculatorDialog(
            initialCurrency = uiState.activeCurrency,
            onCalculateFee = { amt, curr -> viewModel.calculateTransferFee(amt, curr) },
            onCalculateClassicFee = { amt, curr -> viewModel.calculateClassicFee(amt, curr) },
            onDismiss = { viewModel.closeSavingsCalculator() }
        )
    }

    // Modal: Currency Exchange (CDF <-> USD)
    if (uiState.showExchangeModal) {
        CurrencyExchangeDialog(
            account = account,
            onExchange = { amt, fromCurr -> viewModel.exchangeCurrency(amt, fromCurr) },
            onDismiss = { viewModel.closeExchangeModal() }
        )
    }

    // Modal: Personal QR & Wave Card
    if (uiState.showQrCardModal) {
        QrCardModal(
            account = account,
            onToggleLock = { viewModel.toggleCardLock() },
            onDismiss = { viewModel.closeQrCardModal() },
            onCopyId = {
                clipboardManager.setText(AnnotatedString(account.futaId))
                Toast.makeText(context, "ID Futa ${account.futaId} copié !", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
