package com.closemates.games.game

import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
enum class Rank(val value: Int) {
    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
    EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10)
}

@Serializable
data class Card(val suit: Suit, val rank: Rank) {
    companion object {

        /** Creates a standard 52-card deck in rank 1..13 for each suit. */
        private fun fullDeck(): List<Card> {
            val deck = mutableListOf<Card>();
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    deck.add(Card(suit, rank))
                }
            }
            return deck
        }

        /** Convenience: get a new shuffled deck (mutable) */
        fun shuffledDeck(random: Random = Random(System.currentTimeMillis())): MutableList<Card> =
            fullDeck().shuffled(random).toMutableList()
    }
}

fun Card.value(jokerRank: Rank): Int {
    return if (this.rank == jokerRank) 0 else this.rank.value
}
