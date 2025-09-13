package com.closemates.games.websocket

import com.closemates.games.game.*
import com.closemates.games.models.GameState
import game.SessionRegistry
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.gameRoutes() {
    webSocket("/game") {
        var joinedRoomId: String? = null
        try {
            // Main loop: stays alive as long as client is connected
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val json = Json { ignoreUnknownKeys = true }
                    val clientMsg = json.decodeFromString<ClientMessage>(message)
                    joinedRoomId = clientMsg.roomId
                    try {
                        when (clientMsg.action) {
                            "joinRoom" -> {
                                val room = GameManager.getRoom(clientMsg.roomId) ?: return@consumeEach

                                val existingPlayer = room.players.find { it.id == clientMsg.playerId }

                                if (existingPlayer != null) {
                                    // Player already exists: treat as reconnect
                                    SessionRegistry.addSession(room.id, this, clientMsg.playerId)
                                    application.log.info("Player ${clientMsg.playerId} reconnected to room ${room.id}")
                                    SessionRegistry.sendToAll(room)
                                } else {
                                    // New player trying to join
                                    if (room.gameState != GameState.LOBBY) {
                                        application.log.info("Cannot join ${clientMsg.playerId} , game in progress at ${room.id}")
                                    } else {
                                        if (clientMsg.name != null) {
                                            val newPlayer = Player(clientMsg.playerId, clientMsg.name)
                                            SessionRegistry.addSession(room.id, this, clientMsg.playerId)
                                            GameManager.joinRoom(room.id, newPlayer)
                                            application.log.info("Player ${clientMsg.playerId} joined room ${room.id}")
                                        }
                                    }
                                }
                            }


                            "addAI" -> {
                                GameManager.addAI(clientMsg.roomId)
                                send("AI added")
                            }

                            "pickOpen" -> {
                                GameManager.pickOpenCard(clientMsg.roomId, clientMsg.playerId)
                            }

                            "pickDeck" -> {
                                GameManager.pickDeckCard(clientMsg.roomId, clientMsg.playerId)
                            }

                            "discard" -> {
                                GameManager.discardCards(
                                    clientMsg.roomId,
                                    clientMsg.playerId,
                                    clientMsg.cardsToDiscard
                                )
                            }

                            "call" -> {
                                GameManager.call(clientMsg.roomId, clientMsg.playerId)
                            }

                            "strike" -> {
                                GameManager.strike(clientMsg.roomId, clientMsg.playerId)
                            }
                            "startNewRound" -> {
                                GameManager.startNewRound(clientMsg.roomId)
                            }
                            "forceEnd" -> {
                                val room = GameManager.getRoom(clientMsg.roomId)
                                if (room != null) {
                                    room.jokerCard = Card(Suit.SPADES, Rank.TEN)
                                    for (player in room.players) {
                                        player.hand.clear()
                                    }
                                room.players[0].hand
                                    .addAll(listOf(
                                        Card(Suit.SPADES, Rank.FOUR),
                                        Card(Suit.CLUBS, Rank.ACE),
                                    ))
                                    room.players[1].hand
                                        .addAll(listOf(
                                            Card(Suit.SPADES, Rank.ACE),
                                            Card(Suit.CLUBS, Rank.TWO),
                                        ))
                                    room.players[2].hand
                                        .addAll(listOf(
                                            Card(Suit.SPADES, Rank.JACK),
                                            Card(Suit.CLUBS, Rank.QUEEN),
                                        ))
                                    room.players[3].hand
                                        .addAll(listOf(
                                            Card(Suit.SPADES, Rank.JACK),
                                            Card(Suit.CLUBS, Rank.QUEEN),
                                            Card(Suit.HEARTS, Rank.FOUR),
                                            Card(Suit.DIAMONDS, Rank.TEN)
                                        ))
                                    room.currentIndex = 1
                                    SessionRegistry.sendToAll(room)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        application.log.error("Error processing client message", e)
                        send("Error: ${e.message}")
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            application.log.info("WebSocket closed by client: ${closeReason.await()}")
        } catch (e: Throwable) {
            application.log.error("WebSocket error", e)
        } finally {
            joinedRoomId?.let {
                application.log.info("Cleaning up WS session in room $it: $this")
                SessionRegistry.removeSession(it, this)
            }
        }
    }
}
