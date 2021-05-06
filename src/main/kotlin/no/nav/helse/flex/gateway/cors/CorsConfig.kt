package no.nav.helse.flex.gateway.cors

import no.nav.helse.flex.gateway.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import javax.annotation.PostConstruct

@Configuration
class CorsConfig(@Value("\${allowed.origins}") private val allowedOrigins: String) {

    private val allowedOriginsList: List<String> = allowedOrigins.split(",")

    val log = logger()

    @PostConstruct
    fun postContruct() {
        log.info("Allowed origins $allowedOriginsList")
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        corsConfig.allowedOrigins = allowedOriginsList
        corsConfig.allowCredentials = true
        corsConfig.allowedMethods = listOf("*")
        corsConfig.addAllowedHeader("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
}
