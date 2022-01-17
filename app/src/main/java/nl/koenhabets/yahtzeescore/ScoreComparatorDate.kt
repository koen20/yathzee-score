package nl.koenhabets.yahtzeescore

import java.util.*

class ScoreComparatorDate : Comparator<ScoreItem> {
    override fun compare(scoreItem: ScoreItem, scoreItem1: ScoreItem): Int {
        return scoreItem1.date.compareTo(scoreItem.date)
    }
}