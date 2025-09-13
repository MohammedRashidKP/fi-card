package com.closemates.games.game


data class Player(val id: String, val name: String, val isAI: Boolean = false) {
    val hand: MutableList<Card> = mutableListOf()
    var openCard: Card? = null
    var active: Boolean = true

    fun handValue(jokerCard: Card): Int = hand.sumOf { it.value(jokerCard.rank) }


}
