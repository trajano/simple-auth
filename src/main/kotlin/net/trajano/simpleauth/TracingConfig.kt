package net.trajano.simpleauth

import io.lettuce.core.tracing.Tracing
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class TracingConfig {
    @Bean
    @Lazy
    fun foo(d: Tracing) = ForwardHeadersRedirectStrategy()
}