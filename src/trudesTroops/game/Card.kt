package trudesTroops.game

class Berserker(
    override val maxHP: Int = 6,
    override val name: String = "Berserker"
) : CardInstance(maxHP) {
    override val baseAttack: Int
        get() = 1 + maxHP - currentHP

    override fun newInstance(): CardInstance = Berserker()
}

class Gladiator(
    override val maxHP: Int = 6,
    override val baseAttack: Int = 1,
    override val name: String = "Gladiator"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Gladiator()
}

class Guard(
    override val maxHP: Int = 6,
    override val baseAttack: Int = 2,
    override val name: String = "Guard"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Guard()
}

class Halberder(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 4,
    override val name: String = "Halberder"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Halberder()
}

class Ninja(
    override val maxHP: Int = 1,
    override val baseAttack: Int = 8,
    override val name: String = "Ninja"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Ninja()
}

class Shieldbot(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 2,
    override val name: String = "Shieldbot",
    var hasShield: Boolean = true
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Shieldbot()

    override fun equals(other: Any?): Boolean =
        super.equals(other) && other is Shieldbot && hasShield == other.hasShield

    override fun hashCode(): Int = 31 * super.hashCode() + hasShield.hashCode()
}

class Spearsman(
    override val maxHP: Int = 5,
    override val baseAttack: Int = 3,
    override val name: String = "Spearsman"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Spearsman()
}

class Vampire(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 3,
    override val name: String = "Vampire"
) : CardInstance(maxHP), HealingSpecial {
    override fun newInstance(): CardInstance = Vampire()
}

class Wallman(
    override val maxHP: Int = 8,
    override val baseAttack: Int = 1,
    override val name: String = "Wallman"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Wallman()
}

class Cannonball(
    override val maxHP: Int = 1,
    override val baseAttack: Int = 0,
    override val name: String = "Cannonball"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Cannonball()
}

class Horseman(
    override val maxHP: Int = 5,
    override val baseAttack: Int = 2,
    override val name: String = "Horseman"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Horseman()
}

class Siren(
    override val maxHP: Int = 3,
    override val baseAttack: Int = 2,
    override val name: String = "Siren"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Siren()
}

class Sniper(
    override val maxHP: Int = 1,
    override val baseAttack: Int = 1,
    override val name: String = "Sniper"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Sniper()
}

class Bowman(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 1,
    override val name: String = "Bowman"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Bowman()
}

class Dervish(
    override val maxHP: Int = 5,
    override val baseAttack: Int = 2,
    override val name: String = "Dervish"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Dervish()
}

class Alchemist(
    override val maxHP: Int = 5,
    override val baseAttack: Int = 0,
    override val name: String = "Alchemist"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Alchemist()
}

class Hammerman(
    override val maxHP: Int = 5,
    override val baseAttack: Int = 0,
    override val name: String = "Hammerman"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Hammerman()
}

class Lanceman(
    override val maxHP: Int = 3,
    override val baseAttack: Int = 2,
    override val name: String = "Lanceman"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Lanceman()
}

class MadBomber(
    override val maxHP: Int = 1,
    override val baseAttack: Int = 2,
    override val name: String = "Mad Bomber"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = MadBomber()
}

class Mage(
    override val maxHP: Int = 1,
    override val baseAttack: Int = 0,
    override val name: String = "Mage"
) : CardInstance(maxHP), AttackSpecial {
    override fun newInstance(): CardInstance = Mage()
}

class Nurse(
    override val maxHP: Int = 3,
    override val baseAttack: Int = 1,
    override val name: String = "Nurse"
) : CardInstance(maxHP), HealingSpecial {
    override fun newInstance(): CardInstance = Nurse()
}

class Medic(
    override val maxHP: Int = 2,
    override val baseAttack: Int = 1,
    override val name: String = "Medic"
) : CardInstance(maxHP), HealingSpecial {
    override fun newInstance(): CardInstance = Medic()
}

class Cleric(
    override val maxHP: Int = 2,
    override val baseAttack: Int = 1,
    override val name: String = "Cleric"
) : CardInstance(maxHP), HealingSpecial {
    override fun newInstance(): CardInstance = Cleric()
}

class MadScientist(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 1,
    override val name: String = "Mad Scientist"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = MadScientist()
}

class Page(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 1,
    override val name: String = "Page"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Page()
}

class Shieldmaiden(
    override val maxHP: Int = 4,
    override val baseAttack: Int = 1,
    override val name: String = "Shieldmaiden"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Shieldmaiden()
}

class Martyr(
    override val maxHP: Int = 3,
    override val baseAttack: Int = 1,
    override val name: String = "Martyr"
) : CardInstance(maxHP) {
    override fun newInstance(): CardInstance = Martyr()
}

enum class Card(private val card: CardEntity) {
    Berserker(Berserker()),
    Gladiator(Gladiator()),
    Guard(Guard()),
    Halberder(Halberder()),
    Ninja(Ninja()),
    Shieldbot(Shieldbot()),
    Spearsman(Spearsman()),
    Vampire(Vampire()),
    Wallman(Wallman()),
    Cannonball(Cannonball()),
    Horseman(Horseman()),
    Siren(Siren()),
    Sniper(Sniper()),
    Bowman(Bowman()),
    Dervish(Dervish()),
    Alchemist(Alchemist()),
    Hammerman(Hammerman()),
    Lanceman(Lanceman()),
    MadBomber(MadBomber()),
    Mage(Mage()),
    Nurse(Nurse()),
    Medic(Medic()),
    Cleric(Cleric()),
    MadScientist(MadScientist()),
    Page(Page()),
    Shieldmaiden(Shieldmaiden()),
    Martyr(Martyr());

    fun getCardInstance(): CardInstance = card.newInstance()

    companion object {
        val values: List<Card> = values().asList()
    }
}