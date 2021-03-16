package bike.hackboy.bronco;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import bike.hackboy.bronco.hal.BikeService;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_RESET_SPEED = "bike.hackboy.bronco.RESET_SPEED";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");

            if(!event.equals("disconnected")) return;

            PendingIntent pendingIntent = new NavDeepLinkBuilder(MainActivity.this.getApplicationContext())
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.CbyDiscovery)
                .createPendingIntent();

            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e("disconnect", "intent failed", e);
            }
        }
    };

    // --------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(getApplicationContext())
            .registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, BikeService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO stop service
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.disconnect);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.about:
                new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Bike hack & app by /u/runereader for /r/cowboybikes. Use at your own risk.")
                    .setNegativeButton("Got it", null)
                    .setPositiveButton("Visit sub", (dialog, whichButton) -> {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/cowboybikes/"));
                        startActivity(browserIntent);
                    })
                    .show();
            break;
            case R.id.disconnect:
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                    new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "disconnect")
                );
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    // --------------------------------------------------

}