package su.gear.imageservice;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageLoaderService extends IntentService {

    public static final String TAG = ImageLoaderService.class.getSimpleName();

    public static final String ACTION = "su.gear.imageservice.ImageLoaderService";

    public ImageLoaderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started");
        String urlToDownload = intent.getStringExtra("url");
        Log.d(TAG, "URL: " + urlToDownload);

        int result = Utils.RESULT_LOADING;

        File outputFile = null;

        URLConnection connection;
        InputStream input = null;
        OutputStream output = null;

        try {
            URL url = new URL(urlToDownload);
            connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            input = new BufferedInputStream(connection.getInputStream());
            outputFile = new File(getApplicationContext().getFilesDir(), "image.png");
            output = new FileOutputStream(outputFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);

                /*Intent in = new Intent(ACTION);
                in.putExtra("progress", (int) (total * 100 / fileLength));
                in.putExtra("result", result);
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);*/
            }
            output.flush();

            result = Utils.RESULT_OK;
        } catch (Exception e) {
            result = Activity.RESULT_CANCELED;
            Log.e(TAG, "Failed to get image: " + urlToDownload);
        } finally {
            Utils.closeSilently(input);
            Utils.closeSilently(output);
        }

        Intent in = new Intent(ACTION);
        in.putExtra("progress", 100);
        in.putExtra("result", result);
        if (outputFile != null) {
            in.putExtra("path", outputFile.getAbsolutePath());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        Log.d(TAG, "Service stopped");
    }
}