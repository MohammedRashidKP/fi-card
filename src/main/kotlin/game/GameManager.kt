package com.closemates.games.game

import com.closemates.games.models.GameState
import game.SessionRegistry
import kotlin.random.Random

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
        if (room.players.map { it.id }.contains(player.id)){
            return
        }
        if (room.players.size >= 10 || room.gameState != GameState.LOBBY) return
        room.players.add(player)
        broadcast(roomId)
    }

    fun addAI(roomId: String) {
        val room = rooms[roomId] ?: return
        if (room.players.size >= 10 || room.gameState != GameState.LOBBY) return
        val ai = Player("AI-${Random.nextInt(1000)}", "AI Bot", isAI = true)
        room.players.add(ai)
    }

    suspend fun pickOpenCard(roomId: String, playerId: String) {
        val room = rooms[roomId] ?: return
        if (room.currentPlayer().id != playerId) return

        val player = room.players.find { it.id == playerId } ?: return
        val open = player.openCard ?: return

        player.hand.add(open)
        player.openCard = null
        room.currentIndex = (room.currentIndex + 1) % room.players.size
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
        room.currentIndex = (room.currentIndex + 1) % room.players.size
        broadcast(roomId)
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
        val callValue = room.playerHandValue(callerId)
        if (callValue!! > 5) return
        room.callerId = callerId
        room.callValue = callValue
        room.gameState = GameState.CALLED
        broadcast(roomId)
        room.startStrikePhase {
            finishRound(room)
        }
    }

    suspend fun strike(roomId: String, strikerId: String) {
        val room = rooms[roomId] ?: return
        if (room.strikerId != null) return
        if (room.gameState != GameState.CALLED) return

        room.cancelStrikePhase()

        room.strikerId = strikerId
        finishRound(room)
    }

    private suspend fun finishRound(room: GameRoom) {
        room.gameState = GameState.FINISHED
        ScoreManager.saveRoundScore(room.id, room.roundNumber, room.getScoreCard())
        room.roundHistory = ScoreManager.getRoundHistoryPerRound(room.id)
        room.globalLeaderBoard = ScoreManager.getGlobalLeaderboard()
        room.dealerIndex = (room.dealerIndex + 1) % room.players.size
        broadcast(room.id)
    }

    fun getRoom(roomId: String): GameRoom? = rooms[roomId]

    suspend fun startNewRound(roomId: String) {
        val room = getRoom(roomId) ?: return
        room.startNewRound()
        room.globalLeaderBoard = ScoreManager.getGlobalLeaderboard()
        room.roundHistory = ScoreManager.getRoundHistoryPerRound(roomId)
        broadcast(roomId)
    }

    private fun generateRoomId(length: Int = 6): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
