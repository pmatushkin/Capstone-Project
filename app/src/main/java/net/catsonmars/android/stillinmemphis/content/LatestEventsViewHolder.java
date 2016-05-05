package net.catsonmars.android.stillinmemphis.content;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.catsonmars.android.stillinmemphis.R;

/**
 * Created by pmatushkin on 5/3/2016.
 */
public class LatestEventsViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public LatestEventsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mIdView = (TextView) itemView.findViewById(R.id.id);
        mContentView = (TextView) itemView.findViewById(R.id.content);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + mContentView.getText() + "'";
    }
}
