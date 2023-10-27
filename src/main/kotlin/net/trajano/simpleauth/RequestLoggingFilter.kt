package net.trajano.simpleauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.WebFilter

@Configuration
class RequestHeaderLoggingFilter {

    @Bean
    fun userDetailsService() = MapReactiveUserDetailsService(
        User.withDefaultPasswordEncoder()
            .username("user")
            .password("user")
            .roles("USER")
            .build()
    )

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.addFilterBefore(
             WebFilter { exchange, chain ->
                val requestHeaders = exchange.request.headers
                logHeaders(requestHeaders)
                chain.filter(exchange)
            }, SecurityWebFiltersOrder.HTTPS_REDIRECT
        ).authorizeExchange { exchanges -> exchanges.anyExchange().authenticated() }
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
        return http.build()
    }

    @Bean
    fun logRequestHeaders(): WebFilter {
        return WebFilter { exchange, chain ->
            val requestHeaders = exchange.request.headers
            logHeaders(requestHeaders)
            chain.filter(exchange)
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
