package com.closemates.games

import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    val frontendUrl = environment.config.property("ktor.app.frontendUrl").getString()

    configureSerialization()
    configureSockets(frontendUrl)
    configureCORS(frontendUrl)
}
