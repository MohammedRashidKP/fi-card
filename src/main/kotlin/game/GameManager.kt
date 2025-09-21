package com.closemates.games.game

import com.closemates.games.models.*
import game.SessionRegistry
import kotlinx.coroutines.delay

object GameManager {
    private val rooms = mutableMapOf<String, GameRoom>()

    private suspend fun broadcast(roomId: String) {
        val room = rooms[roomId] ?: return
        SessionRegistry.sendToAll(room)
    }

    fun createRoom(): String? {
        val roomId = generateRoomId()
        if (rooms.contains(roomId)) return null
        val room = GameRoom(id = roomId, roundNumber = 1)
        rooms[roomId] = room
        room.globalLeaderBoard = ScoreManager.getGlobalLeaderboard()
        return roomId
    }

    suspend fun joinRoom(roomId: String, player: Player) {
        val room = rooms[roomId] ?: return
        if (room.players.map { it.id }.contains(player.id)) {
            return
        }
        room.players.add(player)
        broadcast(roomId)
    }

    suspend fun pickOpenCard(roomId: String, playerId: String) {
        val room = rooms[roomId] ?: return
        if (room.currentPlayer().id != playerId) return

        val player = room.players.find { it.id == playerId } ?: return
        val open = player.openCard ?: return

        player.hand.add(open)
        player.openCard = null
        advanceTurn(room)
        SessionRegistry.sendSoundToPlayer(roomId, room.currentPlayer().id, SoundCue(SoundEvent.PLAYER_TURN))
        broadcast(roomId)
    }

    suspend fun pickDeckCard(roomId: String, playerId: String) {
        val room = rooms[roomId] ?: return
        if (room.currentPlayer().id != playerId) return

        val player = room.players.find { it.id == playerId } ?: return

        if (room.deck.size < 5) {
            val playerHands = room.players.flatMap { it.hand }
            val openCards = room.players.map { it.openCard }
            val newDeck = Card.shuffledDeck()
                .filter { !playerHands.contains(it) }
                .filter { !openCards.contains(it) }
                .filter { room.jokerCard != it }
            room.deck.clear()
            room.deck.addAll(newDeck)
        }

        val card = room.deck.removeFirstOrNull() ?: return

        player.hand.add(card)

        advanceTurn(room)

        if (room.currentPlayer().isBot)

            SessionRegistry.sendSoundToPlayer(roomId, room.currentPlayer().id, SoundCue(SoundEvent.PLAYER_TURN))
        broadcast(roomId)
    }

    private fun advanceTurn(room: GameRoom) {
        room.currentIndex = (room.currentIndex + 1) % room.players.size
    }

    suspend fun discardCards(roomId: String, playerId: String, cardsToDiscard: List<Card>) {
        val room = rooms[roomId] ?: return
        val playerIndex = room.players.indexOfFirst { it.id == playerId }
        if (playerIndex == -1) return

        val player = room.players[playerIndex]

        // ensure player actually has these cards
        if (!player.hand.containsAll(cardsToDiscard)) return

        // remove them from player's hand
        player.hand.removeAll(cardsToDiscard)

        // assign one of them as openCard for the next player
        val nextIndex = (playerIndex + 1) % room.players.size
        room.players[nextIndex].openCard = cardsToDiscard.first()
        broadcast(roomId)
    }

    suspend fun call(roomId: String, callerId: String) {
        val room = rooms[roomId] ?: return
        if (room.currentPlayer().id != callerId) return
        if (room.callerId != null) return
        val callValue = room.playerHandValue(callerId)
        if (callValue!! > 5) return
        room.callerId = callerId
        room.callValue = callValue
        room.gameState = GameState.CALLED
        SessionRegistry.sendSoundToAll(roomId, SoundCue(SoundEvent.CALL))
        delay(3000)
        broadcast(roomId)
        room.startStrikePhase {
            SessionRegistry.sendSoundToAll(roomId, SoundCue(SoundEvent.CALLER_WIN))
            finishRound(room)
        }
    }

    suspend fun strike(roomId: String, strikerId: String) {
        val room = rooms[roomId] ?: return
        if (room.strikerId != null) return
        if (room.gameState != GameState.CALLED) return
        val strikeValue = room.playerHandValue(strikerId)
        if (strikeValue!! > room.callValue!!) return
        SessionRegistry.sendSoundToAll(roomId, SoundCue(SoundEvent.STRIKE))
        room.cancelStrikePhase()
        room.strikerId = strikerId
        room.strikeValue = strikeValue
        delay(6000)
        SessionRegistry.sendSoundToAll(roomId, SoundCue(SoundEvent.STRIKER_WIN))
        finishRound(room)
    }

    private suspend fun finishRound(room: GameRoom) {
        room.gameState = GameState.FINISHED
        val scoreCard = room.getScoreCard()
        ScoreManager.saveLeaderBoardPoint(room.id, room.roundNumber, scoreCard)
        room.roundHistory.add(RoundScore(room.roundNumber, scoreCard.roundScore.map { score ->
            PlayerScore(
                playerId = score.playerId,
                playerName = score.playerName,
                score = score.score
            )
        }.toMutableList()))
        room.globalLeaderBoard = ScoreManager.getGlobalLeaderboard()
        room.dealerIndex = (room.dealerIndex + 1) % room.players.size
        broadcast(room.id)
    }

    fun getRoom(roomId: String): GameRoom? = rooms[roomId]

    suspend fun startNewRound(roomId: String) {
        val room = getRoom(roomId) ?: return
        room.startNewRound()
        room.globalLeaderBoard = ScoreManager.getGlobalLeaderboard()
        SessionRegistry.sendSoundToPlayer(roomId, room.currentPlayer().id, SoundCue(SoundEvent.PLAYER_TURN))
        broadcast(roomId)
    }

    private fun generateRoomId(length: Int = 6): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    suspend fun exitRoom(roomId: String, playerId: String) {
        val room = getRoom(roomId) ?: return
        val currentPlayerId = room.currentPlayer().id
        val players = room.players
        val exitingPlayer = players.find { it.id == playerId } ?: return
        val exitingPlayerIndex = players.indexOf(exitingPlayer)
        players.remove(exitingPlayer)

        if (room.currentIndex >= players.size) {
            // If currentIndex is now out of bounds, wrap around
            room.currentIndex = 0
        } else if (room.currentIndex > exitingPlayerIndex) {
            // If a player before the currentIndex left, shift currentIndex back
            room.currentIndex -= 1
        } else if (room.currentIndex == exitingPlayerIndex) {
            // If the current player exited, keep currentIndex same (it now points to next player)
            // Nothing else needed
        }

        // Reset dealer if needed
        if (room.dealerIndex >= players.size) room.dealerIndex = 0
        room.dealerIndex = 0
        broadcast(room.id)
    }
}
