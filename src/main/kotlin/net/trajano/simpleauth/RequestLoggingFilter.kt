package net.trajano.simpleauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache
import org.springframework.security.web.server.ui.LoginPageGeneratingWebFilter
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter

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
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        userDetailsService: ReactiveUserDetailsService
    ): SecurityWebFilterChain {
        val reactiveAuthenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)

        val requestCache = WebSessionServerRequestCache()
        val forwardHeadersRedirectStrategy = ForwardHeadersRedirectStrategy()
        val authenticationEntryPoint =
            ForwardHeadersAuthenticationEntryPoint("/login", requestCache)

        val authenticationSuccessHandler = RedirectServerAuthenticationSuccessHandler("/")
        authenticationSuccessHandler.setRedirectStrategy(forwardHeadersRedirectStrategy)
        authenticationSuccessHandler.setRequestCache(requestCache)

        val authenticationFailureHandler = RedirectServerAuthenticationFailureHandler("/login?error")
        authenticationFailureHandler.setRedirectStrategy(forwardHeadersRedirectStrategy)

        val loginPageGeneratingWebFilter = LoginPageGeneratingWebFilter()
        loginPageGeneratingWebFilter.setFormLoginEnabled(true)

        return http
            .addFilterAt(loginPageGeneratingWebFilter, SecurityWebFiltersOrder.LOGIN_PAGE_GENERATING)
            .addFilterAt(LogoutPageGeneratingWebFilter(), SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING)
            .authenticationManager(reactiveAuthenticationManager)
            .authorizeExchange { exchanges ->
                exchanges.pathMatchers(HttpMethod.GET, "/actuator/health").hasIpAddress("127.0.0.1")
            }
            .authorizeExchange { exchanges -> exchanges.anyExchange().authenticated() }
            .requestCache {
                it.requestCache(requestCache)
            }
            .httpBasic(withDefaults())
            .formLogin {
                it.authenticationFailureHandler(authenticationFailureHandler)
                it.loginPage("/login")
                it.authenticationSuccessHandler(authenticationSuccessHandler)
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .build()
    }

//    @Bean
//    fun logRequestHeaders(): WebFilter {
//        return WebFilter { exchange, chain ->
//            val requestHeaders = exchange.request.headers
//            logHeaders(requestHeaders)
//            chain.filter(exchange)
//        }
//    }
}
