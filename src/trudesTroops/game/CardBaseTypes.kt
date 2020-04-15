package trudesTroops.game

import kotlin.math.min

interface CardEntity {
    val maxHP: Int

    val baseAttack: Int

    val name: String

    fun newInstance(): CardInstance
}

abstract class CardInstance(
    var currentHP: Int
) : CardEntity {
    fun heal(healAmount: Int) {
        currentHP = min(maxHP, currentHP + healAmount)
    }

    fun copy(): CardInstance {
        val newCopy = newInstance()
        newCopy.currentHP = currentHP

        return newCopy
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardInstance) return false

        if (currentHP != other.currentHP) return false
        if (maxHP != other.maxHP) return false
        if (baseAttack != other.baseAttack) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentHP
        result = 31 * result + maxHP
        result = 31 * result + baseAttack
        result = 31 * result + name.hashCode()
        return result
    }
}

interface AttackSpecial

interface HealingSpecial