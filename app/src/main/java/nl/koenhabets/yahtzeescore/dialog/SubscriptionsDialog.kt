package nl.koenhabets.yahtzeescore.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.adapters.SubscriptionAdapter
import nl.koenhabets.yahtzeescore.data.AppDatabase
import nl.koenhabets.yahtzeescore.databinding.SubscriptionsDialogBinding
import nl.koenhabets.yahtzeescore.model.Subscription

class SubscriptionsDialog(private var context: Context) {
    private var subscriptions: MutableList<Subscription> = ArrayList()
    private lateinit var binding: SubscriptionsDialogBinding
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun showDialog() {
        val builder = AlertDialog.Builder(context)

        binding = SubscriptionsDialogBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.recylerViewSubscriptions.layoutManager = layoutManager
        val subscriptionAdapter = SubscriptionAdapter(context, subscriptions)
        binding.recylerViewSubscriptions.adapter = subscriptionAdapter

        val subscriptionDao = AppDatabase.getDatabase(context).subscriptionDao()

        scope.launch {
            subscriptions.addAll(subscriptionDao.getAll())
            subscriptions.sortBy { it.lastSeen }
            subscriptions.reverse()
            subscriptionAdapter.notifyItemRangeInserted(0, subscriptions.size)
        }

        subscriptionAdapter.setOnLongClickListener(object : SubscriptionAdapter.ItemClickListener {
            override fun onItemLongClick(view: View?, position: Int) {
                val removeBuilder = AlertDialog.Builder(context)
                removeBuilder.setTitle(R.string.remove_player)
                removeBuilder.setPositiveButton(R.string.remove) { _: DialogInterface?, _: Int ->
                    scope.launch {
                        val item = subscriptionAdapter.getItem(position)
                        subscriptionDao.delete(item)
                        subscriptions.remove(item)
                    }
                    subscriptionAdapter.notifyItemRemoved(position)
                }
                removeBuilder.setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> }
                removeBuilder.show()
            }
        })

        builder.setView(view)
        builder.setTitle(context.getString(R.string.discovered_players))

        builder.setNegativeButton(context.getString(R.string.close)) { _: DialogInterface?, _: Int -> }
        builder.show()
    }
}