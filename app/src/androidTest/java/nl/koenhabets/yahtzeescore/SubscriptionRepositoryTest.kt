package nl.koenhabets.yahtzeescore

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import nl.koenhabets.yahtzeescore.data.SubscriptionRepository
import nl.koenhabets.yahtzeescore.model.Subscription
import org.junit.Test

class SubscriptionRepositoryTest {
    val subscriptionRepository = SubscriptionRepository(ApplicationProvider.getApplicationContext())

    @Test
    fun getAll() = runTest {
        val subscriptions = subscriptionRepository.getAll()
        assert(subscriptions.isEmpty())
    }

    @Test
    fun addSubscription() = runTest {
        subscriptionRepository.insert(Subscription("test", "test", 0))
        var subscriptions = subscriptionRepository.getAll()
        assert(subscriptions.size == 1)
        assert(subscriptions[0].userId == "test")

        subscriptionRepository.insert(Subscription("test2", "test", 0))
        subscriptions = subscriptionRepository.getAll()
        assert(subscriptions.size == 2)
        assert(subscriptions[1].userId == "test2")
        subscriptionRepository.deleteAll()
    }

    @Test
    fun getSubscription() = runTest {
        subscriptionRepository.insert(Subscription("test", "test", 0))
        subscriptionRepository.insert(Subscription("test2", "test", 0))
        assert(subscriptionRepository.getUserById("test")?.userId == "test")
        assert(subscriptionRepository.getUserById("test2")?.userId == "test2")
        subscriptionRepository.deleteAll()
    }

    @Test
    fun removeSubscription() = runTest {
        subscriptionRepository.insert(Subscription("test", "test", 0))
        subscriptionRepository.insert(Subscription("test2", "test", 0))
        assert(subscriptionRepository.getAll().size == 2)
        subscriptionRepository.delete(subscriptionRepository.getUserById("test")!!)
        assert(subscriptionRepository.getAll().size == 1)
        assert(subscriptionRepository.getUserById("test") == null)
        subscriptionRepository.deleteAll()
    }
}