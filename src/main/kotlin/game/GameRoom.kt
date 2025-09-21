package com.closemates.games.game

import com.closemates.games.models.*
import kotlinx.coroutines.*

data class GameRoom(
    val id: String,
    var roundNumber: Int,
    val players: MutableList<Player> = mutableListOf(),
    val deck: MutableList<Card> = mutableListOf(),
    var jokerCard: Card? = null,
    var currentIndex: Int = -1,
    var gameState: GameState = GameState.LOBBY,
    var openCard: Card? = null,
    var dealerIndex: Int = 0,
    var callerId: String? = null,
    var callValue: Int? = null,
    var strikerId: String? = null,
    var strikeValue: Int? = null,
    var roundHistory: MutableList<RoundScore> = mutableListOf(),
    var globalLeaderBoard: List<PlayerPoints> = mutableListOf()
) {
    fun currentPlayer(): Player = players[currentIndex]

    fun playerHandValue(id: String): Int? = players.find { id == it.id }?.handValue(jokerCard!!)

    fun dealer(): Player = players[dealerIndex]

    fun getScoreCard(): ScoreCard {

        val playerScores: MutableMap<String, Int> = players.associate { player ->
            player.id to player.handValue(jokerCard!!)
        }.toMutableMap()

        var winnerId: String = callerId!!

        val callerScore = playerHandValue(callerId!!)
        val maxScore = this.players.maxOf { it.handValue(this.jokerCard!!) }
        if (strikerId != null) {
            val strikerScore = playerHandValue(strikerId!!)
            if (strikerScore!! <= callerScore!!) {
                playerScores[callerId!!] = maxScore
                playerScores[strikerId!!] = 0
                winnerId = strikerId!!
            }
        } else {
            playerScores[callerId!!] = 0
        }
        val playerScoreList: List<PlayerScore> =
            players.map { player ->
                PlayerScore(
                    playerId = player.id,
                    playerName = player.name,
                    score = playerScores[player.id] ?: 0
                )
            }
        return ScoreCard(winnerId, playerScoreList.sortedBy { it.score })
    }

    // Each room has its own scope, tied to SupervisorJob
    private val roomScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var strikeJob: Job? = null

    fun startStrikePhase(onTimeout: suspend () -> Unit) {
        // Cancel any existing strike job (in case of re-entry)
        strikeJob?.cancel()

        strikeJob = roomScope.launch {
            delay(7000) // 10 seconds strike window
            onTimeout() // e.g. finish game, broadcast results
        }
    }

    fun cancelStrikePhase() {
        strikeJob?.cancel()
        strikeJob = null
    }

    fun cleanup() {
        roomScope.cancel() // cancel everything when room is destroyed
    }

    fun startNewRound() {

        if (gameState != GameState.FINISHED) return
        // Increment round and dealer
        roundNumber += 1

        // Clear hands & open cards
        players.forEach { p ->
            p.hand.clear()
            p.openCard = null
        }

        // Fresh shuffled deck
        deck.clear()
        deck.addAll(Card.shuffledDeck())
        deck.addAll(Card.shuffledDeck())

        // Joker is first revealed card
       assignJoker()

        // Deal 5 cards to each player
        for (p in players) {
            repeat(5) {
                p.hand.add(deck.removeFirst())
            }
        }

        // Place an open card for first player (after dealer)
        val firstPlayerIndex = (dealerIndex + 1) % players.size
        val firstPlayer = players[firstPlayerIndex]

        var firstOpen: Card
        do {
            firstOpen = deck.removeAt(0)
        } while (firstOpen.rank == jokerCard?.rank)

        firstPlayer.openCard = firstOpen

        // Reset turn
        currentIndex = firstPlayerIndex
        gameState = GameState.IN_PROGRESS
        callValue = null
        strikerId = null
        callerId = null
        strikeValue = null
    }
}


fun GameRoom.startNewGame() {
    deck.clear()
    players.forEach { p ->
        p.hand.clear()
        p.openCard = null
    }

    // Fresh shuffled deck
    deck.addAll(Card.shuffledDeck())
    if (players.size > 4) {
        deck.addAll(Card.shuffledDeck())
    }

    // Dealer deals 5 cards to each player
    for (p in players) {
        repeat(5) {
            p.hand.add(deck.removeFirst())
        }
    }

    // Joker is first revealed card (suit ignored)

    assignJoker()

    // Place an open card specifically for the first player (after dealer)
    val firstPlayerIndex = (dealerIndex + 1) % players.size
    val firstPlayer = players[firstPlayerIndex]

    var firstOpen: Card
    do {
        firstOpen = deck.removeAt(0)
    } while (firstOpen.rank == jokerCard?.rank)

    firstPlayer.openCard = firstOpen

    // Set turn to that first player
    currentIndex = firstPlayerIndex
    gameState = GameState.IN_PROGRESS
    callValue = null
    strikerId = null
    callerId = null
    strikeValue = null
}

private fun GameRoom.assignJoker() {
    val nonJokerIndices = deck.withIndex()
        .filter { !it.value.isJoker }  // assuming you have isJoker flag
        .map { it.index }

    if (nonJokerIndices.isNotEmpty()) {
        val randomIndex = nonJokerIndices.random()   // pick a random index
        jokerCard = deck[randomIndex]           // select the card
        deck.removeAt(randomIndex)                   // remove by index, safe with duplicates
    }
}