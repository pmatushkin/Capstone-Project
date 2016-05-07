package net.catsonmars.android.stillinmemphis;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.catsonmars.android.stillinmemphis.data.TrackingContract;
import net.catsonmars.android.stillinmemphis.dummy.DummyContent;

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

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;
    private String mPackageId;

    private static final int EVENTS_LOADER = 1;
    private static final String[] EVENTS_COLUMNS = {
            // these two columns are for displaying the package description
            TrackingContract.PackagesEntry.COLUMN_TRACKING_NUMBER,
            TrackingContract.PackagesEntry.COLUMN_DESCRIPTION,
            // these three columns are for displaying the event details
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
    static final int COL_EVENT_TYPE = 2;
    static final int COL_EVENT_TIME = 3;
    static final int COL_EVENT_DATE = 4;
    static final int COL_EVENT_DESCRIPTION = 5;
    static final int COL_EVENT_CITY = 6;
    static final int COL_EVENT_STATE = 7;
    static final int COL_EVENT_ZIP = 8;
    static final int COL_EVENT_COUNTRY = 9;

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
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_PACKAGE_ID));

            mPackageId = getArguments().getString(ARG_PACKAGE_ID);
            Log.d(TAG, "mPackageId: " + mPackageId);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }

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

        View rootView = inflater.inflate(R.layout.trackingnumber_detail, container, false);

//        // Show the dummy content as text in a TextView.
//        if (mItem != null) {
//            ((TextView) rootView.findViewById(R.id.trackingnumber_detail)).setText(mItem.details);
//        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        Log.d(TAG, "mPackageId: " + mPackageId);

        return new CursorLoader(getActivity(),
                // all events
                TrackingContract.PackagesEntry.buildPackageWithEventsUri(Long.parseLong(mPackageId)),
                EVENTS_COLUMNS,
                null, // selection
                null, // selection args
                TrackingContract.EventsEntry.COLUMN_EVENT_ORDER + " ASC");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");

        Log.d(TAG, "returned records: " + data.getCount());
    }
}
