package su.gear.imageservice;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String IMAGE_FILE = "image.png";
    private final String IMAGE_TEMP_FILE = "image_temp.png";

    private ImageView imageView;
    private TextView errorTextView;
    private ProgressBar progressBar;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.image);
        errorTextView = (TextView) findViewById(R.id.not_loaded);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        /*progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        progressBar.setMax(100);*/

        File outputFile = new File(getApplicationContext().getFilesDir(), IMAGE_FILE);

        if (outputFile.exists()) {
            showImage(outputFile);
        } else {
            showError();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int resultCode = intent.getIntExtra("result", Activity.RESULT_CANCELED);

                switch (resultCode) {
                    case Utils.RESULT_ERROR:
                        showError();
                        break;
                    case Utils.RESULT_LOADING:
                        /*int progress = intent.getIntExtra("progress", -1);
                        progressBar.setProgress(progress);*/
                        if (progressBar.getVisibility() == View.GONE) {
                            progressBar.setVisibility(View.VISIBLE);
                            errorTextView.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
                        }
                        break;
                    case Utils.RESULT_OK:
                        String path = intent.getStringExtra("path");
                        if (path != null) {
                            showImage(new File(path));
                        }
                        break;
                }
                invalidateOptionsMenu();
            }
        };
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        File savedImage = new File(getApplicationContext().getFilesDir(), IMAGE_TEMP_FILE);
        if (savedImage.exists()) {
            File image = new File(getApplicationContext().getFilesDir(), IMAGE_FILE);
            savedImage.renameTo(image);
            showImage(image);
        }
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
    }

    private void showImage(File image) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        File tempFile = new File(getApplicationContext().getFilesDir(), IMAGE_TEMP_FILE);
        tempFile.delete();
        image.renameTo(tempFile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (isServiceRunning(ImageLoaderService.class)) {
            menu.findItem(R.id.toolbar_update).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.toolbar_update) {
            Intent i = new Intent(getApplicationContext(), ImageLoaderService.class);
            i.putExtra("url", Utils.getNextImageUrl());
            startService(i);
            invalidateOptionsMenu();
            return true;
        } else if (id == R.id.toolbar_status) {
            if (isServiceRunning(ImageLoaderService.class)) {
                Toast.makeText(getApplicationContext(), "Service is running", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Service is not running", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ImageLoaderService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
