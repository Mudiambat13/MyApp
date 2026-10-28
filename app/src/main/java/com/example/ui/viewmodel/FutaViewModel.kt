package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgentPoint
import com.example.data.model.BillProvider
import com.example.data.model.CategorySpendingSummary
import com.example.data.model.Currency
import com.example.data.model.LinkedAccount
import com.example.data.model.LinkedCategory
import com.example.data.model.SpendingCategory
import com.example.data.model.TontineGroup
import com.example.data.model.Transaction
import com.example.data.model.WalletAccount
import com.example.data.repository.FutaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class FutaScreen {
    HOME,
    SEND,
    BANKS,
    BILLS,
    AGENTS,
    SCAN_QR,
    TONTINE,
    PROFILE
}

data class WithdrawalCodeData(
    val code: String,
    val amount: Double,
    val currency: Currency,
    val expiresMinutes: Int = 15
)

data class FutaUiState(
    val currentScreen: FutaScreen = FutaScreen.HOME,
    val activeCurrency: Currency = Currency.CDF,
    val isDarkMode: Boolean = false,
    val selectedTransaction: Transaction? = null,
    val activeWithdrawalCode: WithdrawalCodeData? = null,
    val showSavingsCalculator: Boolean = false,
    val showExchangeModal: Boolean = false,
    val showLinkBankModal: Boolean = false,
    val showQrCardModal: Boolean = false,
    val showSecuritySettings: Boolean = false,
    val notificationMessage: String? = null
)

