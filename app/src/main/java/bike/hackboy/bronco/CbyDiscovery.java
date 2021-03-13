package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Set;

public class CbyDiscovery extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void lookForCboy() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName().trim();
                String deviceMacAddress = device.getAddress();
                Log.d("device found", deviceName);

                if(deviceName.equals("COWBOY")) {
                    Toast.makeText(getActivity(), String.format("Found %s", deviceMacAddress), Toast.LENGTH_SHORT).show();

                    NavHostFragment.findNavController(CbyDiscovery.this)
                        .navigate(R.id.action_CbyDiscovery_to_SpeedSetting);

                    return;
                }
            }
        }

        Toast.makeText(getActivity(), String.format("Could not find any bikes"), Toast.LENGTH_LONG).show();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lookForCboy();
            }
        });
    }
}