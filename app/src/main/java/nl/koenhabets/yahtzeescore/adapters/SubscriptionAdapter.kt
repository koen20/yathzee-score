package nl.koenhabets.yahtzeescore.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.model.Subscription
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class SubscriptionAdapter(context: Context?, data: List<Subscription>) :
    RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    private val mData: List<Subscription>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.subscription_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        item.name?.let { name ->
            holder.textViewName.text = name
            item.lastSeen?.let { lastSeen ->
                val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    android.icu.text.DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    )
                } else {
                    SimpleDateFormat("dd/MM/yy HH:mm")
                }
                holder.textViewLastSeen.text = dateFormat.format(Date(lastSeen))
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener {
        var textViewName: TextView = itemView.findViewById(R.id.textViewSubName)
        var textViewLastSeen: TextView = itemView.findViewById(R.id.textViewSubLastSeen)

        override fun onLongClick(view: View?): Boolean {
            mClickListener?.onItemLongClick(view, adapterPosition)
            return true
        }

        init {
            itemView.setOnLongClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): Subscription {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setOnLongClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemLongClick(view: View?, position: Int)
    }
}