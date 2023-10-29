package net.trajano.simpleauth

import org.springframework.http.HttpMethod
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.savedrequest.ServerRequestCache
import org.springframework.web.server.ServerWebExchange
import java.net.URI

class ForwardHeadersAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
    private val requestCache: ServerRequestCache;
    private val location: URI

    constructor(location: String, requestCache: ServerRequestCache) {
        this.location = URI.create(location)
        this.requestCache = requestCache
    }

    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException?) =
        requestCache
            .saveRequest(
                exchange.mutate()
                    .request(
                        exchange.request.mutate()
                            .method(HttpMethod.valueOf(exchange.request.headers["x-forwarded-method"]?.first() ?: "GET"))
                            .path(exchange.request.headers["X-Forwarded-Uri"]?.first() ?: "/")
                            .build()
                    )
                    .build()
            )
            .then(ForwardHeadersRedirectStrategy().sendRedirect(exchange, location))


}