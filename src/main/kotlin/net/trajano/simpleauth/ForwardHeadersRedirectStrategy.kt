package net.trajano.simpleauth

import org.springframework.http.HttpStatus
import org.springframework.security.web.server.ServerRedirectStrategy
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

class ForwardHeadersRedirectStrategy : ServerRedirectStrategy {
    override fun sendRedirect(exchange: ServerWebExchange, location: URI): Mono<Void> =
        Mono.fromRunnable {
            val response = exchange.response
            response.setStatusCode(HttpStatus.SEE_OTHER)
            val newLocation: URI = createLocation(exchange, location)
            response.headers.location = newLocation

        }

    private fun createLocation(exchange: ServerWebExchange, location: URI): URI {
        val url = location.toASCIIString()
        if (url.startsWith("/") && exchange.request.headers["x-forwarded-proto"] != null) {
            val context = exchange.request.path.contextPath().value()
            val forwardedProto = exchange.request.headers["x-forwarded-proto"]!![0]
            var forwardedPort : String? = null
            if (exchange.request.headers["x-forwarded-port"] != null) {
                forwardedPort = exchange.request.headers["x-forwarded-port"]!![0]
            }
            when {
                "https" == forwardedProto && "443" == forwardedPort -> {
                    forwardedPort = null;
                }

                "http" == forwardedProto && "80" == forwardedPort -> {
                    forwardedPort = null;
                }
            }
            return UriComponentsBuilder.newInstance()
                .scheme(forwardedProto)
                .host(exchange.request.headers["x-forwarded-host"]!![0])
                .port(forwardedPort)
                .path(context)
                .path(location.path)
                .query(location.query)
                .build()
                .toUri()
        }
        return location
    }

}
