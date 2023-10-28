package net.trajano.simpleauth

import io.micrometer.tracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
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
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain

@Configuration
//@EnableRedisWebSession
class RequestHeaderLoggingFilter {

//    @Bean
//    fun userDetailsService() = MapReactiveUserDetailsService(
//        User.withDefaultPasswordEncoder()
//            .username("user")
//            .password("user")
//            .roles("USER")
//            .build()
//    )

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        userDetailsService: ReactiveUserDetailsService,
        tracer: Tracer
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
            .addFilterBefore({ exchange, chain ->
                val headers: HttpHeaders = exchange.response.headers
                headers.add(
                    "traceparent",
                    "00-${tracer.currentSpan()!!.context().traceId()}-${tracer.currentSpan()!!.context().spanId()}-01"
                )
                chain.filter(exchange)
            }, SecurityWebFiltersOrder.HTTP_HEADERS_WRITER)
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


//    /**
//     * https://www.w3.org/TR/trace-context/
//     */
//    @Bean
//    fun tracingContextWebFilter(tracer: Tracer) =
//        WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
//            val headers: HttpHeaders = exchange.response.headers
//            headers.add(
//                "traceparent",
//                "00-${tracer.currentSpan()!!.context().traceId()}-${tracer.currentSpan()!!.context().spanId()}-01"
//            )
//            chain.filter(exchange)
//
//        }

//    @Bean
//    fun logRequestHeaders(): WebFilter {
//        return WebFilter { exchange, chain ->
//            val requestHeaders = exchange.request.headers
//            logHeaders(requestHeaders)
//            chain.filter(exchange)
//        }
//    }
}