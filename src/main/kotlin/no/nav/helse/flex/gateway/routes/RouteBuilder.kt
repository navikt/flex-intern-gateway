package no.nav.helse.flex.gateway.routes

import no.nav.helse.flex.gateway.environment.EnvironmentToggles
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod

@Configuration
class RouteBuilder(
    private val environmentToggles: EnvironmentToggles
) {

    @Bean
    fun myRoutes(builder: RouteLocatorBuilder, services: List<Service>, env: Environment): RouteLocator {
        var routes = builder.routes()

        services
            .filter { !(it.devOnly && environmentToggles.isProduction()) }
            .forEach { service ->
                val uri = env.getProperty(service.serviceurlProperty)
                    ?: throw RuntimeException("Fant ikke property ${service.serviceurlProperty}")

                fun addPath(paths: List<String>, metode: HttpMethod) {
                    paths.map { "/${service.basepath}$it" }
                        .forEach { path ->
                            routes = routes.route("${metode.name()} $path") { p: PredicateSpec ->
                                p.path(path)
                                    .and()
                                    .method(metode)
                                    .filters { f ->
                                        f.rewritePath("/${service.basepath}(?<segment>/?.*)", "\$\\{segment}")
                                    }
                                    .uri(uri)
                            }
                        }
                }
                addPath(service.paths.delete, HttpMethod.DELETE)
                addPath(service.paths.put, HttpMethod.PUT)
                addPath(service.paths.post, HttpMethod.POST)
                addPath(service.paths.get, HttpMethod.GET)
            }

        return routes.build()
    }
}
