package bike.hackboy.bronco;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class SpeedSetting extends Fragment {
    private int value = 28;

    private static final UUID serviceUuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID characteristicUuid = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothGatt connection = ((MainActivity) getActivity()).getConnection();

                    byte[] changedCommand = Command.withValue(Command.SET_SPEED, value);
                    byte[] changedCommandWithChecksum = Command.withChecksum(changedCommand);

                    Log.d("gatt_command", Converter.byteArrayToHexString(changedCommandWithChecksum));

                    Gatt.ensureHasCharacteristic(connection, serviceUuid, characteristicUuid);
                    Gatt.writeCharacteristic(connection, serviceUuid, characteristicUuid, changedCommandWithChecksum);
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();

                } catch(Exception e) {
                    Log.w("write_fail", e.getMessage());
                    Toast.makeText(getActivity(), "Write failed. Check logcat.", Toast.LENGTH_LONG).show();
                }
            }
        });

        SeekBar slider = view.findViewById(R.id.seekBar);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView kph = view.findViewById(R.id.textView3);
                value = 25 + progress;
                kph.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }




}