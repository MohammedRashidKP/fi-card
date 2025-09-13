package com.closemates.games

import com.closemates.games.websocket.controlRoutes
import com.closemates.games.websocket.gameRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.time.Duration
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import io.netty.handler.codec.rtsp.RtspHeaderValues.URL
import java.net.URL

fun Application.configureSockets(frontendUrl: String) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        gameRoutes()
        controlRoutes(frontendUrl)
        staticResources("/", "static")

    }

}

fun Application.configureCORS(frontendUrl: String) {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowCredentials = true
        val url = URL(frontendUrl)
        allowHost(url.host, schemes = listOf(url.protocol.removeSuffix(":")))

        // Optionally also allow localhost dev
        allowHost("localhost", schemes = listOf("http"))
    }
}
