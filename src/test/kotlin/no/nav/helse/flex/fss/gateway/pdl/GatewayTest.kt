package no.nav.helse.flex.fss.gateway.pdl

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToPattern
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
        "nais.cluster=test",
    ],
)
@AutoConfigureWireMock(port = 0)
class GatewayTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun testHealth() {
        webClient
            .get().uri("/internal/health")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `ok kall videresendes`() {
        stubFor(
            get(urlEqualTo("/api/v1/veileder/vedtak"))
                .willReturn(
                    aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")
                )
        )

        webClient
            .get().uri("/spinnsyn-backend/api/v1/veileder/vedtak")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World")
    }

    @Test
    fun `500 kall videresendes`() {
        stubFor(
            get(urlEqualTo("/api/v1/veileder/vedtak"))
                .willReturn(
                    aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                )
        )

        webClient
            .get().uri("/spinnsyn-backend/api/v1/veileder/vedtak")
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World")
    }

    @Test
    fun `ukjent api returnerer 404`() {
        webClient
            .post().uri("/dfgasdyfghuadsfgliuafdg")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `selvbetjening-idtoken cookie flyttes til auth header`() {
        stubFor(
            get(urlEqualTo("/api/v1/veileder/vedtak"))
                .withHeader("Authorization", EqualToPattern("Bearer napoleonskake"))
                .willReturn(
                    aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")
                )
        )

        webClient
            .get().uri("/spinnsyn-backend/api/v1/veileder/vedtak")
            .cookie("selvbetjening-idtoken", "napoleonskake")
            .exchange()

            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World")
    }

    @Test
    fun `isso-idtoken cookie flyttes til auth header`() {
        stubFor(
            get(urlEqualTo("/api/v1/veileder/vedtak"))
                .withHeader("Authorization", EqualToPattern("Bearer napoleonskake"))
                .willReturn(
                    aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")
                )
        )

        webClient
            .get().uri("/spinnsyn-backend/api/v1/veileder/vedtak")
            .cookie("isso-idtoken", "napoleonskake")
            .exchange()

            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World")
    }

    @Test
    fun `isso cookie flyttes ikke til auth header hvis eksisterende auth header`() {
        stubFor(
            get(urlEqualTo("/api/v1/veileder/vedtak"))
                .withHeader("Authorization", EqualToPattern("Bearer original"))
                .willReturn(
                    aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")
                )
        )

        webClient
            .get().uri("/spinnsyn-backend/api/v1/veileder/vedtak")
            .header("Authorization", "Bearer original")
            .cookie("isso-idtoken", "napoleonskake")
            .exchange()

            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.headers.Hello").isEqualTo("World")
    }
}
