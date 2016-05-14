package net.catsonmars.android.stillinmemphis.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import net.catsonmars.android.stillinmemphis.R;
import net.catsonmars.android.stillinmemphis.TrackingNumberDetailActivity;
import net.catsonmars.android.stillinmemphis.TrackingNumberListActivity;
import net.catsonmars.android.stillinmemphis.sync.StillInMemphisSyncAdapter;

/**
 * Created by pmatushkin on 5/12/2016.
 */
public class LatestEventsAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "LatestEventsWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "LatestEventsAppWidgetProvider.onUpdate");

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_latest_events);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, TrackingNumberListActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            Intent clickIntentTemplate = new Intent(context, TrackingNumberDetailActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_latest_events_list, clickPendingIntentTemplate);

            views.setEmptyView(R.id.widget_latest_events_list, R.id.widget_empty_view);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(TAG, "LatestEventsAppWidgetProvider.onReceive");

        if (StillInMemphisSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.d(TAG, "notifyAppWidgetViewDataChanged");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            Log.d(TAG, "found widgets: " + appWidgetIds.length);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_latest_events_list);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_latest_events_list,
                new Intent(context, LatestEventsWidgetService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_latest_events_list,
                new Intent(context, LatestEventsWidgetService.class));
    }

}
