package nl.koenhabets.yahtzeescore

import java.util.*

class ScoreComparator : Comparator<ScoreItem> {
    override fun compare(scoreItem: ScoreItem, scoreItem1: ScoreItem): Int {
        return scoreItem1.score - scoreItem.score
    }
}