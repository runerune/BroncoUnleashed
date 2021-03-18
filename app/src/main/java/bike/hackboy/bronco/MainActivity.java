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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import bike.hackboy.bronco.hal.BikeService;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_RESET_SPEED = "bike.hackboy.bronco.RESET_SPEED";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");

            switch (event) {
                case "disconnected":
                    PendingIntent pendingIntent = new NavDeepLinkBuilder(MainActivity.this.getApplicationContext())
                        .setGraph(R.navigation.nav_graph)
                        .setDestination(R.id.CbyDiscovery)
                        .createPendingIntent();

                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e("disconnect", "intent failed", e);
                    }
                break;
                case "toast":
                    Toast.makeText(
                        MainActivity.this.getApplicationContext(),
                        intent.getStringExtra("message"),
                        Toast.LENGTH_LONG
                    ).show();
                break;
            }
        }
    };

    // --------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
            new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "disconnect")
        );

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getApplicationContext())
            .registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, BikeService.class);
        //ContextCompat.startForegroundService(this, intent);

        startService(intent);

        if (ACTION_RESET_SPEED.equals(getIntent().getAction())) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "reset-speed")
            );

            this.finishAffinity();
        }
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

        if (id == R.id.about) {
            new AlertDialog.Builder(this, R.style.Theme_Bronco_AlertDialog)
                .setTitle(R.string.about_title)
                .setMessage(R.string.credits)
                .setNegativeButton(R.string.got_it, null)
                .setPositiveButton(R.string.visit_sub, (dialog, whichButton) -> {
                    dialog.dismiss();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/cowboybikes/"));
                    startActivity(browserIntent);
                })
                .show();
        } else if(id == R.id.disconnect) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "disconnect")
            );
        } else if(id == R.id.stop_and_quit) {
            stopService(new Intent(this, BikeService.class));
            this.finishAffinity();
        } else if(id == R.id.debug_write_flash) {
            LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
                    .putExtra("event", "write-flash"));
        } else if(id == R.id.debug_close_flash) {
            LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
                    .putExtra("event", "close-flash"));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}