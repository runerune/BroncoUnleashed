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


public class Dashboard extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dashboard, container, false);
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
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to unlock", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Failed to unlock", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}