package com.appiaries.pushsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Activity to handle Notifications.
 * We have this activity class just to simplify the opened-message codes.
 */
public class NotificationHelperActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent baseIntent = getIntent();
        final String action = baseIntent.getAction();
        // Requesting Opened-Message
        if (PushBroadcastReceiver.ACTION_NOTIFICATION_OPEN.equals(action)) {
            // Starting the service.
            final Intent openMessageIntent = new Intent(this, OpenMessageService.class);
            openMessageIntent.putExtra(Config.KEY_PUSH_ID, baseIntent.getStringExtra(Config.KEY_PUSH_ID));
            startService(openMessageIntent);
        }

        // Launch the app.
        final Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(PushBroadcastReceiver.ACTION_NOTIFICATION_OPEN);
        intent.putExtra(Config.NOTIFICATION_KEY_TITLE, baseIntent.getStringExtra(Config.NOTIFICATION_KEY_TITLE));
        intent.putExtra(Config.NOTIFICATION_KEY_MESSAGE, baseIntent.getStringExtra(Config.NOTIFICATION_KEY_MESSAGE));
        startActivity(intent);

        finish();
    }


}
