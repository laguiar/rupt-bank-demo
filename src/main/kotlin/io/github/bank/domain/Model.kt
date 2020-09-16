package io.github.bank.domain

import io.github.bank.api.AccountDto
import io.github.bank.api.TransactionDto
import org.hibernate.annotations.NaturalId
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
data class Customer(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val documentId: Int
)

@Entity
data class Account(
    @Id
    val id: UUID = UUID.randomUUID(),

    @NaturalId
    @Column(updatable = false, unique = true)
    val iban: String,

    val branchNumber: Short,
    val number: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    val customer: Customer,

    @Enumerated(EnumType.STRING)
    val type: AccountType,

    @ElementCollection(targetClass = AccountTransactionType::class, fetch = FetchType.EAGER)
    @CollectionTable(name = "account_capabilities", joinColumns = [JoinColumn(name = "account_id")])
    @Enumerated(EnumType.STRING)
    val allowedTransactions: Set<AccountTransactionType> = setOf(AccountTransactionType.DEPOSIT),

    val balance: BigDecimal = BigDecimal.ZERO,

    @OneToOne(optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_account_id", unique = true, updatable = false)
    val savingAccount: Account? = null,

    val locked: Boolean = false
) {
    fun isCapableOf(transactionType: AccountTransactionType) = allowedTransactions.contains(transactionType)

    fun isLocked() = locked

    fun toDto() = AccountDto(
        iban = iban,
        branchNumber = branchNumber,
        number = number,
        balance = balance,
        type = type,
        allowedTransactions = allowedTransactions,
        isLocked = isLocked()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        if (id != other.id) return false
        if (iban != other.iban) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(iban)
    }
}

enum class AccountType {
    CHECKING, SAVINGS, PRIVATE_LOAN
}

@Entity
data class AccountTransaction(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    val account: Account,

    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val type: AccountTransactionType,

    val payee: String? = null,
    val createdAt: Instant = Instant.now()
) {
    fun toDto() = TransactionDto(
        id = this.id,
        amount = this.amount,
        type = this.type,
        payee = this.payee
    )
}

enum class AccountTransactionType {
    DEPOSIT, WITHDRAWN, TRANSFER_IN, TRANSFER_OUT
}
