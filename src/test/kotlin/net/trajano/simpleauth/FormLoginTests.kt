package net.trajano.simpleauth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.function.Consumer


@SpringBootTest
//@ContextConfiguration(classes = [SecurityConfig::class])
//@WebAppConfiguration
class FormLoginTests {

    @Autowired
    private lateinit var context: ApplicationContext

    private lateinit var rest: WebTestClient

    @Autowired
    private lateinit var userDetailsService: ReactiveUserDetailsService

    @Test
    fun `check user details`() {
        assertThat(userDetailsService)
            .isNotNull()

        StepVerifier
            .create(userDetailsService.findByUsername("user"))
            .expectNextMatches {
                it.username.equals("user")
            }
            .verifyComplete()

        val manager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        val authentication = UsernamePasswordAuthenticationToken("user", "user")
        StepVerifier
            .create(manager.authenticate(authentication)).expectNextMatches { it.isAuthenticated }
            .verifyComplete()

        val badauthentication = UsernamePasswordAuthenticationToken("user", "bad-password")
        StepVerifier
            .create(manager.authenticate(badauthentication))
            .verifyError()

    }

    @Test
    fun basic() {
        val cookieManager = CookieManager()
        this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            // add Spring Security test Support
            .apply(SecurityMockServerConfigurers.springSecurity())
            .configureClient()
            .defaultHeaders {
                it["x-forwarded-proto"] = "https"
                it["x-forwarded-host"] = "trajano.net"
                it["x-forwarded-port"] = "443"
            }
            .filter(cookieManager)
            .build()
        val noAuthResult = rest.get().uri("https://trajano.net/")
            .exchange()
            .expectStatus().isUnauthorized
            .returnResult(String::class.java)
        noAuthResult.assertWithDiagnostics {

        }

        val badAuthResult = rest.get().uri("https://trajano.net/")
            .headers {
                it.setBasicAuth("bad", "auth")
            }
            .exchange()
            .expectStatus().isUnauthorized
            .returnResult(String::class.java)
        badAuthResult.assertWithDiagnostics {

        }

        val goodAuthResult = rest.get().uri("https://trajano.net/")
            .headers {
                it.setBasicAuth("user", "user")
            }
            .exchange()
            .expectStatus().isNoContent
            .returnResult(String::class.java)
        goodAuthResult.assertWithDiagnostics {

        }

    }

    @Test
    fun `happy path`() {
        val cookieManager = CookieManager()
        this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            // add Spring Security test Support
            .apply(SecurityMockServerConfigurers.springSecurity())
            .configureClient()
            .defaultHeaders {
                it.accept = listOf(MediaType.TEXT_HTML)
                it["x-forwarded-proto"] = "https"
                it["x-forwarded-method"] = "GET"
                it["x-forwarded-host"] = "trajano.net"
                it["x-forwarded-port"] = "443"
                it["x-forwarded-uri"] = "/visualizer"
            }
            .filter(cookieManager)
            .build()
            .mutateWith(SecurityMockServerConfigurers.csrf())

        val accessProtectedPageResult = rest.get().uri("https://trajano.net/")
            .exchange()
            .expectStatus().is3xxRedirection
            .returnResult(String::class.java)
        accessProtectedPageResult.assertWithDiagnostics {
            assertThat(accessProtectedPageResult.responseHeaders.location)
                .hasPath("/login")
                .hasToString("https://trajano.net/login")

        }

        val loginPageResult = rest.get()
            .uri("https://trajano.net/login")
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
        loginPageResult.assertWithDiagnostics {
            assertThat(loginPageResult.responseBodyContent!!.decodeToString())
                .startsWith("<!DOCTYPE html>")
        }
        val authData = LinkedMultiValueMap<String, String>().apply {
            add("username", "user")
            add("password", "user")
        }
        rest
            .post()
            .uri("https://trajano.net/login")
            .headers {
                it.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            .body(BodyInserters.fromFormData(authData))
            .exchange()
            .expectStatus().isSeeOther
            .expectHeader().location("https://trajano.net/visualizer")


    }

    @Test
    fun `bad password`() {
        this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            // add Spring Security test Support
            .apply(SecurityMockServerConfigurers.springSecurity())
            .configureClient()
            .defaultHeaders {
                it["x-forwarded-method"] = "GET"
                it["x-forwarded-proto"] = "https"
                it["x-forwarded-host"] = "trajano.net"
                it["x-forwarded-port"] = "443"
                it["x-forwarded-uri"] = "/visualizer"
                it.accept = listOf(MediaType.TEXT_HTML)
            }
            .filter(CookieManager())
            .build()
            .mutateWith(SecurityMockServerConfigurers.csrf())
        val accessProtectedPageResult = rest.get().uri("https://trajano.net/")
            .exchange()
            .expectStatus().is3xxRedirection
            .returnResult(String::class.java)
        accessProtectedPageResult.assertWithDiagnostics {
            assertThat(accessProtectedPageResult.responseHeaders.location)
                .hasPath("/login")
            //.hasToString("https://trajano.net/login")
        }

        rest.get().uri("https://trajano.net/login")
            .exchange()
            .expectStatus().isOk

        val authData = LinkedMultiValueMap<String, String>().apply {
            add("username", "user")
            add("password", "incorrect-password")
        }
        val badLoginResult = rest
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("https://trajano.net/login")
            .headers {
                it.contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            .body(BodyInserters.fromFormData(authData))
            .exchange()
            .expectStatus().isSeeOther
            .returnResult(String::class.java)
        badLoginResult.assertWithDiagnostics {
            assertThat(badLoginResult.responseHeaders.location)
                .hasToString("https://trajano.net/login?error")
        }

    }

    internal class CookieManager : ExchangeFilterFunction {
        private val cookies: MutableMap<String, ResponseCookie> = HashMap()
        fun dump() {
            println(cookies)
        }

        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(withClientCookies(request)).doOnSuccess { response: ClientResponse ->
                response.cookies().values.forEach(
                    Consumer { cookies: List<ResponseCookie> ->
                        cookies.forEach(
                            Consumer { cookie: ResponseCookie ->
                                if (cookie.maxAge.isZero) {
                                    this.cookies.remove(cookie.name)
                                } else {
                                    this.cookies[cookie.name] = cookie
                                }
                            })
                    })
            }
        }

        private fun withClientCookies(request: ClientRequest): ClientRequest {
            return ClientRequest.from(request).cookies { c -> c.addAll(clientCookies()) }.build()
        }

        private fun clientCookies(): MultiValueMap<String, String> {
            val result: MultiValueMap<String, String> = LinkedMultiValueMap(cookies.size)
            cookies.values.forEach(Consumer { cookie: ResponseCookie ->
                result.add(
                    cookie.name,
                    cookie.value
                )
            })
            return result
        }
    }

}