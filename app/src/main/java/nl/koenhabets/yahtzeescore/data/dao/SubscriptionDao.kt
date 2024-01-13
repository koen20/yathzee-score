package nl.koenhabets.yahtzeescore.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.koenhabets.yahtzeescore.model.Subscription

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription")
    suspend fun getAll(): List<Subscription>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg subscriptions: Subscription)

    @Query("SELECT * FROM subscription WHERE userId = :userId")
    suspend fun getUserById(userId: String): Subscription?

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("DELETE FROM subscription where userId IS NULL")
    suspend fun deleteUserIdNull()
}