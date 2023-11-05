package net.trajano.simpleauth

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Mono

@Controller
class RootController {
    companion object {
        val log = LoggerFactory.getLogger(RootController::class.java)
    }

    @GetMapping("/")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun index(): Mono<ResponseEntity<Void>> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .map {
                // at this point there would likely be a transformation to provide a proper header for the application
                // for this simple example we simply output to string and the class name
                val headers = HttpHeaders()
                headers["X-Authentication-Class"] = it.javaClass.toString()
                headers["X-Authentication-To-String"] = it.toString()
                headers
            }
            .map {
                ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .headers(it)
                    .build()
            }
    }

    //    @GetMapping("/login")
//    fun login(): String {
//        return "login"
//    }
//
    @GetMapping("/whoami2")
    @org.springframework.web.bind.annotation.ResponseBody
    fun whoAmI(req: ServerHttpRequest): Map<String, Any> {
        log.info("whomami 2 requested")
        return mapOf("headers" to req.headers)
    }

    @GetMapping("/whoami3")
    @org.springframework.web.bind.annotation.ResponseBody
    fun whoAmI3(req: ServerHttpRequest): Map<String, Any> {
        log.info("whomami 3 requested")
        return mapOf("headers" to req.headers)
    }
//    // = Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())

}
