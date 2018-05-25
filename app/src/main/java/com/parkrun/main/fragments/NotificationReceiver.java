package com.parkrun.main.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.parkrun.main.R;
import com.parkrun.main.activities.MainActivity;

public class NotificationReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeatingIntent = new Intent(context, MainActivity.class);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channel = "weather";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.parkrun_icon_inverted)
                .setContentTitle("parkrun Weather")
                .setContentText("Check the app for weather for tomorrow morning!")
                .setAutoCancel(true);

        notificationManager.notify(100, builder.build());
    }
}
