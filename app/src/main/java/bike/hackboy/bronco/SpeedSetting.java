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
import androidx.navigation.fragment.NavHostFragment;

import java.util.UUID;

import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.gatt.Gatt;
import bike.hackboy.bronco.utils.Converter;

public class SpeedSetting extends Fragment {
    private int value = 28;

    private UUID serviceUuid;
    private UUID characteristicUuid;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        serviceUuid = Uuid.serviceSettings;
        characteristicUuid = Uuid.characteristicSettingsPcb;

        return inflater.inflate(R.layout.speed_setting, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_speed_apply).setOnClickListener(new View.OnClickListener() {
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

                    NavHostFragment.findNavController(SpeedSetting.this).navigate(R.id.action_SpeedSetting_to_Dashboard);
                } catch(Exception e) {
                    Log.w("write_fail", e.getMessage());
                    Toast.makeText(getActivity(), "Write failed. Check logcat.", Toast.LENGTH_LONG).show();
                }
            }
        });

        view.findViewById(R.id.button_speed_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SpeedSetting.this).navigate(R.id.action_SpeedSetting_to_Dashboard);
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