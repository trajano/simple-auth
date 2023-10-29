package net.trajano.simpleauth

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.security.Principal

@Controller
class RootController {
    @GetMapping("/")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun index(): Mono<ResponseEntity<Void>> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .map { ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header("X-Authentication-To-String", it.toString())
                .build() }
    }

//    @GetMapping("/login")
//    fun login(): String {
//        return "login"
//    }
//
//    @GetMapping("/whoami")
//    @ResponseBody
//    fun whoAmI(req: ServerHttpRequest): Map<String, Any> {
//        return mapOf("headers" to req.headers)
//    }
//    // = Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())

}
