package io.github.bank.api

import io.github.bank.domain.Account
import io.github.bank.domain.AccountRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class AccountFinder(private val repository: AccountRepository) {

    /**
     * Find an Account from the ServerRequest and execute the function argument
     * using the found Account as argument.
     * Returns a default HttpStatus.NOT_FOUND when an Account is not found.
     */
    fun findAccountFromRequest(request: ServerRequest, action: (account: Account) -> Mono<ServerResponse>) =
        repository.findByIban(request.pathVariable("iban"))
            ?.let { action(it) }
            ?: ServerResponse.notFound().build()

}
