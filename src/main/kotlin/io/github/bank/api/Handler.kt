package io.github.bank.api

import io.github.bank.domain.*
import io.github.bank.domain.AccountTransactionType.*
import org.iban4j.CountryCode
import org.iban4j.Iban
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono

@Component
class AccountHandler(
    private val repository: AccountRepository,
    private val transactionRepository: AccountTransactionRepository,
    private val customerRepository: CustomerRepository
) {

    // TODO move it to properties file
    private val initialAllowedTransactions = mapOf(
        AccountType.CHECKING to setOf(DEPOSIT, WITHDRAWN, TRANSFER_IN, TRANSFER_OUT),
        AccountType.SAVINGS to setOf(DEPOSIT, WITHDRAWN, TRANSFER_IN, TRANSFER_OUT),
        AccountType.PRIVATE_LOAN to setOf(DEPOSIT, TRANSFER_IN)
    )

    /**
     * Creates a new account for a given customer
     * Iban value is randomly generated
     */
    @Transactional
    fun createAccount(form: AccountForm): Mono<ServerResponse> =
        when (customerRepository.existsById(form.customerId)) {
            true -> {
                val account = Account(
                    customer = customerRepository.getOne(form.customerId),
                    branchNumber = form.branchNumber,
                    number = form.number,
                    iban = Iban.random(CountryCode.DE).toString(),
                    type = form.type,
                    allowedTransactions = initialAllowedTransactions[form.type] ?: emptySet()
                )
                val saved = repository.save(account)
                ServerResponse.status(HttpStatus.CREATED).bodyValue(IbanDto(saved.iban))
            }

            false -> ServerResponse.unprocessableEntity().build()
        }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun deposit(iban: String, form: DepositForm): Mono<ServerResponse> =
        repository.findByIban(iban)?.let { account ->
            // verify if account can perform the transaction type
            when (account.isCapableOf(DEPOSIT)) {
                true -> {
                    val updatedAccount = repository.save(
                        account.copy(balance = account.balance + form.amount)
                    )
                    val accountTransaction = AccountTransaction(
                        account = updatedAccount,
                        amount = form.amount,
                        type = DEPOSIT
                    )
                    transactionRepository.save(accountTransaction)

                    ServerResponse.accepted().build()
                }

                false -> ServerResponse.status(HttpStatus.FORBIDDEN).build()
            }

        } ?: ServerResponse.notFound().build()


    @Transactional(readOnly = true)
    fun getAccount(request: ServerRequest): Mono<ServerResponse> =
        repository.findByIban(request.pathVariable("iban"))?.let { account ->
            ServerResponse.ok().bodyValue(account.toDto())
        } ?: ServerResponse.notFound().build()


    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun transfer(iban: String, form: TransferForm): Mono<ServerResponse> {
        val accountOut = repository.findByIban(iban)
        val accountIn = repository.findByIban(form.payee)

        if (accountIn == null || accountOut == null)
            return ServerResponse.notFound().build()

        // check if both accounts are capable of transfers
        if (accountOut.isCapableOf(TRANSFER_OUT).not()
            || accountIn.isCapableOf(TRANSFER_IN).not())
            return ServerResponse.status(HttpStatus.FORBIDDEN).build()

        // check if a saving account is transferring to its checking account
        if (accountOut.type == AccountType.SAVINGS
            && accountIn.savingAccount?.iban != accountOut.iban)
            return ServerResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).build()

        // negative balance is not being considered
        val updatedOut = accountOut.copy(balance = accountOut.balance - form.amount)
        val updatedIn = accountIn.copy(balance = accountIn.balance + form.amount)
        repository.saveAll(listOf(updatedIn, updatedOut))

        // add a new transaction
        val transaction = AccountTransaction(
            account = updatedOut,
            amount = form.amount,
            payee = accountIn.iban,
            type = TRANSFER_OUT
        )
        transactionRepository.save(transaction)

        return ServerResponse.accepted().build()
    }

    @Transactional(readOnly = true)
    fun listAccounts(request: ServerRequest): Mono<ServerResponse> =
        when (request.queryParam("types").isPresent) {
            true -> {
                val types = request.queryParamOrNull("types")?.let { query ->
                    AccountType.values()
                        .map { it.name }
                        .filter { query.contains(it) }
                        .map { AccountType.valueOf(it) }
                } ?: emptyList()

                val accounts = repository.findAllByTypeIn(types).map(Account::toDto)
                ServerResponse.ok().bodyValue(accounts)
            }

            false -> ServerResponse.ok().bodyValue(repository.findAll().map(Account::toDto))
        }

    /**
     * List all transactions made by an account and the ones that account was the payee
     */
    @Transactional(readOnly = true)
    fun listAccountTransactions(request: ServerRequest): Mono<ServerResponse> =
        repository.findByIban(request.pathVariable("iban"))?.let { account ->
            val transactions = transactionRepository.findAllByAccount(account)
            val payeeTransactions = transactionRepository.findAllByPayee(account.iban)
            val result = transactions.plus(payeeTransactions).sortedByDescending { it.createdAt }

            ServerResponse.ok().bodyValue(result)
        } ?: ServerResponse.notFound().build()


    /**
     * Update the current lock state of an account by inverting it
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun changeLockState(request: ServerRequest): Mono<ServerResponse> =
        repository.findByIban(request.pathVariable("iban"))?.let { account ->
            val updated = account.copy(locked = account.isLocked().not())
            repository.save(updated)

            ServerResponse.ok().bodyValue(LockedDto(locked = updated.isLocked()))
        } ?: ServerResponse.notFound().build()

}
