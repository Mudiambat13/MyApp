package com.example

import com.example.data.model.Currency
import com.example.data.model.LinkedCategory
import com.example.data.repository.FutaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: FutaRepository

    @Before
    fun setUp() {
        repository = FutaRepository()
    }

    @Test
    fun calculateTransferFee_isAccurateOnePercentWithCeiling() {
        // 100,000 CDF -> 1% = 1,000 CDF
        val fee100k = repository.calculateTransferFee(100000.0, Currency.CDF)
        assertEquals(1000.0, fee100k, 0.01)

        // 1,000,000 CDF -> 1% = 10,000 CDF, but capped at 5,000 CDF
        val fee1M = repository.calculateTransferFee(1000000.0, Currency.CDF)
        assertEquals(5000.0, fee1M, 0.01)

        // 50 USD -> 1% = 0.50 USD
        val feeUsd = repository.calculateTransferFee(50.0, Currency.USD)
        assertEquals(0.50, feeUsd, 0.01)

        // 500 USD -> 1% = 5.00 USD, capped at 2.00 USD
        val feeHighUsd = repository.calculateTransferFee(500.0, Currency.USD)
        assertEquals(2.00, feeHighUsd, 0.01)
    }

    @Test
    fun sendMoney_deductsAmountPlusReducedFee() {
        val initialCdf = repository.account.value.cdfBalance
        val sendAmount = 50000.0
        val fee = repository.calculateTransferFee(sendAmount, Currency.CDF) // 500.0

        val result = repository.sendMoney(
            recipientName = "Dieudonné Kabeya",
            recipientPhone = "+243 89 104 2209",
            amount = sendAmount,
            currency = Currency.CDF
        )

        assertTrue(result.isSuccess)
        val expectedBalance = initialCdf - (sendAmount + fee)
        assertEquals(expectedBalance, repository.account.value.cdfBalance, 0.01)
    }

    @Test
    fun depositFromLinked_isCompletelyFreeZeroFee() {
        val initialUsd = repository.account.value.usdBalance
        val depositAmount = 25.0
        val linkedAccount = repository.linkedAccounts.value.first()

        val result = repository.depositFromLinked(linkedAccount, depositAmount, Currency.USD)
        assertTrue(result.isSuccess)
        val tx = result.getOrNull()
        assertEquals(0.0, tx?.fee ?: 1.0, 0.001)
        assertEquals(initialUsd + depositAmount, repository.account.value.usdBalance, 0.01)
    }

    @Test
    fun exchangeCurrency_swapsAtOfficialRateWithoutCommission() {
        val initialCdf = repository.account.value.cdfBalance
        val initialUsd = repository.account.value.usdBalance
        val rate = repository.account.value.exchangeRateUsdToCdf // 2850.0

        val usdToSell = 10.0
        val result = repository.exchangeCurrency(usdToSell, Currency.USD)
        assertTrue(result.isSuccess)

        assertEquals(initialUsd - usdToSell, repository.account.value.usdBalance, 0.01)
        assertEquals(initialCdf + (usdToSell * rate), repository.account.value.cdfBalance, 0.01)
    }

    @Test
    fun payBill_snelGeneratesTwentyDigitToken() {
        val snelProvider = repository.billProviders.value.first { it.id == "prov-snel" }
        val result = repository.payBill(snelProvider, "01449382103", 10000.0, Currency.CDF)

        assertTrue(result.isSuccess)
        val tx = result.getOrNull()
        assertTrue(tx?.notes?.contains("Token Énergie:") == true)
        assertEquals(0.0, tx?.fee ?: 1.0, 0.001)
    }
}
