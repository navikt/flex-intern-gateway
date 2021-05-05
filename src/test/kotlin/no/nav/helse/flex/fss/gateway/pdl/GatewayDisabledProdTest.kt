package no.nav.helse.flex.fss.gateway.pdl

import no.nav.helse.flex.gateway.Application
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = RANDOM_PORT,
    properties = [
        "spinnsyn.backend.url=http://localhost:\${wiremock.server.port}",
        "nais.cluster=prod-gcp",
    ],
)
@AutoConfigureWireMock(port = 0)
class GatewayDisabledProdTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun `testdata api gir 404 i prod`() {

        webClient
            .post().uri("/spinnsyn-backend-testdata/api/v2/testdata/vedtak/1234")
            .exchange()
            .expectStatus().isNotFound
    }
}
