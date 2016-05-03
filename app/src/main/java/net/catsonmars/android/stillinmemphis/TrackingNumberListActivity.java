package net.catsonmars.android.stillinmemphis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.catsonmars.android.stillinmemphis.data.TrackingContract;
import net.catsonmars.android.stillinmemphis.dummy.DummyContent;
import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncAdapter;
import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncService;

import java.util.List;

/**
 * An activity representing a list of Tracking Numbers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TrackingNumberDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TrackingNumberListActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "NumberListActivity";

    private boolean mTwoPane;

    private boolean mIsRefreshing = false;

    private static final int PACKAGES_LOADER = 0;
    private static final String[] PACKAGES_COLUMNS = {
            // these two columns are for displaying the package description
            TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER,
            TrackingContract.PackagesEntry.COLUMN_DESCRIPTION,
            // this column is for sorting by Newest First
            TrackingContract.EventsEntry.COLUMN_TIMESTAMP,
            // these three columns are for displaying the event details
            TrackingContract.EventsEntry.COLUMN_TIME,
            TrackingContract.EventsEntry.COLUMN_DATE,
            TrackingContract.EventsEntry.COLUMN_EVENT
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_PACKAGE_TRACKING_NUMBER = 0;
    static final int COL_PACKAGE_DESCRIPTION = 1;
    static final int COL_EVENT_TIMESTAMP = 2;
    static final int COL_EVENT_TIME = 3;
    static final int COL_EVENT_DATE = 4;
    static final int COL_EVENT_DESCRIPTION = 5;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_trackingnumber_list);

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }

        // set up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "FloatingActionButton.onClick");

                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        // set up SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        // set up RecyclerView
        mRecyclerView = findViewById(R.id.trackingnumber_list);
        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);

        // set up two pane mode
        if (findViewById(R.id.trackingnumber_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        // initialize sync adapter
        StillInMemphisSyncService.initializeSyncAdapter(this);

        // Prepare the loader
        getSupportLoaderManager().initLoader(PACKAGES_LOADER, null, this);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        IntentFilter intentFilter = new IntentFilter(StillInMemphisSyncAdapter.BROADCAST_ACTION_DATA_CHANGE);
        intentFilter.addAction(StillInMemphisSyncAdapter.BROADCAST_ACTION_STATE_CHANGE);

        registerReceiver(mRefreshingReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");

        unregisterReceiver(mRefreshingReceiver);

        super.onStop();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        // TODO maybe consider the sorting preferences set by user
        String sortOrder = TrackingContract.EventsEntry.COLUMN_TIMESTAMP + " DESC";

        return new CursorLoader(this,
                // all events
                TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri(),
                PACKAGES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
//        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");

        Log.d(TAG, "returned records: " + data.getCount());
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(TAG, "setupRecyclerView");

        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    private void refresh() {
        Log.d(TAG, "refresh");

        StillInMemphisSyncService.syncImmediately(this);
    }

    private void updateRefreshingUI() {
        Log.d(TAG, "updateRefreshingUI");

        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mRefreshingReceiver.onReceive");

            if (StillInMemphisSyncAdapter.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(StillInMemphisSyncAdapter.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trackingnumber_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TrackingNumberDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        TrackingNumberDetailFragment fragment = new TrackingNumberDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.trackingnumber_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TrackingNumberDetailActivity.class);
                        intent.putExtra(TrackingNumberDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
