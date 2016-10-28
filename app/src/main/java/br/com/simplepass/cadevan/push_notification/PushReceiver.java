package br.com.simplepass.cadevan.push_notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import br.com.simplepass.cadevan.R;

/**
 * Created by leandro on 4/19/16.
 */
public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationDesc = "test";

        if (intent.getStringExtra("message") != null ) {
            notificationDesc = intent.getStringExtra("message");
            sendNotification(context, notificationDesc);
        }

    }

    private void sendNotification(Context context, String message){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_app)
                        .setContentText(message)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setSound(alarmSound);

        /*Intent notificationIntentDefault = new Intent(this, RequestRideActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntentDefault,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(contentIntent);*/

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(8780, mBuilder.build());
    }
}
