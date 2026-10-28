package com.example.data.repository

import com.example.data.model.AgentPoint
import com.example.data.model.BillProvider
import com.example.data.model.CategorySpendingSummary
import com.example.data.model.Currency
import com.example.data.model.LinkedAccount
import com.example.data.model.LinkedCategory
import com.example.data.model.SpendingCategory
import com.example.data.model.TontineGroup
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.WalletAccount
import com.example.data.model.getSpendingCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

class FutaRepository {

    private val _account = MutableStateFlow(
        WalletAccount(
            userName = "Jonathan Mudiambata",
            userPhone = "+243 82 145 9081",
            futaId = "FT-821459",
            cdfBalance = 485000.0,
            usdBalance = 175.50,
            exchangeRateUsdToCdf = 2850.0,
            isCardFrozen = false,
            virtualCardNumber = "4243 •••• •••• 9081",
            isBalanceVisible = true
        )
    )
    val account: StateFlow<WalletAccount> = _account.asStateFlow()

    private val _transactions = MutableStateFlow(createInitialTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _linkedAccounts = MutableStateFlow(createInitialLinkedAccounts())
    val linkedAccounts: StateFlow<List<LinkedAccount>> = _linkedAccounts.asStateFlow()

    private val _agentPoints = MutableStateFlow(createInitialAgentPoints())
    val agentPoints: StateFlow<List<AgentPoint>> = _agentPoints.asStateFlow()

    private val _billProviders = MutableStateFlow(createInitialBillProviders())
    val billProviders: StateFlow<List<BillProvider>> = _billProviders.asStateFlow()

    private val _tontineGroups = MutableStateFlow(createInitialTontines())
    val tontineGroups: StateFlow<List<TontineGroup>> = _tontineGroups.asStateFlow()

    // 1% fee calculation with ceiling (Wave style ultra-low fee)
    fun calculateTransferFee(amount: Double, currency: Currency): Double {
        val rawFee = amount * 0.01 // 1%
        return if (currency == Currency.CDF) {
            // Min 50 FC, Max 5000 FC
            rawFee.coerceIn(50.0, 5000.0)
        } else {
            // Min $0.05, Max $2.00
            rawFee.coerceIn(0.05, 2.00)
        }
    }

    // Classic Mobile money fee comparison (traditional telecom charges 3% to 4.5% + withdrawal tax)
    fun calculateClassicTelecomFee(amount: Double, currency: Currency): Double {
        val telecomRate = 0.035 // ~3.5%
        return if (currency == Currency.CDF) {
            (amount * telecomRate).coerceAtLeast(350.0)
        } else {
            (amount * telecomRate).coerceAtLeast(0.50)
        }
    }

    fun toggleBalanceVisibility() {
        _account.update { it.copy(isBalanceVisible = !it.isBalanceVisible) }
    }

    fun toggleCardLock() {
        _account.update { it.copy(isCardFrozen = !it.isCardFrozen) }
    }

    fun toggleBiometric() {
        _account.update { it.copy(isBiometricEnabled = !it.isBiometricEnabled) }
    }

    fun getMonthlySpending(
        monthOffset: Int = 0
    ): List<CategorySpendingSummary> {
        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L
        val rate = _account.value.exchangeRateUsdToCdf

        val (minTimestamp, maxTimestamp) = when (monthOffset) {
            0 -> Pair(now - 30 * day, now + 1 * day) // Ce mois (30 derniers jours)
            1 -> Pair(now - 60 * day, now - 30 * day) // Mois dernier
            else -> Pair(now - 90 * day, now - 60 * day) // Il y a 2 mois
        }

        val eligibleTxs = _transactions.value.filter { tx ->
            tx.timestamp in minTimestamp..maxTimestamp && tx.getSpendingCategory() != null
        }

        val totalSpendingCdf = eligibleTxs.sumOf { tx ->
            if (tx.currency == Currency.CDF) tx.amount else tx.amount * rate
        }

        if (totalSpendingCdf <= 0.0) {
            return emptyList()
        }

        val grouped = eligibleTxs.groupBy { it.getSpendingCategory()!! }

        return grouped.map { (cat, txs) ->
            val catTotalCdf = txs.sumOf { tx ->
                if (tx.currency == Currency.CDF) tx.amount else tx.amount * rate
            }
            val catTotalUsd = catTotalCdf / rate
            val percentage = ((catTotalCdf / totalSpendingCdf) * 100f).toFloat()

            CategorySpendingSummary(
                category = cat,
                totalCdf = catTotalCdf,
                totalUsd = catTotalUsd,
                transactionCount = txs.size,
                percentage = percentage
            )
        }.sortedByDescending { it.totalCdf }
    }

    fun getTransactionsForCategory(
        category: SpendingCategory?,
        monthOffset: Int = 0
    ): List<Transaction> {
        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L
        val (minTimestamp, maxTimestamp) = when (monthOffset) {
            0 -> Pair(now - 30 * day, now + 1 * day)
            1 -> Pair(now - 60 * day, now - 30 * day)
            else -> Pair(now - 90 * day, now - 60 * day)
        }

        return _transactions.value.filter { tx ->
            val isWithinWindow = tx.timestamp in minTimestamp..maxTimestamp
            val txCat = tx.getSpendingCategory()
            if (category == null) {
                isWithinWindow && txCat != null
            } else {
                isWithinWindow && txCat == category
            }
        }
    }

    fun sendMoney(
        recipientName: String,
        recipientPhone: String,
        amount: Double,
        currency: Currency,
        notes: String? = null
    ): Result<Transaction> {
        val fee = calculateTransferFee(amount, currency)
        val totalDeduction = amount + fee

        val current = _account.value
        val hasSufficientFunds = if (currency == Currency.CDF) {
            current.cdfBalance >= totalDeduction
        } else {
            current.usdBalance >= totalDeduction
        }

        if (!hasSufficientFunds) {
            return Result.failure(Exception("Solde insuffisant pour effectuer ce transfert (frais de 1% inclus)."))
        }

        // Deduct balance
        _account.update {
            if (currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance - totalDeduction)
            } else {
                it.copy(usdBalance = it.usdBalance - totalDeduction)
            }
        }

        val ref = "FT-TRF-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Envoi à $recipientName",
            subtitle = recipientPhone,
            amount = amount,
            currency = currency,
            fee = fee,
            type = TransactionType.TRANSFER_SENT,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            reference = ref,
            partnerName = "Futa Transfert",
            recipientPhone = recipientPhone,
            notes = notes
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun depositFromLinked(
        linkedAccount: LinkedAccount,
        amount: Double,
        currency: Currency
    ): Result<Transaction> {
        // Dépôt 100% GRATUIT chez Futa
        val fee = 0.0

        _account.update {
            if (currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance + amount)
            } else {
                it.copy(usdBalance = it.usdBalance + amount)
            }
        }

        val ref = "FT-DEP-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Recharge depuis ${linkedAccount.providerName}",
            subtitle = "Compte ${linkedAccount.accountNumber}",
            amount = amount,
            currency = currency,
            fee = fee,
            type = TransactionType.BANK_TOPUP,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            reference = ref,
            partnerName = linkedAccount.providerName
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun withdrawToLinked(
        linkedAccount: LinkedAccount,
        amount: Double,
        currency: Currency
    ): Result<Transaction> {
        val fee = calculateTransferFee(amount, currency)
        val totalDeduction = amount + fee

        val current = _account.value
        val hasSufficientFunds = if (currency == Currency.CDF) {
            current.cdfBalance >= totalDeduction
        } else {
            current.usdBalance >= totalDeduction
        }

        if (!hasSufficientFunds) {
            return Result.failure(Exception("Solde insuffisant pour virer vers votre banque/mobile money."))
        }

        _account.update {
            if (currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance - totalDeduction)
            } else {
                it.copy(usdBalance = it.usdBalance - totalDeduction)
            }
        }

        val ref = "FT-VIR-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Virement vers ${linkedAccount.providerName}",
            subtitle = linkedAccount.accountNumber,
            amount = amount,
            currency = currency,
            fee = fee,
            type = TransactionType.BANK_WITHDRAWAL,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            reference = ref,
            partnerName = linkedAccount.providerName
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun payMerchant(
        merchantName: String,
        merchantCode: String,
        amount: Double,
        currency: Currency
    ): Result<Transaction> {
        // Paiement marchand GRATUIT pour le client (0% fee)
        val fee = 0.0
        val current = _account.value
        val hasSufficientFunds = if (currency == Currency.CDF) {
            current.cdfBalance >= amount
        } else {
            current.usdBalance >= amount
        }

        if (!hasSufficientFunds) {
            return Result.failure(Exception("Solde insuffisant pour ce paiement marchand."))
        }

        _account.update {
            if (currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance - amount)
            } else {
                it.copy(usdBalance = it.usdBalance - amount)
            }
        }

        val ref = "FT-MER-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Paiement $merchantName",
            subtitle = "Code Marchand #$merchantCode",
            amount = amount,
            currency = currency,
            fee = fee,
            type = TransactionType.MERCHANT_PAY,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            reference = ref,
            partnerName = merchantName
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun payBill(
        provider: BillProvider,
        targetNumber: String,
        amount: Double,
        currency: Currency
    ): Result<Transaction> {
        val fee = 0.0 // 0% fee on bill payments
        val current = _account.value
        val hasSufficientFunds = if (currency == Currency.CDF) {
            current.cdfBalance >= amount
        } else {
            current.usdBalance >= amount
        }

        if (!hasSufficientFunds) {
            return Result.failure(Exception("Solde insuffisant pour régler cette facture."))
        }

        _account.update {
            if (currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance - amount)
            } else {
                it.copy(usdBalance = it.usdBalance - amount)
            }
        }

        val generatedToken = if (provider.isInstantToken) {
            // Generate a 20-digit SNEL CashPower token formatted in 4-digit blocks
            "${Random.nextInt(1000, 9999)} ${Random.nextInt(1000, 9999)} ${Random.nextInt(1000, 9999)} ${Random.nextInt(1000, 9999)} ${Random.nextInt(1000, 9999)}"
        } else null

        val ref = "FT-FAC-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "${provider.name} - Facture réglée",
            subtitle = "Réf: $targetNumber",
            amount = amount,
            currency = currency,
            fee = fee,
            type = TransactionType.BILL_PAYMENT,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            reference = ref,
            partnerName = provider.name,
            notes = generatedToken?.let { "Token Énergie: $it" }
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun exchangeCurrency(
        fromAmount: Double,
        fromCurrency: Currency
    ): Result<Transaction> {
        val rate = _account.value.exchangeRateUsdToCdf
        val current = _account.value

        if (fromCurrency == Currency.CDF) {
            if (current.cdfBalance < fromAmount) {
                return Result.failure(Exception("Solde CDF insuffisant."))
            }
            val convertedUsd = fromAmount / rate
            _account.update {
                it.copy(
                    cdfBalance = it.cdfBalance - fromAmount,
                    usdBalance = it.usdBalance + convertedUsd
                )
            }
            val ref = "FT-FX-${Random.nextInt(100000, 999999)}"
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                title = "Conversion CDF vers USD",
                subtitle = "Taux: 1 USD = $rate FC",
                amount = fromAmount,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.CURRENCY_EXCHANGE,
                reference = ref,
                partnerName = "Futa Change"
            )
            _transactions.update { listOf(tx) + it }
            return Result.success(tx)
        } else {
            if (current.usdBalance < fromAmount) {
                return Result.failure(Exception("Solde USD insuffisant."))
            }
            val convertedCdf = fromAmount * rate
            _account.update {
                it.copy(
                    usdBalance = it.usdBalance - fromAmount,
                    cdfBalance = it.cdfBalance + convertedCdf
                )
            }
            val ref = "FT-FX-${Random.nextInt(100000, 999999)}"
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                title = "Conversion USD vers CDF",
                subtitle = "Taux: 1 USD = $rate FC",
                amount = fromAmount,
                currency = Currency.USD,
                fee = 0.0,
                type = TransactionType.CURRENCY_EXCHANGE,
                reference = ref,
                partnerName = "Futa Change"
            )
            _transactions.update { listOf(tx) + it }
            return Result.success(tx)
        }
    }

    fun contributeToTontine(tontineId: String): Result<Transaction> {
        val tontine = _tontineGroups.value.find { it.id == tontineId }
            ?: return Result.failure(Exception("Tontine introuvable."))

        val amount = tontine.targetCycleAmount
        val current = _account.value

        val hasFunds = if (tontine.currency == Currency.CDF) {
            current.cdfBalance >= amount
        } else {
            current.usdBalance >= amount
        }

        if (!hasFunds) {
            return Result.failure(Exception("Solde insuffisant pour la cotisation tontine."))
        }

        _account.update {
            if (tontine.currency == Currency.CDF) {
                it.copy(cdfBalance = it.cdfBalance - amount)
            } else {
                it.copy(usdBalance = it.usdBalance - amount)
            }
        }

        _tontineGroups.update { list ->
            list.map {
                if (it.id == tontineId) it.copy(hasUserPaidThisRound = true) else it
            }
        }

        val ref = "FT-TNT-${Random.nextInt(100000, 999999)}"
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            title = "Cotisation Tontine : ${tontine.title}",
            subtitle = "Tour ${tontine.currentCycleRound}/${tontine.totalRounds} pour ${tontine.nextRecipient}",
            amount = amount,
            currency = tontine.currency,
            fee = 0.0,
            type = TransactionType.TONTINE_CONTRIBUTION,
            reference = ref,
            partnerName = "Tontine Kitemo"
        )

        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun linkAccount(providerName: String, category: LinkedCategory, accountNumber: String, holderName: String) {
        val colorHex = when {
            providerName.contains("Rawbank", true) -> 0xFFFDB913
            providerName.contains("Equity", true) -> 0xFFC8102E
            providerName.contains("TMB", true) -> 0xFF00843D
            providerName.contains("M-Pesa", true) -> 0xFFE60000
            providerName.contains("Orange", true) -> 0xFFFF6600
            providerName.contains("Airtel", true) -> 0xFFFF0000
            else -> 0xFF0056D2
        }

        val newAcc = LinkedAccount(
            id = UUID.randomUUID().toString(),
            providerName = providerName,
            category = category,
            accountNumber = accountNumber,
            holderName = holderName,
            accentColorHex = colorHex,
            logoText = providerName.take(3).uppercase()
        )

        _linkedAccounts.update { it + newAcc }
    }

    private fun createInitialTransactions(): List<Transaction> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            // --- Current Month (Ce mois - Octobre) ---
            // Airtime (Crédits & Forfaits)
            Transaction(
                id = "tx-airtime-1",
                title = "Forfait Internet Airtel 10 Go",
                subtitle = "Recharge Data +243 99 821 4590",
                amount = 28500.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 3 * hour,
                reference = "FT-AIR-892104",
                partnerName = "Airtel RDC Airtime",
                notes = "Forfait 30 Jours 10 Go 4G LTE"
            ),
            Transaction(
                id = "tx-airtime-2",
                title = "Crédit d'appel Vodacom",
                subtitle = "Airtime direct +243 81 294 8831",
                amount = 5.0,
                currency = Currency.USD,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 2 * day,
                reference = "FT-AIR-551920",
                partnerName = "Vodacom RDC Airtime",
                notes = "Recharge crédit d'appel"
            ),
            Transaction(
                id = "tx-airtime-3",
                title = "Forfait Nuit Orange 5 Go",
                subtitle = "Recharge Data +243 89 554 1120",
                amount = 5700.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 7 * day,
                reference = "FT-AIR-220194",
                partnerName = "Orange RDC Airtime",
                notes = "Pass Nuit 5 Go"
            ),

            // Utility Bills (Factures d'Énergie & Eau & TV)
            Transaction(
                id = "tx-bill-1",
                title = "SNEL CashPower - Électricité",
                subtitle = "Compteur #014493821",
                amount = 35000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 4 * day,
                reference = "FT-FAC-401928",
                partnerName = "SNEL CashPower",
                notes = "Token: 8492 1049 3810 5920 1842"
            ),
            Transaction(
                id = "tx-bill-2",
                title = "REGIDESO - Facture d'eau",
                subtitle = "Réf Client RGD-99384-KIN",
                amount = 25000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 10 * day,
                reference = "FT-FAC-192847",
                partnerName = "REGIDESO",
                notes = "Facture Eau Gombe Sept/Oct"
            ),
            Transaction(
                id = "tx-bill-3",
                title = "CANAL+ Bouquet Évasion",
                subtitle = "Carte Décodeur 1449 2819 04",
                amount = 20.0,
                currency = Currency.USD,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 14 * day,
                reference = "FT-FAC-773918",
                partnerName = "CANAL+ RDC",
                notes = "Abonnement 30 jours"
            ),

            // Transfers (Envois d'argent P2P & Tontines)
            Transaction(
                id = "tx-trf-1",
                title = "Envoi à Grace Mwamba",
                subtitle = "+243 81 294 8831",
                amount = 45000.0,
                currency = Currency.CDF,
                fee = 450.0,
                type = TransactionType.TRANSFER_SENT,
                timestamp = now - 1 * day,
                reference = "FT-TRF-902314",
                partnerName = "Futa Transfert",
                recipientPhone = "+243 81 294 8831",
                notes = "Frais scolaires"
            ),
            Transaction(
                id = "tx-trf-2",
                title = "Envoi à Patrick Tshisekedi",
                subtitle = "+243 82 401 9283",
                amount = 25.0,
                currency = Currency.USD,
                fee = 0.25,
                type = TransactionType.TRANSFER_SENT,
                timestamp = now - 5 * day,
                reference = "FT-TRF-110294",
                partnerName = "Futa Transfert",
                recipientPhone = "+243 82 401 9283",
                notes = "Remboursement courses"
            ),
            Transaction(
                id = "tx-trf-3",
                title = "Cotisation Tontine Kitemo",
                subtitle = "Tour 3 pour Maman Rachel",
                amount = 50000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.TONTINE_CONTRIBUTION,
                timestamp = now - 8 * day,
                reference = "FT-TNT-492019",
                partnerName = "Tontine Kitemo"
            ),

            // Merchant Purchases (Achats & Commerces)
            Transaction(
                id = "tx-mer-1",
                title = "Kin Mart Supermarché",
                subtitle = "Succursale Gombe",
                amount = 28500.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.MERCHANT_PAY,
                timestamp = now - 6 * day,
                reference = "FT-MER-773910",
                partnerName = "Kin Mart Gombe"
            ),
            Transaction(
                id = "tx-mer-2",
                title = "Pharmacie Moderne de Kinshasa",
                subtitle = "Avenue De la Paix",
                amount = 18000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.MERCHANT_PAY,
                timestamp = now - 12 * day,
                reference = "FT-MER-392019",
                partnerName = "Pharmacie Moderne"
            ),

            // Cash Withdrawals (Retraits)
            Transaction(
                id = "tx-wdr-1",
                title = "Retrait Kiosque Futa Express",
                subtitle = "Agent Gombe Boulevard 30 Juin",
                amount = 40000.0,
                currency = Currency.CDF,
                fee = 400.0,
                type = TransactionType.BANK_WITHDRAWAL,
                timestamp = now - 9 * day,
                reference = "FT-WDR-501928",
                partnerName = "Kiosque Futa Gombe"
            ),

            // Inflows (Not counted in spending chart, but visible in transaction history)
            Transaction(
                id = "tx-in-1",
                title = "Recharge depuis Rawbank Illicocash",
                subtitle = "Compte *4819",
                amount = 100.0,
                currency = Currency.USD,
                fee = 0.0,
                type = TransactionType.BANK_TOPUP,
                timestamp = now - 5 * hour,
                reference = "FT-DEP-118492",
                partnerName = "Rawbank Illicocash"
            ),
            Transaction(
                id = "tx-in-2",
                title = "Reçu de Dieudonné Kabeya",
                subtitle = "Transfert instantané Futa",
                amount = 120000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.TRANSFER_RECEIVED,
                timestamp = now - 11 * day,
                reference = "FT-REC-654812",
                partnerName = "Futa Transfert"
            ),

            // --- Previous Month (Mois dernier - Septembre) ---
            Transaction(
                id = "tx-prev-1",
                title = "Forfait Internet Airtel 15 Go",
                subtitle = "Recharge Data Mensuelle",
                amount = 42000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 35 * day,
                reference = "FT-AIR-001923",
                partnerName = "Airtel RDC Airtime"
            ),
            Transaction(
                id = "tx-prev-2",
                title = "SNEL CashPower - Électricité",
                subtitle = "Compteur #014493821",
                amount = 30000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.BILL_PAYMENT,
                timestamp = now - 38 * day,
                reference = "FT-FAC-881920",
                partnerName = "SNEL CashPower"
            ),
            Transaction(
                id = "tx-prev-3",
                title = "Envoi à Patrick Tshisekedi",
                subtitle = "+243 82 401 9283",
                amount = 60000.0,
                currency = Currency.CDF,
                fee = 600.0,
                type = TransactionType.TRANSFER_SENT,
                timestamp = now - 42 * day,
                reference = "FT-TRF-667192",
                partnerName = "Futa Transfert"
            ),
            Transaction(
                id = "tx-prev-4",
                title = "Kin Mart Supermarché",
                subtitle = "Courses alimentaires",
                amount = 34000.0,
                currency = Currency.CDF,
                fee = 0.0,
                type = TransactionType.MERCHANT_PAY,
                timestamp = now - 45 * day,
                reference = "FT-MER-994012",
                partnerName = "Kin Mart Gombe"
            )
        )
    }

    private fun createInitialLinkedAccounts(): List<LinkedAccount> {
        return listOf(
            LinkedAccount(
                id = "link-1",
                providerName = "Rawbank (Illicocash)",
                category = LinkedCategory.BANK,
                accountNumber = "05100-01048192-01",
                holderName = "Jonathan Mudiambata",
                accentColorHex = 0xFFFDB913,
                logoText = "RAW"
            ),
            LinkedAccount(
                id = "link-2",
                providerName = "M-Pesa (Vodacom RDC)",
                category = LinkedCategory.MOBILE_MONEY,
                accountNumber = "+243 82 145 9081",
                holderName = "Jonathan Mudiambata",
                accentColorHex = 0xFFE60000,
                logoText = "VOD"
            ),
            LinkedAccount(
                id = "link-3",
                providerName = "Equity BCDC",
                category = LinkedCategory.BANK,
                accountNumber = "00018-09214482-55",
                holderName = "Jonathan Mudiambata",
                accentColorHex = 0xFFC8102E,
                logoText = "EQT"
            ),
            LinkedAccount(
                id = "link-4",
                providerName = "Orange Money RDC",
                category = LinkedCategory.MOBILE_MONEY,
                accountNumber = "+243 89 554 1120",
                holderName = "Jonathan Mudiambata",
                accentColorHex = 0xFFFF6600,
                logoText = "ORA"
            ),
            LinkedAccount(
                id = "link-5",
                providerName = "TMB (Pepele Mobile)",
                category = LinkedCategory.BANK,
                accountNumber = "00012-33019482-19",
                holderName = "Jonathan Mudiambata",
                accentColorHex = 0xFF00843D,
                logoText = "TMB"
            )
        )
    }

    private fun createInitialAgentPoints(): List<AgentPoint> {
        return listOf(
            AgentPoint(
                id = "ag-1",
                name = "Kiosque Futa Express - Gombe",
                commune = "Gombe",
                address = "Boulevard du 30 Juin, en face de l'Hôtel Memling",
                distanceKm = 0.3,
                hasCdfCash = true,
                hasUsdCash = true,
                phone = "+243 82 440 1022"
            ),
            AgentPoint(
                id = "ag-2",
                name = "Agence Futa Partenaire - Bandal",
                commune = "Bandalungwa",
                address = "Avenue Tshibangu, Arrêt Bakayawu",
                distanceKm = 1.2,
                hasCdfCash = true,
                hasUsdCash = true,
                phone = "+243 81 993 4019"
            ),
            AgentPoint(
                id = "ag-3",
                name = "Point Relais Futa - Limete 7ème",
                commune = "Limete",
                address = "7ème Rue Résidentielle, Rond-point Place Commerciale",
                distanceKm = 2.8,
                hasCdfCash = true,
                hasUsdCash = true,
                phone = "+243 89 220 5511"
            ),
            AgentPoint(
                id = "ag-4",
                name = "Kiosque Futa - Kintambo Magasin",
                commune = "Kintambo",
                address = "Croisement Avenue Kasa-Vubu & OUA",
                distanceKm = 3.5,
                hasCdfCash = true,
                hasUsdCash = false, // CDF only at this moment
                phone = "+243 82 001 9283"
            ),
            AgentPoint(
                id = "ag-5",
                name = "Futa Hub - Matete Marché",
                commune = "Matete",
                address = "Marché Principal de Matete, Kiosque #42",
                distanceKm = 5.1,
                hasCdfCash = true,
                hasUsdCash = true,
                phone = "+243 84 311 0984"
            )
        )
    }

    private fun createInitialBillProviders(): List<BillProvider> {
        return listOf(
            BillProvider(
                id = "prov-snel",
                name = "SNEL CashPower (Électricité)",
                category = "Électricité",
                description = "Achat direct de jetons d'électricité prépayée 24h/24",
                inputPrompt = "Numéro de compteur CashPower",
                inputPlaceholder = "ex: 01449382103",
                suggestedAmountsCdf = listOf(10000.0, 25000.0, 50000.0, 100000.0),
                suggestedAmountsUsd = listOf(5.0, 10.0, 20.0, 50.0),
                isInstantToken = true
            ),
            BillProvider(
                id = "prov-regideso",
                name = "REGIDESO (Facture d'eau)",
                category = "Eau",
                description = "Paiement direct de facture d'eau sans déplacement",
                inputPrompt = "Référence client / Facture",
                inputPlaceholder = "ex: RGD-99384-KIN",
                suggestedAmountsCdf = listOf(15000.0, 30000.0, 60000.0),
                suggestedAmountsUsd = listOf(6.0, 12.0, 25.0)
            ),
            BillProvider(
                id = "prov-canal",
                name = "CANAL+ RDC",
                category = "Télévision",
                description = "Réabonnement instantané bouquets Access, Évasion, Tout Canal",
                inputPrompt = "Numéro de carte décodeur CANAL+",
                inputPlaceholder = "ex: 14 chiffres sur la carte",
                suggestedAmountsCdf = listOf(30000.0, 60000.0, 120000.0),
                suggestedAmountsUsd = listOf(12.0, 25.0, 50.0)
            ),
            BillProvider(
                id = "prov-easytv",
                name = "EasyTV RDC",
                category = "Télévision",
                description = "Abonnement mensuel chaînes TNT Kinshasa & Lubumbashi",
                inputPrompt = "Numéro de décodeur EasyTV",
                inputPlaceholder = "ex: 0284918239",
                suggestedAmountsCdf = listOf(15000.0, 30000.0),
                suggestedAmountsUsd = listOf(6.0, 12.0)
            ),
            BillProvider(
                id = "prov-airtel",
                name = "Airtel RDC (Crédit & Forfaits)",
                category = "Télécoms",
                description = "Recharge crédit d'appel et forfaits internet 4G LTE sans surcoût",
                inputPrompt = "Numéro Airtel à recharger",
                inputPlaceholder = "ex: 0998214590",
                suggestedAmountsCdf = listOf(2850.0, 5700.0, 14250.0, 28500.0),
                suggestedAmountsUsd = listOf(1.0, 2.0, 5.0, 10.0)
            ),
            BillProvider(
                id = "prov-vodacom",
                name = "Vodacom RDC (Airtime & Pass)",
                category = "Télécoms",
                description = "Recharge crédit d'appel et forfaits M-Pesa / Vodacom",
                inputPrompt = "Numéro Vodacom à recharger",
                inputPlaceholder = "ex: 0812948831",
                suggestedAmountsCdf = listOf(2850.0, 5700.0, 14250.0, 28500.0),
                suggestedAmountsUsd = listOf(1.0, 2.0, 5.0, 10.0)
            ),
            BillProvider(
                id = "prov-orange",
                name = "Orange RDC (Crédit & Pass Data)",
                category = "Télécoms",
                description = "Recharge directe forfaits internet et crédit Orange",
                inputPrompt = "Numéro Orange à recharger",
                inputPlaceholder = "ex: 0895541120",
                suggestedAmountsCdf = listOf(2850.0, 5700.0, 14250.0, 28500.0),
                suggestedAmountsUsd = listOf(1.0, 2.0, 5.0, 10.0)
            )
        )
    }

    private fun createInitialTontines(): List<TontineGroup> {
        return listOf(
            TontineGroup(
                id = "tont-1",
                title = "Kitemo Famille Mwamba & Amis",
                targetCycleAmount = 50000.0,
                currency = Currency.CDF,
                frequency = "Chaque Vendredi",
                membersCount = 8,
                currentCycleRound = 3,
                totalRounds = 8,
                nextRecipient = "Maman Rachel",
                hasUserPaidThisRound = false
            ),
            TontineGroup(
                id = "tont-2",
                title = "Tontine Entrepreneurs Gombe Tech",
                targetCycleAmount = 100.0,
                currency = Currency.USD,
                frequency = "Fin du mois",
                membersCount = 10,
                currentCycleRound = 4,
                totalRounds = 10,
                nextRecipient = "Jonathan (Vous)",
                hasUserPaidThisRound = true
            )
        )
    }
}
