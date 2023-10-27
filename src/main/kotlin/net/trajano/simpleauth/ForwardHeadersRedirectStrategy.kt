package net.trajano.simpleauth

import org.springframework.http.HttpStatus
import org.springframework.security.web.server.ServerRedirectStrategy
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

class ForwardHeadersRedirectStrategy(val loginPage: String) : ServerRedirectStrategy {
    override fun sendRedirect(exchange: ServerWebExchange, location: URI): Mono<Void> =
        Mono.fromRunnable<Void> {
            val response = exchange.response
            response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT)
            val newLocation: URI = createLocation(exchange, location)
            response.headers.location = newLocation

        }

    private fun createLocation(exchange: ServerWebExchange, location: URI): URI {
        val url = location.toASCIIString()
        if (url.startsWith("/")) {
            val context = exchange.request.path.contextPath().value()
            return UriComponentsBuilder.newInstance()
                .scheme(exchange.request.headers["x-forwarded-proto"]!![0])
                .host(exchange.request.headers["x-forwarded-host"]!![0])
                .port(exchange.request.headers["x-forwarded-port"]!![0])
                .path(context)
                .path(url)
                .build()
                .toUri()
        }
        return location
    }

}
