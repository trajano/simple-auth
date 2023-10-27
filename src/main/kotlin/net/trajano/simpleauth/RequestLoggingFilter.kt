package net.trajano.simpleauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint
import org.springframework.security.web.server.ui.LoginPageGeneratingWebFilter
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter
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
        val loginPageGeneratingWebFilter = LoginPageGeneratingWebFilter()
        loginPageGeneratingWebFilter.setFormLoginEnabled(true)
        http
            .addFilterBefore(loginPageGeneratingWebFilter, SecurityWebFiltersOrder.LOGIN_PAGE_GENERATING)
            .addFilterBefore(LogoutPageGeneratingWebFilter(), SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING)
//            .addFilterBefore(
//                { exchange, chain ->
//                    val requestHeaders = exchange.request.headers
//                    logHeaders(requestHeaders)
//                    print(exchange.request.sslInfo)
//                    print(exchange.request.method)
//                    print(exchange.request.uri)
//                    print(exchange.request.remoteAddress)
//                    chain.filter(exchange)
//                },
//                SecurityWebFiltersOrder.HTTPS_REDIRECT
//            )
            .authorizeExchange { exchanges ->
                exchanges.pathMatchers(HttpMethod.GET, "/actuator/health").hasIpAddress("127.0.0.1")
            }
//            .authorizeExchange { exchanges -> exchanges.pathMatchers("/login").permitAll() }
            .authorizeExchange { exchanges -> exchanges.anyExchange().authenticated() }
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
            .exceptionHandling {
                val redirectServerAuthenticationEntryPoint =
                    RedirectServerAuthenticationEntryPoint("/login")
                redirectServerAuthenticationEntryPoint.setRedirectStrategy(ForwardHeadersRedirectStrategy())
                it.authenticationEntryPoint(redirectServerAuthenticationEntryPoint)
            }
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
