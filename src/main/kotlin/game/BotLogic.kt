package com.closemates.games.game

object BotLogic {

    fun choosePick(hand: List<Card>, openCard: Card?, joker: Rank): PickAction {
        openCard ?: return PickAction.DECK // nothing to pick from discard

        val useful = hand.any { it.rank == openCard.rank || it.rank == joker || it.rank!!.value < 5 }
        return if (useful) PickAction.DISCARD else PickAction.DECK
    }

    fun chooseDiscard(hand: List<Card>, joker: Rank): List<Card> {
        // Group cards by rank
        val groups = hand.groupBy { it.rank }

        // Exclude jokers (valuable cards)
        val safeGroups = groups.filterKeys { it != joker }

        // Function to score a group (sum of values)
        fun score(group: List<Card>): Int =
            group.sumOf { it.rank!!.value } // define rank.value (e.g., A=1, J=11, Q=12, K=13)

        // Pick group with the highest total value
        val bestGroup = safeGroups.values.maxByOrNull { score(it) }

        // Fallback in case everything is joker
        return bestGroup ?: hand.groupBy { it.rank }.values.first()
    }

}

enum class PickAction { DECK, DISCARD }
