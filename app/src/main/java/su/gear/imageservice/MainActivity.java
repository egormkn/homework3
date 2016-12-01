package su.gear.imageservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

    private ImageView imageView, blurredImageView;
    private TextView errorTextView, copyright;
    private ProgressBar progressBar;

    private BroadcastReceiver receiver;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        copyright = (TextView) findViewById(R.id.copyright);
        copyright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://simonstalenhag.se/"));
                startActivity(browserIntent);
            }
        });

        imageView = (ImageView) findViewById(R.id.image);
        blurredImageView = (ImageView) findViewById(R.id.image_blurred);
        errorTextView = (TextView) findViewById(R.id.not_loaded);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);
        progressBar.setMax(100);

        image = new File(getApplicationContext().getFilesDir(), ImageLoaderService.FILENAME);
        if (image.exists()) {
            showProgress();
            new ImageDrawingTask().execute(image);
        } else {
            showError();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int resultCode = intent.getIntExtra("result", Utils.RESULT_ERROR);

                switch (resultCode) {
                    case Utils.RESULT_ERROR:
                        showError();
                        break;
                    case Utils.RESULT_STARTED:
                        showProgress();
                        progressBar.setProgress(0);
                        break;
                    case Utils.RESULT_LOADING:
                        int progress = intent.getIntExtra("progress", 0);
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(progress);
                        if (progressBar.getVisibility() == View.GONE) {
                            showProgress();
                        }
                        break;
                    case Utils.RESULT_OK:
                        new ImageDrawingTask().execute(image);
                        break;
                }
                invalidateOptionsMenu();
            }
        };
    }

    private void showProgress() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        copyright.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        blurredImageView.setVisibility(View.GONE);
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        copyright.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        blurredImageView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (Utils.isServiceRunning(this, ImageLoaderService.class)) {
            menu.findItem(R.id.toolbar_update).setVisible(false);
        }
        if (!image.exists()) {
            menu.findItem(R.id.toolbar_delete).setVisible(false);
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
            if (Utils.isServiceRunning(this, ImageLoaderService.class)) {
                Toast.makeText(getApplicationContext(), "Service is running", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Service is not running", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.toolbar_delete) {
            showError();
            boolean ignored = image.delete();
            invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    class BitmapPair {
        private final Bitmap original, blurred;

        public BitmapPair(Bitmap original, Bitmap blurred) {
            this.original = original;
            this.blurred = blurred;
        }

        public Bitmap getOriginal() {
            return original;
        }

        public Bitmap getBlurred() {
            return blurred;
        }
    }

    class ImageDrawingTask extends AsyncTask<File, Void, BitmapPair> {

        @Override
        protected BitmapPair doInBackground(File... files) {
            if (!files[0].exists()) {
                return new BitmapPair(null, null);
            }
            Bitmap original = BitmapFactory.decodeFile(files[0].getAbsolutePath());
            return new BitmapPair(original, Utils.blurBitmap(getApplicationContext(), original));
        }

        @Override
        protected void onPostExecute(BitmapPair result) {
            super.onPostExecute(result);
            if (result.getBlurred() == null || result.getOriginal() == null) {
                showError();
            } else {
                imageView.setImageBitmap(result.getOriginal());
                blurredImageView.setImageBitmap(result.getBlurred());
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                blurredImageView.setVisibility(View.VISIBLE);
                copyright.setVisibility(View.VISIBLE);
            }
        }
    }
}
