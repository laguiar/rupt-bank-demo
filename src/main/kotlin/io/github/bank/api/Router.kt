package io.github.bank.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

const val ACCOUNTS_API = "/api/v1/accounts"

@Configuration
class ApplicationRouter(private val accountHandler: AccountHandler) {

    @Bean
    fun bankRouter(): RouterFunction<ServerResponse> =
        router {
            "/api/v1/accounts".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", accountHandler::listAccounts)
                    GET("/{iban}", accountHandler::getAccount)
                    GET("/{iban}/transactions", accountHandler::listAccountTransactions)

                    POST("") { request ->
                        request.bodyToMono(AccountForm::class.java).flatMap { form ->
                            accountHandler.createAccount(form)
                        }.switchIfEmpty(ServerResponse.badRequest().build())
                    }

                    POST("/{iban}/deposit") { request ->
                        request.bodyToMono(DepositForm::class.java).flatMap { form ->
                            accountHandler.deposit(request, form)
                        }.switchIfEmpty(ServerResponse.badRequest().build())
                    }

                    POST("/{iban}/transfer") { request ->
                        request.bodyToMono(TransferForm::class.java).flatMap { form ->
                            accountHandler.transfer(request.pathVariable("iban"), form)
                        }.switchIfEmpty(ServerResponse.badRequest().build())
                    }

                    PUT("/{iban}/locker", accountHandler::changeLockState)
                }
            }
        }
}
