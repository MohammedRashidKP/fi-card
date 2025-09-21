package com.closemates.games.game

import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
enum class Rank(val value: Int) {
    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
    EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10)
}

enum class JokerColor { RED, BLACK }


@Serializable
data class Card(val suit: Suit?, val rank: Rank?, val jokerColor: JokerColor? = null) {

    val isJoker: Boolean = jokerColor != null

    companion object {

        /** Creates a standard 52-card deck in rank 1..13 for each suit. */
        private fun fullDeck(): List<Card> {
            val deck = mutableListOf<Card>()
            for (suit in Suit.entries) {
                for (rank in Rank.entries) {
                    deck.add(Card(suit, rank))
                }
            }
            deck.add(Card(null, null, JokerColor.RED))
            deck.add(Card(null, null, JokerColor.BLACK))
            return deck
        }

        /** Convenience: get a new shuffled deck (mutable) */
        fun shuffledDeck(random: Random = Random(System.currentTimeMillis())): MutableList<Card> =
            fullDeck().shuffled(random)
                .shuffled()
                .toMutableList()
    }
}

fun Card.value(jokerRank: Rank): Int {
    return if (this.isJoker) 0             // physical joker
    else if (this.rank == jokerRank) 0 // round joker
    else this.rank!!.value
}
