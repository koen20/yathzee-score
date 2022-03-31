package nl.koenhabets.yahtzeescore

import nl.koenhabets.yahtzeescore.data.Game
import org.json.JSONObject

class ScoreItem(val score: Int, val date: Long, val id: String, val game: Game, val allScores: JSONObject)