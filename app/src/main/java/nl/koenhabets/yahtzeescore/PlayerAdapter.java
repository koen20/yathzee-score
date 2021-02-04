package nl.koenhabets.yahtzeescore;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    private List<PlayerItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public PlayerAdapter(Context context, List<PlayerItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.player_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String player = mData.get(position).getName();
        String score = Integer.toString(mData.get(position).getScore());
        if (mData.get(position).isLocal()){
            player = "<b>" + player + "</b>";
            score = "<b>" + score + "</b>";
        }
        holder.textViewPlayer.setText(Html.fromHtml(player));
        holder.textViewScore.setText(Html.fromHtml(score));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewPlayer;
        TextView textViewScore;

        ViewHolder(View itemView) {
            super(itemView);
            textViewPlayer = itemView.findViewById(R.id.textViewPlayer);
            textViewScore = itemView.findViewById(R.id.textViewScore);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    PlayerItem getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
