package nl.koenhabets.yahtzeescore.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import nl.koenhabets.yahtzeescore.R
import nl.koenhabets.yahtzeescore.model.PlayerItem

class PlayerAdapter(context: Context?, data: List<PlayerItem>) :
    RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {
    private val mData: List<PlayerItem>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.player_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        if (item.name != null) {
            var player = item.name
            var score = item.score.toString()
            if (mData[position].isLocal) {
                player = "<b>$player</b>"
                score = "<b>$score</b>"
            }
            if (player != null && score != "null") {
                holder.textViewPlayer.text =
                    HtmlCompat.fromHtml(player, HtmlCompat.FROM_HTML_MODE_LEGACY)
                holder.textViewScore.text =
                    HtmlCompat.fromHtml(score, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textViewPlayer: TextView = itemView.findViewById(R.id.textViewPlayer)
        var textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): PlayerItem {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
    }
}