package no.nav.helse.flex.gateway.cookie

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthCookieTilHeaderFlytter : GlobalFilter {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val httpCookie = exchange.request.cookies.hentAuthCookie()
        return if (httpCookie != null && exchange.request.manglerAuthHeader()) {
            val mutertRequest = exchange.request.mutate().header("Authorization", "Bearer ${httpCookie.value}").build()
            chain.filter(exchange.mutate().request(mutertRequest).build())
        } else {
            chain.filter(exchange)
        }
    }

    private fun MultiValueMap<String, HttpCookie>.hentAuthCookie(): HttpCookie? {
        this.getFirst("selvbetjening-idtoken")?.let {
            return it
        }
        this.getFirst("isso-idtoken")?.let {
            return it
        }
        return null
    }

    private fun ServerHttpRequest.manglerAuthHeader(): Boolean {
        return !this.headers.containsKey("Authorization")
    }
}
