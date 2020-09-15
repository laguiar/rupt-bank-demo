package io.github.bank.api

import io.github.bank.domain.AccountTransactionType
import io.github.bank.domain.AccountType
import java.math.BigDecimal
import java.util.*

data class DepositForm(
    val amount: BigDecimal
)

data class TransferForm(
    val payee: String,
    val amount: BigDecimal
)

data class AccountDto(
    val iban: String,
    val branchNumber: Short,
    val number: Int,
    val type: AccountType,
    val allowedTransactions: Set<AccountTransactionType>,
    val balance: BigDecimal,
    val isLocked: Boolean
)

data class TransactionDto(
    val id: UUID,
    val amount: BigDecimal,
    val type: AccountTransactionType,
    val payee: String? = null
)

data class AccountForm(
    val customerId: UUID,
    val branchNumber: Short,
    val number: Int,
    val type: AccountType
)

data class LockedDto(
    val locked: Boolean
)

data class IbanDto(val iban: String)
