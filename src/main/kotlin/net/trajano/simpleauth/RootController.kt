package net.trajano.simpleauth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class RootController {
    @GetMapping("/")
    @ResponseStatus(code= HttpStatus.NO_CONTENT)
    fun index() : Mono<ResponseEntity<Void>> = Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())

}