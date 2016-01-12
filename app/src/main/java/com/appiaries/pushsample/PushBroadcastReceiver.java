package com.appiaries.pushsample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * BroadcastReceviver to receive Push Notification.
 */
public class PushBroadcastReceiver extends WakefulBroadcastReceiver {

    /** Action for Opened-Message */
    public static final String ACTION_NOTIFICATION_OPEN = "appiaries.intent.action.NOTIFICATION_OPEN";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Notification Settings
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        // Here is our icon.
        builder.setSmallIcon(R.mipmap.ic_launcher);
        final String title = intent.getStringExtra(Config.NOTIFICATION_KEY_TITLE);
        final String message = intent.getStringExtra(Config.NOTIFICATION_KEY_MESSAGE);
        // Ticker text (shown to the status region)
        builder.setTicker(title);
        // Title of the message.
        builder.setContentTitle(title);
        // Message of the message.
        builder.setContentText(message);
        // Optional settings to let the message disappear when tapping.
        builder.setAutoCancel(true);

        // Intent issued when tapped.
        final Intent newIntent = new Intent(context, NotificationHelperActivity.class);
        newIntent.setAction(ACTION_NOTIFICATION_OPEN);
        // Set every possible data passed.
        newIntent.putExtras(intent.getExtras());
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Sound and vibration.
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // Creating NotificationManager.
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}
