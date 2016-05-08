package net.catsonmars.android.stillinmemphis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;

import net.catsonmars.android.stillinmemphis.data.TrackingContract;
import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncAdapter;
import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncService;
import net.catsonmars.android.stillinmemphis.ui.DividerItemDecoration;

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
            TrackingContract.PackagesEntry.TABLE_NAME + "." + TrackingContract.PackagesEntry._ID,
            // these two columns are for displaying the package description
            TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER,
            TrackingContract.PackagesEntry.COLUMN_DESCRIPTION,
            // these two columns are for selecting the right icon
            TrackingContract.PackagesEntry.COLUMN_DATE_DELIVERED,
            TrackingContract.PackagesEntry.COLUMN_ARCHIVED,
            // this column is for sorting by Newest First
            TrackingContract.EventsEntry.COLUMN_TIMESTAMP,
            // these three columns are for displaying the event details
            TrackingContract.EventsEntry.COLUMN_TYPE,
            TrackingContract.EventsEntry.COLUMN_TIME,
            TrackingContract.EventsEntry.COLUMN_DATE,
            TrackingContract.EventsEntry.COLUMN_EVENT
    };
    // These indices are tied to PACKAGES_COLUMNS.
    // If PACKAGES_COLUMNS changes, these must change.
    static final int COL_PACKAGE_ID = 0;
    static final int COL_PACKAGE_TRACKING_NUMBER = 1;
    static final int COL_PACKAGE_DESCRIPTION = 2;
    static final int COL_PACKAGE_DATE_DELIVERED = 3;
    static final int COL_PACKAGE_ARCHIVED = 4;
    static final int COL_EVENT_TIMESTAMP = 5;
    static final int COL_EVENT_TYPE = 6;
    static final int COL_EVENT_TIME = 7;
    static final int COL_EVENT_DATE = 8;
    static final int COL_EVENT_DESCRIPTION = 9;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LatestEventsAdapter mLatestEventsAdapter;
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

        mLatestEventsAdapter = new LatestEventsAdapter();

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
        if (getSupportLoaderManager().getLoader(PACKAGES_LOADER) == null) {
            getSupportLoaderManager().initLoader(PACKAGES_LOADER, savedInstanceState, this);
        } else {
            getSupportLoaderManager().restartLoader(PACKAGES_LOADER, savedInstanceState, this);
        }
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

        // TODO use selection and selection args to retrieve active or archived tracking numbers
        return new CursorLoader(this,
                // all events
                TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri(),
                PACKAGES_COLUMNS,
                null, // selection
                null, // selection args
                sortOrder);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");

        mLatestEventsAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");

        Log.d(TAG, "returned records: " + data.getCount());

        mLatestEventsAdapter.swapCursor(data);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(TAG, "setupRecyclerView");

        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setAdapter(mLatestEventsAdapter);
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

    public class LatestEventsAdapter
            extends RecyclerView.Adapter<LatestEventsAdapter.LatestEventsViewHolder> {

        private Cursor mCursor;

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
                    .inflate(R.layout.latestevents_list_content, parent, false);

            return new LatestEventsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LatestEventsViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            // set the package id
            holder.mPackageId = mCursor.getString(COL_PACKAGE_ID);

            // set the package description string
            String packageDescriptionString = mCursor.getString(COL_PACKAGE_DESCRIPTION);
            if((null == packageDescriptionString) || packageDescriptionString.isEmpty()) {
                packageDescriptionString = mCursor.getString(COL_PACKAGE_TRACKING_NUMBER);
            }
            holder.mIdView.setText(packageDescriptionString);

            // set the event description string
            String eventDescriptionString;
            String eventType = mCursor.getString(COL_EVENT_TYPE);
            String eventDescription = mCursor.getString(COL_EVENT_DESCRIPTION);
            if (eventType.equals(TrackingContract.EventsEntry.TYPE_EVENT)) {
                String eventDate = mCursor.getString(COL_EVENT_DATE);
                String eventTime = mCursor.getString(COL_EVENT_TIME);

                eventDescriptionString = String.format("%s, %s: %s", eventDate, eventTime, eventDescription);
            } else {
                eventDescriptionString = eventDescription;
            }
            holder.mContentView.setText(eventDescriptionString);

            // set the icon
            Drawable icon;
            int iconId;
            int dateDelivered = mCursor.getInt(COL_PACKAGE_DATE_DELIVERED);
            int packageArchived = mCursor.getInt(COL_PACKAGE_ARCHIVED);
            // first check for the ARCHIVED flag
            // all archived packages will have the same icon, regardless of the package status
            if (packageArchived != 0) {
                iconId = R.drawable.package_archived;
            } else if (eventType.equals(TrackingContract.EventsEntry.TYPE_ERROR)) {
                iconId = R.drawable.package_error;
            } else if (dateDelivered != 0) {
                iconId = R.drawable.package_delivered;
            } else {
                iconId = R.drawable.package_regular;
            }
            icon = ContextCompat.getDrawable(getApplicationContext(), iconId);
            holder.mIconView.setImageDrawable(icon);
            holder.mIconView.setContentDescription("package " + packageDescriptionString);

            // set the setOnClickListener
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(TrackingNumberDetailFragment.ARG_PACKAGE_ID, holder.mPackageId);
                        TrackingNumberDetailFragment fragment = new TrackingNumberDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.trackingnumber_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TrackingNumberDetailActivity.class);
                        intent.putExtra(TrackingNumberDetailFragment.ARG_PACKAGE_ID, holder.mPackageId);

                        context.startActivity(intent);
                    }
                }
            });
        }

        public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        public class LatestEventsViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mIconView;

            public String mPackageId;

            public LatestEventsViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mIdView = (TextView) itemView.findViewById(R.id.text1);
                mContentView = (TextView) itemView.findViewById(R.id.text2);
                mIconView = (ImageView) itemView.findViewById(R.id.icon);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
