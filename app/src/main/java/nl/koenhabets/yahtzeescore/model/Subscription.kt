package nl.koenhabets.yahtzeescore.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Subscription(
    @PrimaryKey val userId: String,
    var name: String?,
    var lastSeen: Long?
)

