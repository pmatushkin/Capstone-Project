package net.catsonmars.android.stillinmemphis;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.catsonmars.android.stillinmemphis.data.TrackingContract;
import net.catsonmars.android.stillinmemphis.ui.DividerItemDecoration;

import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a single Tracking Number detail screen.
 * This fragment is either contained in a {@link TrackingNumberListActivity}
 * in two-pane mode (on tablets) or a {@link TrackingNumberDetailActivity}
 * on handsets.
 */
public class TrackingNumberDetailFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback  {
    private static final String TAG = "NumberDetailFragment";

    /**
     * The fragment argument representing the package ID
     * that this fragment represents.
     */
    public static final String ARG_PACKAGE_ID = "package_id";
    public static final String ARG_PACKAGE_DESCRIPTION = "package_description";

    /**
     * The package id this fragment is presenting.
     */
    private String mPackageId;
    private String mPackageDescription;

    private PackageEventsAdapter mPackageEventsAdapter;
    private View mRecyclerView;

    private MapFragment mMapFragment;
    private View mMapView;
    private GoogleMap mGoogleMap;

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
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        if (getArguments().containsKey(ARG_PACKAGE_ID)) {
            mPackageId = getArguments().getString(ARG_PACKAGE_ID);
        }
        if (getArguments().containsKey(ARG_PACKAGE_DESCRIPTION)) {
            mPackageDescription = getArguments().getString(ARG_PACKAGE_DESCRIPTION);
        } else {
            mPackageDescription = "";
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
        Log.d(TAG, "onCreateView");

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mPackageDescription);
        }

        View rootView = inflater.inflate(R.layout.trackingnumber_detail, container, false);

        // set up map fragment
        mGoogleMap = null;
        mMapFragment = (MapFragment) getActivity()
                .getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (null == mMapFragment) {
            Log.d(TAG, "MapFragment is not found");
        } else {
            Log.d(TAG, "mapFragment is found");
            mMapFragment.getMapAsync(this);
            mMapView = mMapFragment.getView();
        }

        // set up RecyclerView
        mRecyclerView = rootView.findViewById(R.id.trackingnumber_detail_list);
        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mGoogleMap = googleMap;
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

        // Hide the map if there is no event data to display,
        // because the map is displayed for the first event only.
        // No events -> no map.
        // It happens for example when in a two-pane mode
        // a package is selected, and then deleted.
        if (data.getCount() == 0 && mMapView != null) {
            mMapView.setVisibility(View.GONE);
        }

        mPackageEventsAdapter.swapCursor(data);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(TAG, "setupRecyclerView");

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mPackageEventsAdapter);
    }

    public class PackageEventsAdapter
            extends RecyclerView.Adapter<PackageEventsAdapter.PackageEventsViewHolder> {
        private Cursor mCursor;

        public void swapCursor(Cursor newCursor) {
            Log.d(TAG, "swapCursor");

            if (mCursor != null && !mCursor.isClosed())
                mCursor.close();
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
            String eventId = mCursor.getString(COL_EVENT_ORDER);
            holder.mEventId = eventId;

            Context context = getContext();

            // get the event type (event/error)
            String eventType = mCursor.getString(COL_EVENT_TYPE);

            // set the event description string
            String eventDescriptionString;
            String eventDescription = mCursor.getString(COL_EVENT_DESCRIPTION);
            if (eventType.equals(TrackingContract.EventsEntry.TYPE_EVENT)) {
                String eventDate = mCursor.getString(COL_EVENT_DATE);
                String eventTime = mCursor.getString(COL_EVENT_TIME);

                String eventDateTimeString = FormatUtils.formatUSPSDateTime(eventDate, eventTime);

                eventDescriptionString = String.format("%s: %s", eventDateTimeString, eventDescription);
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
                if (eventAddress.length() == 0) {
                    eventAddress = eventCountry;
                } else {
                    eventAddress = String.format("%s, %s", eventAddress, eventCountry);
                }
            }
            holder.mContentView.setText(eventAddress);

            // show/hide map fragment depending on the event type
            if (mMapView != null) {
                if (eventType.equals(TrackingContract.EventsEntry.TYPE_ERROR)) {
                    mMapView.setVisibility(View.GONE);
                } else if (mMapView.getVisibility() == View.GONE) {
                    mMapView.setVisibility(View.VISIBLE);
                }

                if (mMapView.getVisibility() == View.VISIBLE
                        && eventId.equals("0")) {
                    // make no more than 10 attempts to geocode the address
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    int maxGeocodingResults = 1;

                    try {
                        int maxGeocodingAttempts = 10;
                        int currGeocodingAttempts = 0;

                        List<Address> geocodingResults =
                                geocoder.getFromLocationName(eventAddress, maxGeocodingResults);

                        while (0 == geocodingResults.size()
                                && currGeocodingAttempts < maxGeocodingAttempts) {
                            geocodingResults =
                                    geocoder.getFromLocationName(eventAddress, maxGeocodingResults);

                            currGeocodingAttempts++;
                        }

                        // set up the map marker
                        LatLng latLng;
                        if (geocodingResults.size() > 0) {
                            Address address = geocodingResults.get(0);
                            latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        } else {
                            // Geographic Center of the Contiguous United States
                            latLng = new LatLng(39.8333314, -98.6008429);
                        }

                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                        mGoogleMap.addMarker(new MarkerOptions()
                                .visible(true)
                                .title(eventAddress)
                                .snippet(eventDescriptionString)
                                .position(latLng));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }

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
            icon = ContextCompat.getDrawable(context, iconId);
            holder.mIconView.setImageDrawable(icon);
            holder.mIconView.setContentDescription(eventDescriptionString);
        }

        @Override
        public int getItemCount() {
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
