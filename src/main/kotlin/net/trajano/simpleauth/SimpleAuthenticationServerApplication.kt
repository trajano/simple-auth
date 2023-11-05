package net.trajano.simpleauth

import io.lettuce.core.resource.DefaultClientResources
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class SimpleAuthenticationServerApplication

fun main(args: Array<String>) {
	Hooks.enableAutomaticContextPropagation()
	runApplication<SimpleAuthenticationServerApplication>(*args)
}
