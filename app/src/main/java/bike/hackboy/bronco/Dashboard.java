package bike.hackboy.bronco;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.gatt.Gatt;


public class Dashboard extends Fragment {
    private boolean locked = false;
    private boolean lightsOn = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getObservableLocked().setListener(new ObservableLocked.ChangeListener() {
            @Override
            public void onChange() {
                Dashboard.this.locked = ((MainActivity) getActivity()).getObservableLocked().isLocked();
                Log.w("is_locked", Dashboard.this.locked ? "LOCKED": "UNLOCKED");


            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dashboard, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

            Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
            Gatt.requestReadCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);

            Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Failed to request read lock state", Toast.LENGTH_LONG).show();
            Log.e("read_lock", "failed to read lock state", e);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        view.findViewById(R.id.button_goto_set_speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(Dashboard.this)
                    .navigate(R.id.action_Dashboard_to_SpeedSetting);
            }
        });

        view.findViewById(R.id.button_unlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

                    Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
                    Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.UNLOCK);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to unlock", Toast.LENGTH_LONG).show();
                    Log.e("cmd_unlosk", "failed to unlock", e);
                }
            }
        });

        view.findViewById(R.id.button_lock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

                    Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
                    Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.LOCK);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to lock", Toast.LENGTH_LONG).show();
                }
            }
        });

        view.findViewById(R.id.button_light_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

                    byte[] command = Command.withChecksum(Command.LIGHT_ON);

                    Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
                    Gatt.writeCharacteristic(connection,  Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to turn lights on", Toast.LENGTH_LONG).show();
                }
            }
        });

        view.findViewById(R.id.button_light_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

                    byte[] command = Command.withChecksum(Command.LIGHT_OFF);

                    Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
                    Gatt.writeCharacteristic(connection,  Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to turn lights off", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}