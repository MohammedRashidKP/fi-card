package com.closemates.games.game


data class Player(val id: String, val name: String) {
    val hand: MutableList<Card> = mutableListOf()
    var openCard: Card? = null
    var isBot: Boolean = false

    fun handValue(jokerCard: Card): Int = hand.sumOf { it.value(jokerCard.rank!!) }
}
