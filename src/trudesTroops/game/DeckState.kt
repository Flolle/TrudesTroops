package trudesTroops.game

/**
 * Representation of a deck of cards. It is implemented as a sort of doubly linked list.
 */
class Deck(deck: List<Card>) {
    /**
     * The card in rank 1.
     */
    var firstRank: CardElement? = CardElement(deck.first().getCardInstance())

    /**
     * The card in the last rank.
     */
    var lastRank: CardElement?

    init {
        require(deck.size > 1)

        var index = firstRank!!
        for (i in 1 until deck.size) {
            val currentElement = CardElement(deck[i].getCardInstance(), index)
            index.nextRank = currentElement
            index = currentElement
        }
        lastRank = index
    }

    /**
     * The amount of ranks present in this deck.
     */
    var ranks: Int = deck.size

    val isEmpty: Boolean
        get() = firstRank == null

    /**
     * Returns the card in the given rank if one is present.
     *
     * Please note that ranks are 1-based, in contrast to 0-based arrays or lists.
     */
    fun getCardAtRank(rank: Int): CardElement? {
        if (rank == 1)
            return firstRank
        if (rank == ranks)
            return lastRank

        var i = 1
        var index = firstRank?.nextRank
        while (index != null) {
            if (++i == rank)
                return index

            index = index.nextRank
        }

        return null
    }

    /**
     * Returns the rank of the card of the given type or -1 if no card of that type is present in the deck.
     *
     * Please note that ranks are 1-based, in contrast to 0-based arrays or lists.
     */
    inline fun <reified T : CardInstance> findRankOfType(): Int {
        var i = 1
        var index = firstRank
        while (index != null) {
            if (index.card is T)
                return i

            i++
            index = index.nextRank
        }

        return -1
    }

    /**
     * Removes the card at the given rank from the deck.
     *
     * Please note that ranks are 1-based, in contrast to 0-based arrays or lists.
     */
    fun removeCardAtRank(rank: Int) {
        require(rank in 1..ranks) { "rank must be in the range of 1..ranks!" }
        if (rank == 1) {
            firstRank = firstRank?.nextRank
            firstRank?.previousRank = null
            ranks--
            return
        }
        if (rank == ranks) {
            lastRank = lastRank?.previousRank
            lastRank?.nextRank = null
            ranks--
            return
        }

        var i = 1
        var index = firstRank?.nextRank
        while (index != null) {
            if (++i == rank) {
                index.previousRank?.nextRank = index.nextRank
                index.nextRank?.previousRank = index.previousRank
                ranks--
                return
            }

            index = index.nextRank
        }
    }

    /**
     * Removes all cards with HP values of under 1.
     */
    fun removeKilledCards() {
        var index = firstRank
        while (index != null) {
            val previous = index.previousRank
            val next = index.nextRank
            if (index.card.currentHP < 1) {
                previous?.nextRank = next
                next?.previousRank = previous
                ranks--

                if (previous == null)
                    firstRank = next
                if (next == null)
                    lastRank = previous
            }
            index = next
        }
    }

    /**
     * Returns true if the given predicate evaluates to true for at least one card.
     */
    inline fun any(predicate: (card: CardInstance) -> Boolean): Boolean {
        forEachCardInstance {
            if (predicate(it))
                return true
        }

        return false
    }

    /**
     * Applies the given action to every card in the deck.
     */
    inline fun forEachCardElement(action: (card: CardElement) -> Unit) {
        var index = firstRank
        while (index != null) {
            action(index)
            index = index.nextRank
        }
    }

    /**
     * Applies the given action to every card in the deck.
     */
    inline fun forEachCardInstance(action: (card: CardInstance) -> Unit) {
        var index = firstRank
        while (index != null) {
            action(index.card)
            index = index.nextRank
        }
    }

    /**
     * Returns an array representation of this deck with all cards in the correct order.
     *
     * Please note that all [CardInstance]s are deep copies of the CardInstances within the deck.
     */
    fun toArray(): Array<CardInstance> {
        if (isEmpty)
            return emptyArray()

        val result = arrayOfNulls<CardInstance>(ranks)
        var i = ranks
        var index = lastRank
        while (--i >= 0) {
            result[i] = index!!.card.copy()
            index = index.previousRank
        }

        @Suppress("UNCHECKED_CAST")
        return result as Array<CardInstance>
    }

    /**
     * Returns a list representation of this deck with all cards in the correct order.
     *
     * Please note that all [CardInstance]s are deep copies of the CardInstances within the deck.
     */
    fun toList(): List<CardInstance> {
        if (isEmpty)
            return emptyList()

        val result = ArrayList<CardInstance>(ranks)
        forEachCardInstance {
            result += it.copy()
        }

        return result
    }
}

/**
 * This class basically represents a list node which is aware of its predecessor and successor.
 */
data class CardElement(
    val card: CardInstance,
    var previousRank: CardElement? = null,
    var nextRank: CardElement? = null
) {
    /**
     * Returns either this [CardElement] or [nextRank] in case nextRank contains a Martyr.
     */
    fun thisOrMartyr(): CardElement {
        val nextCard = nextRank
        return if (nextCard != null && nextCard.card is Martyr) nextCard else this
    }
}