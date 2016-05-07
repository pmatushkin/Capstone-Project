package net.catsonmars.android.stillinmemphis.content;

import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.catsonmars.android.stillinmemphis.R;

/**
 * Created by pmatushkin on 5/3/2016.
 */
public class LatestEventsAdapter extends RecyclerView.Adapter<LatestEventsViewHolder> {

    private Cursor mCursor;

    // These indices are tied to TrackingNumberListActivity.PACKAGES_COLUMNS.
    // If PACKAGES_COLUMNS changes, these must change.
    static final int COL_PACKAGE_ID = 0;
    static final int COL_PACKAGE_TRACKING_NUMBER = 1;
    static final int COL_PACKAGE_DESCRIPTION = 2;
    static final int COL_EVENT_TIMESTAMP = 3;
    static final int COL_EVENT_TIME = 4;
    static final int COL_EVENT_DATE = 5;
    static final int COL_EVENT_DESCRIPTION = 6;

    @Override
    public int getItemCount() {
        return null == mCursor ?
                0
                :
                mCursor.getCount();
    }

    @Override
    public LatestEventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trackingnumber_list_content, parent, false);

        return new LatestEventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LatestEventsViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.mIdView.setText(Integer.toString(mCursor.getInt(COL_PACKAGE_ID)));
        holder.mContentView.setText(mCursor.getString(COL_PACKAGE_TRACKING_NUMBER));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

//                if (mTwoPane) {
//                    Bundle arguments = new Bundle();
//                    arguments.putString(TrackingNumberDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//                    TrackingNumberDetailFragment fragment = new TrackingNumberDetailFragment();
//                    fragment.setArguments(arguments);
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.trackingnumber_detail_container, fragment)
//                            .commit();
//                } else {
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, TrackingNumberDetailActivity.class);
//                    intent.putExtra(TrackingNumberDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//
//                    context.startActivity(intent);
//                }
            }
        });
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
