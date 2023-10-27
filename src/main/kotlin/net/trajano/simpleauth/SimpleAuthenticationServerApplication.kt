package net.trajano.simpleauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleAuthenticationServerApplication

fun main(args: Array<String>) {
	runApplication<SimpleAuthenticationServerApplication>(*args)
}
