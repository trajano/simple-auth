package net.trajano.simpleauth

import io.lettuce.core.tracing.Tracing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class SimpleAuthenticationServerApplication

fun main(args: Array<String>) {
	Hooks.enableAutomaticContextPropagation()
	Tracing.disabled()
	runApplication<SimpleAuthenticationServerApplication>(*args)
}
