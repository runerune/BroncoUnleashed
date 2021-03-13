package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.UUID;

import bike.hackboy.bronco.gatt.Gatt;

public class CbyDiscovery extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.speed_setting, container, false);
    }

    public void lookForCboy() {
        try {
            String mac = Gatt.getDeviceMacByName(BluetoothAdapter.getDefaultAdapter(), "COWBOY");
            Toast.makeText(getActivity(), String.format("Found %s", mac), Toast.LENGTH_SHORT).show();

            BluetoothGatt connection = new GattConnection()
                // we need services to be discovered before sending commands so connect here
                .setOnDiscoveryCallback(new DeployableVoid() {
                    void deploy() {
                        Log.w("foo", "in deploy");
                        NavHostFragment.findNavController(CbyDiscovery.this)
                            .navigate(R.id.action_CbyDiscovery_to_Dashboard);
                    }
                })
                .setOnCharacteristicRead(new DeployableCharacteristicRead() {
                    void deploy(UUID uuid, byte[] value) {

                    }
                })
                .connect(getContext(), "COWBOY");

            ((MainActivity) getActivity()).setConnection(connection);
        } catch(Exception e) {
            Log.w("lookup_fail", e.getMessage());
            Toast.makeText(getActivity(), "Could not find any bikes", Toast.LENGTH_LONG).show();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lookForCboy();
            }
        });
    }
}