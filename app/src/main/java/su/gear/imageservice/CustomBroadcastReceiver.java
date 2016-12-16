package su.gear.imageservice;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class CustomBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = CustomBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ImageLoaderService.isRunning) {
            return;
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_update)
                        .setContentTitle("ImageService")
                        .setContentText("Receiver triggered")
                        .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

        Intent i = new Intent(context, ImageLoaderService.class);
        i.putExtra("url", Utils.getNextImageUrl());
        context.startService(i);
    }
}
