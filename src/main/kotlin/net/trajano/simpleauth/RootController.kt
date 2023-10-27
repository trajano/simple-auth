package net.trajano.simpleauth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Controller
class RootController {
    @GetMapping("/")
    @ResponseStatus(code= HttpStatus.NO_CONTENT)
    fun index( @RequestHeader headers: Map<String, String> ) : Mono<ResponseEntity<Void>> {
        print(headers)
        return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())
    }

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }
    // = Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())

}
