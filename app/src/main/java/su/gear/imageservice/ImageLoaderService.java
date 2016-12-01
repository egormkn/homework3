package su.gear.imageservice;

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
    public static final String FILENAME = "image.jpg";
    private static final String TEMP_FILENAME = "image_temp.jpg";

    public ImageLoaderService() {
        super(TAG);
    }

    private void sendBroadcast(int result, int progress) {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ACTION)
                        .putExtra("result", result)
                        .putExtra("progress", progress));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started");
        String urlToDownload = intent.getStringExtra("url");
        Log.d(TAG, "URL: " + urlToDownload);

        sendBroadcast(Utils.RESULT_STARTED, 0);

        int result = Utils.RESULT_LOADING;

        URLConnection connection;
        InputStream input = null;
        OutputStream output = null;

        try {
            File originalFile = new File(getApplicationContext().getFilesDir(), FILENAME);
            if (originalFile.exists()) {
                originalFile.delete();
            }
            URL url = new URL(urlToDownload);
            connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            input = new BufferedInputStream(connection.getInputStream());
            File outputFile = new File(getApplicationContext().getFilesDir(), TEMP_FILENAME);
            output = new FileOutputStream(outputFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);

                sendBroadcast(result, (int) (total * 100 / fileLength));
            }
            output.flush();

            boolean ignored = outputFile.renameTo(originalFile);
            result = Utils.RESULT_OK;
        } catch (Exception e) {
            result = Utils.RESULT_ERROR;
            Log.e(TAG, "Failed to get image: " + urlToDownload);
        } finally {
            Utils.closeSilently(input);
            Utils.closeSilently(output);
        }

        sendBroadcast(result, 100);
        Log.d(TAG, "Service stopped");
    }
}