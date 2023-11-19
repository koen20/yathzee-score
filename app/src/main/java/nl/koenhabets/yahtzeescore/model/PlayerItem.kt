package nl.koenhabets.yahtzeescore.model

import kotlinx.serialization.json.JsonObject

data class PlayerItem(
    val id: String,
    var name: String?,
    var score: Int?,
    var fullScore: JsonObject?,
    var lastUpdate: Long,
    val isLocal: Boolean,
    var game: String?
) : Comparable<PlayerItem> {
    override fun compareTo(other: PlayerItem): Int {
        other.score?.let {
            this.score?.let { scoreThis ->
                return it.compareTo(scoreThis)
            }
        }
        return 0
    }

    /*    val scoreCount: Int
            get() {
                var count = 0
                for (i in 0..13) {
                    var d = i
                    if (d > 6) {
                        d = i + 15
                    }
                    try {
                        if (fullScore.has(d.toString())) {
                            if (fullScore.getString(d.toString()) != "") {
                                count++
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                return count
            }*/
}