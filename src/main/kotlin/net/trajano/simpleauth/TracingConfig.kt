package net.trajano.simpleauth

import io.lettuce.core.resource.DefaultClientResources
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TracingConfig {
    @Bean
    fun foo(d: DefaultClientResources) = ForwardHeadersRedirectStrategy()
}