package io.github.bank.api

import io.github.bank.domain.AccountRepository
import io.github.bank.domain.AccountType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class BankApiTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Test
    @Order(1)
    fun shouldDepositIntoAccount() {
        // checking account
        val iban = "DE06286592751368036575"
        val amount = BigDecimal("1000")
        val form = DepositForm(amount)
        val balance = accountRepository.findByIban(iban)?.balance

        webClient.post()
            .uri("$ACCOUNTS_API/{iban}/deposit", iban)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(form)
            .exchange()
            .expectStatus().isAccepted

        val currentBalance = accountRepository.findByIban(iban)?.balance
        assertEquals(balance?.plus(amount), currentBalance)
    }

    @Test
    @Order(2)
    fun shouldTransferBetweenAccounts() {
        // savings account
        val saIban = "DE70977433509451310526"
        // checking account
        val caIban = "DE06286592751368036575"

        val savingsBalance = accountRepository.findByIban(saIban)?.balance
        val checkingBalance = accountRepository.findByIban(caIban)?.balance
        val amount = BigDecimal(500)

        val form = TransferForm(
            payee = caIban,
            amount = amount
        )

        webClient.post()
            .uri("$ACCOUNTS_API/{iban}/transfer", saIban)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(form)
            .exchange()
            .expectStatus().isAccepted


        val currentSavingsBalance = accountRepository.findByIban(saIban)?.balance
        val currentCheckingBalance = accountRepository.findByIban(caIban)?.balance

        assertEquals(checkingBalance?.plus(amount), currentCheckingBalance)
        assertEquals(savingsBalance?.minus(amount), currentSavingsBalance)
    }

    @Test
    fun shouldReadAccountBalance() {
        val iban = "DE06286592751368036575"

        webClient.get()
            .uri("$ACCOUNTS_API/{iban}", iban)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.balance").isNumber
    }

    @Test
    fun shouldListAndFilterAccounts() {
        webClient.get()
            .uri("$ACCOUNTS_API?types=PRIVATE_LOAN,SAVINGS")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(AccountDto::class.java)
            .hasSize(2)
    }

    @Test
    @Order(3)
    fun shouldListAccountTransactionHistory() {
        // checking account
        val iban = "DE06286592751368036575"

        webClient.get()
            .uri("$ACCOUNTS_API/{iban}/transactions", iban)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(TransactionDto::class.java)
            .hasSize(2)
    }

    @Test
    fun shouldCreateAccount() {
        val form = AccountForm(
            customerId = UUID.fromString("5ce15c7e-3589-444a-a260-1c3a5a970c96"),
            branchNumber = 12345,
            number = 987654321,
            type = AccountType.PRIVATE_LOAN
        )

        webClient.post()
            .uri(ACCOUNTS_API)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(form)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.iban").isNotEmpty
    }

    @Test
    fun shouldChangeAccountLockState() {
        // checking account
        val iban = "DE06286592751368036575"

        webClient.put()
            .uri("$ACCOUNTS_API/{iban}/locker", iban)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.locked").isEqualTo(true)
    }

}
