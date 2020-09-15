package io.github.bank.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CustomerRepository : JpaRepository<Customer, UUID>

interface AccountRepository : JpaRepository<Account, UUID> {
    fun findByIban(iban: String): Account?
    fun findAllByTypeIn(types: List<AccountType>): Set<Account>
}

interface AccountTransactionRepository : JpaRepository<AccountTransaction, UUID> {
    fun findAllByAccount(account: Account): Set<AccountTransaction>
    fun findAllByPayee(iban: String): Set<AccountTransaction>
}
