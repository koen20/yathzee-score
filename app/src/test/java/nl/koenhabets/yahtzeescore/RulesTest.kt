package nl.koenhabets.yahtzeescore

import nl.koenhabets.yahtzeescore.data.Game
import org.junit.Test
import org.junit.Assert.*

class RulesTest {
    @Test
    fun getRules() {
        assertNotNull(Rules.getRules(Game.Yahtzee))
        assertNotNull(Rules.getRules(Game.YahtzeeBonus))
        assertNotNull(Rules.getRules(Game.Yatzy))
    }
}