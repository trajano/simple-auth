package net.trajano.simpleauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono

@Configuration
class RequestHeaderLoggingFilter {
    @Bean
    fun logRequestHeaders(): WebFilter {
        return WebFilter { exchange, chain ->
            val requestHeaders = exchange.request.headers
            logHeaders(requestHeaders)
            chain.filter(exchange)
        }.apply {
            setOrder(Ordered.HIGHEST_PRECEDENCE) // Set the order to be highest
        }
    }

    private fun logHeaders(headers: HttpHeaders) {
        headers.forEach { key, values ->
            values.forEach { value ->
                println("Request Header: $key = $value")
            }
        }
    }
}
