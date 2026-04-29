package com.knowave.cashboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CashboardApplication

fun main(args: Array<String>) {
	runApplication<CashboardApplication>(*args)
}
