package net.catsonmars.android.stillinmemphis;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.catsonmars.android.stillinmemphis.data.TrackingContract;

/**
 * A fragment representing a single Tracking Number detail screen.
 * This fragment is either contained in a {@link TrackingNumberListActivity}
 * in two-pane mode (on tablets) or a {@link TrackingNumberDetailActivity}
 * on handsets.
 */
public class TrackingNumberDetailFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "NumberDetailFragment";

    /**
     * The fragment argument representing the package ID
     * that this fragment represents.
     */
    public static final String ARG_PACKAGE_ID = "package_id";

    /**
     * The package id this fragment is presenting.
     */
    private String mPackageId;

    private PackageEventsAdapter mPackageEventsAdapter;
    private View mRecyclerView;

    private static final int EVENTS_LOADER = 1;
    private static final String[] EVENTS_COLUMNS = {
            // these two columns are for displaying the package description
            TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER,
            TrackingContract.PackagesEntry.COLUMN_DESCRIPTION,
            // these two columns are for selecting the right icon
            TrackingContract.PackagesEntry.COLUMN_DATE_DELIVERED,
            TrackingContract.PackagesEntry.COLUMN_ARCHIVED,
            // this column is there for the debugging purposes
            TrackingContract.EventsEntry.COLUMN_EVENT_ORDER,
            // these columns are for displaying the event details
            TrackingContract.EventsEntry.COLUMN_TYPE,
            TrackingContract.EventsEntry.COLUMN_TIME,
            TrackingContract.EventsEntry.COLUMN_DATE,
            TrackingContract.EventsEntry.COLUMN_EVENT,
            TrackingContract.EventsEntry.COLUMN_CITY,
            TrackingContract.EventsEntry.COLUMN_STATE,
            TrackingContract.EventsEntry.COLUMN_ZIP,
            TrackingContract.EventsEntry.COLUMN_COUNTRY
    };
    // These indices are tied to EVENTS_COLUMNS.
    // If EVENTS_COLUMNS changes, these must change.
    static final int COL_PACKAGE_TRACKING_NUMBER = 0;
    static final int COL_PACKAGE_DESCRIPTION = 1;
    static final int COL_PACKAGE_DATE_DELIVERED = 2;
    static final int COL_PACKAGE_ARCHIVED = 3;
    static final int COL_EVENT_ORDER = 4;
    static final int COL_EVENT_TYPE = 5;
    static final int COL_EVENT_TIME = 6;
    static final int COL_EVENT_DATE = 7;
    static final int COL_EVENT_DESCRIPTION = 8;
    static final int COL_EVENT_CITY = 9;
    static final int COL_EVENT_STATE = 10;
    static final int COL_EVENT_ZIP = 11;
    static final int COL_EVENT_COUNTRY = 12;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackingNumberDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO fix onCreate by removing the references to the dummy object
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        if (getArguments().containsKey(ARG_PACKAGE_ID)) {
            mPackageId = getArguments().getString(ARG_PACKAGE_ID);
        }

        mPackageEventsAdapter = new PackageEventsAdapter();

        if (getLoaderManager().getLoader(EVENTS_LOADER) == null) {
            getLoaderManager().initLoader(EVENTS_LOADER, savedInstanceState, this);
        } else {
            getLoaderManager().restartLoader(EVENTS_LOADER, savedInstanceState, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO fix onCreate by removing the references to the dummy object
        Log.d(TAG, "onCreateView");

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mPackageId);
        }

        View rootView = inflater.inflate(R.layout.trackingnumber_detail, container, false);

        // set up RecyclerView
        mRecyclerView = rootView.findViewById(R.id.trackingnumber_detail_list);
        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader for package id " + mPackageId);

        return new CursorLoader(getActivity(),
                // all events
                TrackingContract.PackagesEntry.buildPackageWithEventsUri(Long.parseLong(mPackageId)),
                EVENTS_COLUMNS,
                null, // selection
                null, // selection args
                // sort the events from the mot recent to the most distant
                TrackingContract.EventsEntry.COLUMN_EVENT_ORDER + " ASC");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");

        mPackageEventsAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");

        Log.d(TAG, "returned records: " + data.getCount());

        mPackageEventsAdapter.swapCursor(data);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(TAG, "setupRecyclerView");

        recyclerView.setAdapter(mPackageEventsAdapter);
    }

    public class PackageEventsAdapter
            extends RecyclerView.Adapter<PackageEventsAdapter.PackageEventsViewHolder> {
        private Cursor mCursor;

        public void swapCursor(Cursor newCursor) {
            Log.d(TAG, "swapCursor");

            mCursor = newCursor;
            notifyDataSetChanged();
        }

        @Override
        public PackageEventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            Log.d(TAG, "onCreateViewHolder");

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.packageevents_list_content, parent, false);

            return new PackageEventsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PackageEventsViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            // set the event id
            holder.mEventId = mCursor.getString(COL_EVENT_ORDER);

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
            holder.mIdView.setText(eventDescriptionString);

            // set the event address string
            String eventCity = mCursor.getString(COL_EVENT_CITY);
            if (null == eventCity) {
                eventCity = "";
            }
            String eventState = mCursor.getString(COL_EVENT_STATE);
            if (null == eventState) {
                eventState = "";
            }
            String eventZip = mCursor.getString(COL_EVENT_ZIP);
            if (null == eventZip) {
                eventZip = "";
            }
            String eventStateZip = String.format("%s %s", eventState, eventZip).trim();
            String eventCountry = mCursor.getString(COL_EVENT_COUNTRY);
            if (null == eventCountry) {
                eventCountry = "";
            }
            String eventAddress = "";
            if (eventCity.length() > 0) {
                eventAddress = eventCity;
            }
            if (eventStateZip.length() > 0) {
                if (eventAddress.length() == 0) {
                    eventAddress = eventStateZip;
                } else {
                    eventAddress = String.format("%s, %s", eventAddress, eventStateZip);
                }
            }
            if (eventCountry.length() > 0) {
                if (eventAddress.length() > 0) {
                    eventAddress = eventCountry;
                } else {
                    eventAddress = String.format("%s, %s", eventAddress, eventCountry);
                }
            }
            holder.mContentView.setText(eventAddress);

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
            icon = ContextCompat.getDrawable(getContext(), iconId);
            holder.mIconView.setImageDrawable(icon);
            holder.mIconView.setContentDescription(eventDescriptionString);
        }

        @Override
        public int getItemCount() {
//            Log.d(TAG, "getItemCount");

            return null == mCursor ?
                    0
                    :
                    mCursor.getCount();
        }

        public class PackageEventsViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final View mBackgroundView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mIconView;

            public String mEventId;

            public PackageEventsViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mBackgroundView = itemView.findViewById(R.id.package_background_view);
                mIdView = (TextView) itemView.findViewById(R.id.package_text1);
                mContentView = (TextView) itemView.findViewById(R.id.package_text2);
                mIconView = (ImageView) itemView.findViewById(R.id.package_icon);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
