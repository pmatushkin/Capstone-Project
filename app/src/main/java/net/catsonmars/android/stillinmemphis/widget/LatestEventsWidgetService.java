package net.catsonmars.android.stillinmemphis.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.catsonmars.android.stillinmemphis.R;
import net.catsonmars.android.stillinmemphis.data.TrackingContract;

/**
 * Created by pmatushkin on 5/12/2016.
 */
public class LatestEventsWidgetService extends RemoteViewsService {
    private static final String TAG = "LatestEventsWidget";

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

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public int getCount() {
                Log.d(TAG, "RemoteViewsFactory.getCount");

                int itemCount = null == data ? 0 : data.getCount();
                Log.d(TAG, "getCount returned records: " + itemCount);

                return itemCount;
            }

            @Override
            public void onDataSetChanged() {
                Log.d(TAG, "RemoteViewsFactory.onDataSetChanged");
                if (data != null) {
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                String sortOrder = TrackingContract.EventsEntry.COLUMN_TIMESTAMP + " DESC";
                data = getContentResolver().query(TrackingContract.PackagesEntry.buildPackagesWithLatestEventUri(),
                        PACKAGES_COLUMNS,
                        null,
                        null,
                        sortOrder);
                Log.d(TAG, "onDataSetChanged returned records: " + getCount());

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (AdapterView.INVALID_POSITION == position
                        || null == data
                        || !data.moveToPosition(position)) {
                    return null;
                }

                Log.d(TAG, "RemoteViewsFactory.getViewAt");

                RemoteViews views = getLoadingView();
                // set the package description string
                String packageDescriptionString = data.getString(COL_PACKAGE_DESCRIPTION);
                if((null == packageDescriptionString) || packageDescriptionString.isEmpty()) {
                    packageDescriptionString = data.getString(COL_PACKAGE_TRACKING_NUMBER);
                }
                Log.d(TAG, "packageDescriptionString: " + packageDescriptionString);
                views.setTextViewText(R.id.widget_latest_text1, packageDescriptionString);

                // set the event description string
                String eventDescriptionString;
                String eventType = data.getString(COL_EVENT_TYPE);
                String eventDescription = data.getString(COL_EVENT_DESCRIPTION);
                if (eventType.equals(TrackingContract.EventsEntry.TYPE_EVENT)) {
                    String eventDate = data.getString(COL_EVENT_DATE);
                    String eventTime = data.getString(COL_EVENT_TIME);

                    eventDescriptionString = String.format("%s, %s: %s", eventDate, eventTime, eventDescription);
                } else {
                    eventDescriptionString = eventDescription;
                }
                Log.d(TAG, "eventDescriptionString: " + eventDescriptionString);
                views.setTextViewText(R.id.widget_latest_text2, eventDescriptionString);

                // set the icon
                int iconId;
                int dateDelivered = data.getInt(COL_PACKAGE_DATE_DELIVERED);
                int packageArchived = data.getInt(COL_PACKAGE_ARCHIVED);
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
                views.setImageViewResource(R.id.widget_latest_icon, iconId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, packageDescriptionString + " " + eventType);
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.setData(TrackingContract.PackagesEntry.buildPackageUri(data.getInt(COL_PACKAGE_ID)));
                views.setOnClickFillInIntent(R.id.widget_latest_background_view, fillInIntent);

                return views;
            }

            @Override
            public long getItemId(int position) {
                Log.d(TAG, "RemoteViewsFactory.getItemId");

                if (data.moveToPosition(position))
                    return data.getInt(COL_PACKAGE_ID);
                else {
                    return position;
                }
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();

                    data = null;
                }
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.d(TAG, "RemoteViewsFactory.getLoadingView");

                return new RemoteViews(getPackageName(), R.layout.widget_latest_events_content);
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_latest_icon, "package " + description);
            }
        };
    }
}
