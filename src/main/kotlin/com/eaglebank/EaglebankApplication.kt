package com.eaglebank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EaglebankApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EaglebankApplication>(*args)
}