class FutaViewModel(
    private val repository: FutaRepository = FutaRepository()
) : ViewModel() {

    val account: StateFlow<WalletAccount> = repository.account
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val linkedAccounts: StateFlow<List<LinkedAccount>> = repository.linkedAccounts
    val agentPoints: StateFlow<List<AgentPoint>> = repository.agentPoints
    val billProviders: StateFlow<List<BillProvider>> = repository.billProviders
    val tontineGroups: StateFlow<List<TontineGroup>> = repository.tontineGroups

    private val _selectedSpendingMonth = MutableStateFlow(0) // 0 = Ce mois, 1 = Mois dernier, 2 = Il y a 2 mois
    val selectedSpendingMonth: StateFlow<Int> = _selectedSpendingMonth.asStateFlow()

    private val _selectedSpendingCategory = MutableStateFlow<SpendingCategory?>(null)
    val selectedSpendingCategory: StateFlow<SpendingCategory?> = _selectedSpendingCategory.asStateFlow()

    val spendingBreakdown: StateFlow<List<CategorySpendingSummary>> = combine(
        transactions,
        _selectedSpendingMonth,
        account
    ) { _, monthOffset, _ ->
        repository.getMonthlySpending(monthOffset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spendingTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _selectedSpendingCategory,
        _selectedSpendingMonth
    ) { _, cat, monthOffset ->
        repository.getTransactionsForCategory(cat, monthOffset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(FutaUiState())
    val uiState: StateFlow<FutaUiState> = _uiState.asStateFlow()

    fun setSpendingMonth(offset: Int) {
        _selectedSpendingMonth.value = offset
    }

    fun setSelectedSpendingCategory(category: SpendingCategory?) {
        _selectedSpendingCategory.value = category
    }

    fun clearSelectedSpendingCategory() {
        _selectedSpendingCategory.value = null
    }

    fun navigateTo(screen: FutaScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setActiveCurrency(currency: Currency) {
        _uiState.update { it.copy(activeCurrency = currency) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleBalanceVisibility() {
        repository.toggleBalanceVisibility()
    }

    fun toggleCardLock() {
        repository.toggleCardLock()
        val isFrozen = repository.account.value.isCardFrozen
        val msg = if (isFrozen) "Carte Futa verrouillée pour sécurité." else "Carte Futa déverrouillée et active."
        setNotification(msg)
    }

    fun openSavingsCalculator() {
        _uiState.update { it.copy(showSavingsCalculator = true) }
    }

    fun closeSavingsCalculator() {
        _uiState.update { it.copy(showSavingsCalculator = false) }
    }

    fun openExchangeModal() {
        _uiState.update { it.copy(showExchangeModal = true) }
    }

    fun closeExchangeModal() {
        _uiState.update { it.copy(showExchangeModal = false) }
    }

    fun openLinkBankModal() {
        _uiState.update { it.copy(showLinkBankModal = true) }
    }

    fun closeLinkBankModal() {
        _uiState.update { it.copy(showLinkBankModal = false) }
    }

    fun openQrCardModal() {
        _uiState.update { it.copy(showQrCardModal = true) }
    }

    fun closeQrCardModal() {
        _uiState.update { it.copy(showQrCardModal = false) }
    }

    fun openSecuritySettings() {
        _uiState.update { it.copy(showSecuritySettings = true) }
    }

    fun closeSecuritySettings() {
        _uiState.update { it.copy(showSecuritySettings = false) }
    }

    fun toggleBiometric() {
        repository.toggleBiometric()
        val isEnabled = repository.account.value.isBiometricEnabled
        setNotification(
            if (isEnabled) "Authentification biométrique activée (Empreinte / Face Unlock)"
            else "Authentification biométrique désactivée (Code PIN uniquement)"
        )
    }

    fun showTransactionReceipt(transaction: Transaction) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }

    fun closeTransactionReceipt() {
        _uiState.update { it.copy(selectedTransaction = null) }
    }

    fun clearWithdrawalCode() {
        _uiState.update { it.copy(activeWithdrawalCode = null) }
    }

    fun generateWithdrawalCode(amount: Double, currency: Currency) {
        val code = "${Random.nextInt(100, 999)} ${Random.nextInt(100, 999)}"
        _uiState.update {
            it.copy(
                activeWithdrawalCode = WithdrawalCodeData(
                    code = code,
                    amount = amount,
                    currency = currency
                )
            )
        }
    }

    fun sendMoney(
        recipientName: String,
        recipientPhone: String,
        amount: Double,
        currency: Currency,
        notes: String?
    ) {
        val result = repository.sendMoney(
            recipientName = recipientName,
            recipientPhone = recipientPhone,
            amount = amount,
            currency = currency,
            notes = notes
        )
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, currentScreen = FutaScreen.HOME) }
            setNotification("Transfert envoyé avec succès (frais réduits de 1% appliqués) !")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur lors du transfert")
        }
    }

    fun depositFromLinked(linkedAccount: LinkedAccount, amount: Double, currency: Currency) {
        val result = repository.depositFromLinked(linkedAccount, amount, currency)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, currentScreen = FutaScreen.HOME) }
            setNotification("Recharge effectuée avec succès ! Frais : 0 FC (GRATUIT)")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur lors de la recharge")
        }
    }

    fun withdrawToLinked(linkedAccount: LinkedAccount, amount: Double, currency: Currency) {
        val result = repository.withdrawToLinked(linkedAccount, amount, currency)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, currentScreen = FutaScreen.HOME) }
            setNotification("Virement vers ${linkedAccount.providerName} réussi !")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur de virement")
        }
    }

    fun payMerchant(merchantName: String, merchantCode: String, amount: Double, currency: Currency) {
        val result = repository.payMerchant(merchantName, merchantCode, amount, currency)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, currentScreen = FutaScreen.HOME) }
            setNotification("Paiement marchand réussi ! Frais client : 0 FC")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur de paiement")
        }
    }

    fun payBill(provider: BillProvider, targetNumber: String, amount: Double, currency: Currency) {
        val result = repository.payBill(provider, targetNumber, amount, currency)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, currentScreen = FutaScreen.HOME) }
            setNotification("Facture ${provider.name} payée avec succès ! Aucun frais ajouté.")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur de paiement de facture")
        }
    }

    fun exchangeCurrency(amount: Double, fromCurrency: Currency) {
        val result = repository.exchangeCurrency(amount, fromCurrency)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx, showExchangeModal = false) }
            setNotification("Conversion instantanée réussie au taux officiel !")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur lors de la conversion")
        }
    }

    fun contributeToTontine(tontineId: String) {
        val result = repository.contributeToTontine(tontineId)
        result.onSuccess { tx ->
            _uiState.update { it.copy(selectedTransaction = tx) }
            setNotification("Cotisation tontine validée ! Zéro frais.")
        }.onFailure { error ->
            setNotification(error.message ?: "Erreur lors de la cotisation")
        }
    }

    fun linkAccount(providerName: String, category: LinkedCategory, accountNumber: String, holderName: String) {
        repository.linkAccount(providerName, category, accountNumber, holderName)
        _uiState.update { it.copy(showLinkBankModal = false) }
        setNotification("Compte $providerName connecté avec succès à votre portefeuille Futa.")
    }

    fun setNotification(message: String) {
        _uiState.update { it.copy(notificationMessage = message) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    fun calculateTransferFee(amount: Double, currency: Currency): Double {
        return repository.calculateTransferFee(amount, currency)
    }

    fun calculateClassicFee(amount: Double, currency: Currency): Double {
        return repository.calculateClassicTelecomFee(amount, currency)
    }
}
