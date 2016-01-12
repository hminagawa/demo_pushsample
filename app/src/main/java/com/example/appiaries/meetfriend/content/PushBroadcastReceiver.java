package com.example.appiaries.meetfriend.content;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.appiaries.meetfriend.R;

/**
 * PUSH通知を受け取るためのBroadcastReceiverです。
 */
public class PushBroadcastReceiver extends BroadcastReceiver {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Notificationの設定
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        // アイコン
        builder.setSmallIcon(R.drawable.ic_launcher);
        // Tickerテキスト(ステータスバーに表示される文字列)
        builder.setTicker("MeetFriendからのお知らせ");
        // 通知内容のタイトル
        builder.setContentTitle(intent.getStringExtra("title"));
        // 通知内容のテキスト
        builder.setContentText(intent.getStringExtra("message"));
        // タップすれば消える設定
        builder.setAutoCancel(true);

        // タップされた際に発行するIntent
        Intent newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("url")));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // 音とバイブレーション
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        // NotificationManagerの生成
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}
