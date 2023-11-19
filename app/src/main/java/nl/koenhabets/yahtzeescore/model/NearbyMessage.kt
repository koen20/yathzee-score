package nl.koenhabets.yahtzeescore.model

data class NearbyMessage(
    //userId
    val id: String,
    //username
    var u: String? = null,
    //score
    var s: Int? = null,
    //timestamp
    val t: Long
)
