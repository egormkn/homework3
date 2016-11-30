package su.gear.imageservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = CustomBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ImageLoaderService.class);
        i.putExtra("url", Utils.getNextImageUrl());
        context.startService(i);
    }
}
