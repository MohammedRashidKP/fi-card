package com.closemates.games.websocket

import com.closemates.games.game.GameManager
import com.closemates.games.game.startNewGame
import game.SessionRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.controlRoutes(frontendUrl: String) {

    post("/rooms") {
        val roomId = GameManager.createRoom()
        if (roomId != null) {
            val response = CreateRoomResponse(success = true)
            if (response.success){
                response.roomId = roomId
                response.joinUrl = "${frontendUrl}/join/${roomId}"
            }
            call.respond(response)
        } else {
            call.respond(CreateRoomResponse(success = false))
        }
    }

    post("/rooms/{roomId}/start") {
        val roomId = call.parameters["roomId"]!!
        val room = GameManager.getRoom(roomId)

        if (room == null) {
            call.respond(HttpStatusCode.NotFound, "Room not found")
            return@post
        }

        room.startNewGame()

        SessionRegistry.sendToAll(room)

        call.respond(HttpStatusCode.OK, "Game started")
    }

}
