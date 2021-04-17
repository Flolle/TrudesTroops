package trudesTroops.ai

import trudesTroops.game.Card
import java.util.*

object GameHelper {
    val cardsNeverOptimalInFirstPosition: EnumSet<Card> = EnumSet.of(
        Card.Wallman,
        Card.Cleric,
        Card.Medic,
        Card.Nurse,
        Card.Shieldmaiden,
        Card.MadScientist,
        Card.Page,
        Card.Martyr,
        Card.Sniper,
        Card.Horseman,
        Card.Siren
    )

    val reasonableFirstPositionCards: EnumSet<Card> = EnumSet.complementOf(cardsNeverOptimalInFirstPosition)
}