package io.github.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["io.github.bank.domain"])
class BankDemoApplication

fun main(args: Array<String>) {
	runApplication<BankDemoApplication>(*args)
}
