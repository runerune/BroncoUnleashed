package bike.hackboy.bronco;

import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    BluetoothGatt connection;
    ObservableLocked lockState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lockState = new ObservableLocked();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Bike hack & app by /u/runereader for /r/cowboybikes. Use at your own risk.")
                .setNegativeButton("Got it", null)
                .setPositiveButton("Visit sub", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/cowboybikes/"));
                        startActivity(browserIntent);
                    }
                })
                .show();
        }

        return super.onOptionsItemSelected(item);
    }

    public ObservableLocked getObservableLocked() {
        return lockState;
    }

    public void setLocked(boolean newLockState) {
        lockState.setLocked(newLockState);
    }

    public BluetoothGatt getConnection() {
        return connection;
    }

    public void setConnection(BluetoothGatt newConnection) {
        if (connection instanceof BluetoothGatt) {
            connection.close();
        }
        connection = newConnection;
    }
}