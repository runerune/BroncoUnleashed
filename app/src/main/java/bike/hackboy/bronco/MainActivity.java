package bike.hackboy.bronco;

import android.app.PendingIntent;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavDeepLinkBuilder;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    BluetoothGatt connection;
    ObservableLocked lockState;
    ObservableDashboard dashboardState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lockState = new ObservableLocked();
        dashboardState = new ObservableDashboard();
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
                PendingIntent pendingIntent = new NavDeepLinkBuilder(this.getApplicationContext())
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.CbyDiscovery)
                    .createPendingIntent();

                try {
                    connection.close();
                    connection.disconnect();
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    Log.e("disconnect", "intent failed", e);
                }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public ObservableLocked getObservableLocked() {
        return lockState;
    }

    public ObservableDashboard getObservableDashboard() {
        return dashboardState;
    }

    public BluetoothGatt getConnection() {
        return connection;
    }

    public void setConnection(BluetoothGatt newConnection) {
        if (connection != null) {
            connection.close();
        }
        connection = newConnection;
    }
}