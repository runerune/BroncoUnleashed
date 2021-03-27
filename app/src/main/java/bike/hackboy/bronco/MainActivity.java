package bike.hackboy.bronco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bike.hackboy.bronco.hal.BikeService;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_RESET_SPEED = "bike.hackboy.bronco.RESET_SPEED";
    private LocalBroadcastManager localBroadcastManager;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra("event");

            switch (event) {
                case "disconnected":
                    Navigation
                        .findNavController(MainActivity.this, R.id.nav_host_fragment)
                        .navigate(R.id.CbyDiscovery);
                break;
                case "toast":
                    Toast.makeText(
                        MainActivity.this.getApplicationContext(),
                        intent.getStringExtra("message"),
                        Toast.LENGTH_SHORT
                    ).show();
                break;
            }
        }
    };

    // --------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
            new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "clear-status")
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
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

        if(isCurrentFragmentBackable()) {
            return;
        }

        ensureStillConnected();

        assert this.getSupportActionBar() != null;
        this.getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, BikeService.class);
        startService(intent);

        if (ACTION_RESET_SPEED.equals(getIntent().getAction())) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "reset-speed")
            );

            this.finishAffinity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean isCurrentFragmentBackable() {
        // hacky but getFragmentById is very unreliable

        List<String> backable = new ArrayList<>();
        backable.add((String) getText(R.string.cby_user_details));
        backable.add((String) getText(R.string.speed_setting));
        backable.add((String) getText(R.string.field_weakening));
        backable.add((String) getText(R.string.settings));

        assert getSupportActionBar() != null;
        assert getSupportActionBar().getTitle() != null;

        //Log.d("backable", backable.toString());

        return backable.contains(getSupportActionBar().getTitle().toString());
    }

    protected void ensureStillConnected() {
        localBroadcastManager.sendBroadcast(
            new Intent(BuildConfig.APPLICATION_ID).putExtra("event", "check-connected")
        );
    }

    @Override
    public void onBackPressed() {
        if(isCurrentFragmentBackable()) {
            assert getSupportActionBar() != null;
            assert getSupportActionBar().getTitle() != null;
            CharSequence title = getSupportActionBar().getTitle();

            NavController nav = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
            ensureStillConnected();

            if (title == getText(R.string.settings)) {
                nav.navigate(R.id.Dashboard);
            } else {
                nav.navigate(R.id.Settings);
            }

            return;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}