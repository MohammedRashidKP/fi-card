package com.closemates.games

import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    configureSerialization()
    configureSockets()
    configureCORS()
}
