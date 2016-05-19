package net.catsonmars.android.stillinmemphis;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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

    private static final String ARG_LIST_MODE = "ARG_LIST_MODE";
    private static final int MODE_ACTIVE = 0;
    private static final int MODE_ARCHIVE = 1;
    private int mListMode;

    private boolean mTwoPane;
    private View mTrackingDetailTwoPane;
    private int mSelectedPosition;

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
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

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

        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            Log.d(TAG, "found ActionBar");

            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
        }

        // set up drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // set up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "FloatingActionButton.onClick");

                    refresh();
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

        // set up adapter
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

            // Set up the tracking detail view.
            // In two-pane mode it contains the tracking detail container (a RecyclerView)
            // and a map fragment.
            mTrackingDetailTwoPane = findViewById(R.id.trackingnumber_detail);

            // Hide it when the app starts.
            if (mTrackingDetailTwoPane != null) {
                mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
            }
        }

        // I don't intend to save the state of the selected position,
        // because the configuration change on the tablet switches
        // between one- and two-pane layouts, and it looks confusing
        // to turn a tablet, and see a one-pane layout with an item selected.
        mSelectedPosition = -1;

        // initialize the AdMob view
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            if (mTwoPane) {
                // hide it on the table layout
                // it's for demo purposes only, and I didn't find a good place for the ad view
                mAdView.setVisibility(View.GONE);
            } else {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        }

        // initialize sync adapter
        StillInMemphisSyncService.initializeSyncAdapter(this);

        // set up the list mode
        if (savedInstanceState == null) {
            mListMode = MODE_ACTIVE;
        } else {
            if (savedInstanceState.containsKey(ARG_LIST_MODE)) {
                mListMode = savedInstanceState.getInt(ARG_LIST_MODE);
            }
        }

        // set up loader
        setupLoader(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.main, menu);

        // set up the action view listener
        final Context context = this;
        final MenuItem menuItemAddPackage = menu.findItem(R.id.action_add_package);
        if (MODE_ARCHIVE == mListMode) {
            menuItemAddPackage.setVisible(false);
        } else {
            menuItemAddPackage.setVisible(true);

            final View actionView = MenuItemCompat.getActionView(menuItemAddPackage);
            TextView textAddPackage = (TextView) actionView.findViewById(R.id.textview_add_package);
            if (textAddPackage != null) {
                textAddPackage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            String trackingNumber = String.valueOf(v.getText()).trim();
                            v.setText("");

                            actionView.clearFocus();
                            menuItemAddPackage.collapseActionView();
                            if (!"".equals(trackingNumber)) {
                                refresh(trackingNumber);
                            }

                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_add_package:
                return true;

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_LIST_MODE, mListMode);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        // TODO consider the sorting preferences set by user
        String sortOrder = TrackingContract.EventsEntry.COLUMN_TIMESTAMP + " DESC";

        if (MODE_ACTIVE == mListMode) {
            Log.d(TAG, "MODE_ACTIVE");
            return new CursorLoader(this,
                    TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri(),
                    PACKAGES_COLUMNS,
                    TrackingContract.PackagesEntry.COLUMN_ARCHIVED + " = 0", // selection
                    null, // selection args
                    sortOrder);
        } else {
            Log.d(TAG, "MODE_ARCHIVE");
            return new CursorLoader(this,
                    TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri(),
                    PACKAGES_COLUMNS,
                    TrackingContract.PackagesEntry.COLUMN_ARCHIVED + " != 0", // selection
                    null, // selection args
                    sortOrder);
        }
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

    private void setupLoader(Bundle savedInstanceState) {
        if (getSupportLoaderManager().getLoader(PACKAGES_LOADER) == null) {
            getSupportLoaderManager().initLoader(PACKAGES_LOADER, savedInstanceState, this);
        } else {
            getSupportLoaderManager().restartLoader(PACKAGES_LOADER, savedInstanceState, this);
        }
    }

    private void setupDrawerContent(NavigationView nvDrawer) {
        nvDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_active:
                mListMode = MODE_ACTIVE;
                if (mTrackingDetailTwoPane != null) {
                    mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
                }
                mSelectedPosition = -1;

                break;

            case R.id.nav_archive:
                mListMode = MODE_ARCHIVE;
                if (mTrackingDetailTwoPane != null) {
                    mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
                }
                mSelectedPosition = -1;

                break;

            // TODO add Settings activity
//            case R.id.nav_settings:
//                break;
        }

        // hide the Add Package option in the Archive mode
        invalidateOptionsMenu();

        // set up the new cursor
        setupLoader(null);

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);

        // Set action bar title
        //setTitle(menuItem.getTitle());

        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
    }

    private void refresh() {
        Log.d(TAG, "refresh");

        StillInMemphisSyncService.syncImmediately(this);
    }

    private void refresh(String trackingNumber) {
        Log.d(TAG, "refresh(trackingNumber)");

        StillInMemphisSyncService.syncImmediatelyWithTrackingNumber(this, trackingNumber);
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
            holder.itemView.setSelected(mSelectedPosition == position);

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

                String eventDateTimeString = FormatUtils.formatUSPSDateTime(eventDate, eventTime);

                eventDescriptionString = String.format("%s: %s", eventDateTimeString, eventDescription);
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

            // set the item setOnClickListener
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        notifyItemChanged(mSelectedPosition);
                        mSelectedPosition = holder.getLayoutPosition();
                        notifyItemChanged(mSelectedPosition);

                        Bundle arguments = new Bundle();
                        arguments.putString(
                                TrackingNumberDetailFragment.ARG_PACKAGE_ID,
                                holder.mPackageId);
//                        arguments.putCharSequence(
//                                TrackingNumberDetailFragment.ARG_PACKAGE_DESCRIPTION,
//                                holder.mIdView.getText());
                        TrackingNumberDetailFragment fragment = new TrackingNumberDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.trackingnumber_detail_container, fragment)
                                .commit();
                        if (mTrackingDetailTwoPane != null) {
                            mTrackingDetailTwoPane.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TrackingNumberDetailActivity.class);
                        intent.putExtra(
                                TrackingNumberDetailFragment.ARG_PACKAGE_ID,
                                holder.mPackageId);
                        intent.putExtra(
                                TrackingNumberDetailFragment.ARG_PACKAGE_DESCRIPTION,
                                holder.mIdView.getText());

                        context.startActivity(intent);
                    }
                }
            });

            // set the overflow icon setOnClickListener
            holder.mOverflowIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.inflate(MODE_ACTIVE == mListMode ?
                            R.menu.item_overflow_menu_active
                            :
                            R.menu.item_overflow_menu_archive);
                    popup.setOnMenuItemClickListener(holder);
                    popup.show();
                }
            });
        }

        public void swapCursor(Cursor newCursor) {
            if (mCursor != null && !mCursor.isClosed())
                mCursor.close();
            mCursor = newCursor;

            notifyDataSetChanged();
        }

        public class LatestEventsViewHolder
                extends RecyclerView.ViewHolder
                implements PopupMenu.OnMenuItemClickListener {
            public final View mView;
            public final View mBackgroundView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mIconView;
            public final ImageView mOverflowIconView;

            public String mPackageId;

            public LatestEventsViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mBackgroundView = itemView.findViewById(R.id.latest_background_view);
                mIdView = (TextView) itemView.findViewById(R.id.latest_text1);
                mContentView = (TextView) itemView.findViewById(R.id.latest_text2);
                mIconView = (ImageView) itemView.findViewById(R.id.latest_icon);
                mOverflowIconView = (ImageView) itemView.findViewById(R.id.overflow_icon);
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ContentResolver contentResolver = getContentResolver();
                ContentValues cv;

                switch (item.getItemId()) {
                    case R.id.rename_package:
                        Toast.makeText(getApplicationContext(), "rename package", Toast.LENGTH_SHORT)
                                .show();

                        return true;

                    case R.id.archive_package:
                        cv = new ContentValues(1);
                        cv.put(TrackingContract.PackagesEntry.COLUMN_ARCHIVED, "1");
                        contentResolver.update(TrackingContract.PackagesEntry.CONTENT_URI,
                                cv,
                                TrackingContract.PackagesEntry._ID + "=?",
                                new String[] { mPackageId });
                        if (mTrackingDetailTwoPane != null) {
                            mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
                        }
                        mSelectedPosition = -1;

                        return true;

                    case R.id.move_to_active_package:
                        cv = new ContentValues(1);
                        cv.put(TrackingContract.PackagesEntry.COLUMN_ARCHIVED, "0");
                        contentResolver.update(TrackingContract.PackagesEntry.CONTENT_URI,
                                cv,
                                TrackingContract.PackagesEntry._ID + "=?",
                                new String[] { mPackageId });
                        if (mTrackingDetailTwoPane != null) {
                            mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
                        }
                        mSelectedPosition = -1;

                        return true;

                    case R.id.delete_package:
                        contentResolver.delete(TrackingContract.PackagesEntry.CONTENT_URI,
                                TrackingContract.PackagesEntry._ID + "=?",
                                new String[] { mPackageId });
                        if (mTrackingDetailTwoPane != null) {
                            mTrackingDetailTwoPane.setVisibility(View.INVISIBLE);
                        }
                        mSelectedPosition = -1;

                        return true;

                    default:

                        return false;
                }
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
