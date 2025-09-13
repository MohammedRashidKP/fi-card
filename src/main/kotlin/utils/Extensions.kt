package com.closemates.games.utils

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> DefaultWebSocketServerSession.sendJson(data: T) {
    val jsonString = Json.encodeToString(data)
    send(Frame.Text(jsonString))
}
