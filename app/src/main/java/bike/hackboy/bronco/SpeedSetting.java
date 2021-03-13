package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SpeedSetting extends Fragment {
    private int value = 28;
    private boolean everythingNeededExists = false;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final UUID serviceUuid = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID characteristicUuid = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private static final byte[] command = {10, 16, 0, 4, 0, 1, 2, 0, 30};

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
        connect();

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (everythingNeededExists) {
                    byte[] changedCommand = Arrays.copyOf(command, 9);
                    changedCommand[8] = (byte) value;

                    byte[] checksum = getChecksum(changedCommand);
                    byte[] changedCommandWithChecksum = Arrays.copyOf(changedCommand, 11);

                    changedCommandWithChecksum[9] = checksum[0];
                    changedCommandWithChecksum[10] = checksum[1];

                    Log.d("gatt_command", bytesToHex(changedCommandWithChecksum));

                    writeCharacteristic(changedCommandWithChecksum);
                }

            }
        });

        SeekBar slider = view.findViewById(R.id.seekBar);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView kph = (TextView) view.findViewById(R.id.textView3);
                value = 25 + progress;
                kph.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public static byte[] getChecksum(byte[] bArr) {
        int i = 0;
        int i2 = DEFAULT_INITIAL_WINDOW_SIZE;
        for (byte b : bArr) {
            i2 ^= b & 255;
            for (int i3 = 8; i3 > 0; i3--) {
                int i4 = i2 & 1;
                i2 >>= 1;
                if (i4 != 0) {
                    i2 ^= 40961;
                }
            }
        }
        int[] iArr = {i2 & 255, (i2 >> 8) & 255};
        byte[] bArr2 = new byte[2];
        int i5 = 0;
        while (i < 2) {
            bArr2[i5] = (byte) iArr[i];
            i++;
            i5++;
        }
        return bArr2;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void connect() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName().trim();
                String deviceMacAddress = device.getAddress();

                if(deviceName.equals("COWBOY")) {
                    device = bluetoothAdapter.getRemoteDevice(deviceMacAddress);
                    bluetoothGatt = device.connectGatt(getContext(), false, mGattCallback);
                }
            }
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("onServicesDiscovered", "");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = bluetoothGatt.getServices();

                outerLoop:
                for (BluetoothGattService service : services) {
                    if(service.getUuid().equals(serviceUuid)) {
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            Log.d("characteristic", characteristic.getUuid().toString().toLowerCase());

                            if(characteristic.getUuid().equals(characteristicUuid)) {
                                everythingNeededExists = true;
                                break outerLoop;
                            }
                        }
                    }
                }
            }

            if(!everythingNeededExists) {
                Toast.makeText(getActivity(), "Could not find activity or service", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("onCharacteristicRead", "");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("onCharacteristicChanged", "");
        }
    };

    private void writeCharacteristic(byte[] data) {
        Log.d("gatt_cmd_before_write", bytesToHex(data));
        Toast.makeText(getActivity(), String.format("GATT command %s", bytesToHex(data)), Toast.LENGTH_SHORT).show();

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.e("gatt", "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattService speedService = bluetoothGatt.getService(serviceUuid);
        if(speedService == null){
            Log.e("gatt", "service not found");
            return;
        }

        BluetoothGattCharacteristic characteristic = speedService.getCharacteristic(characteristicUuid);
        characteristic.setValue(data);
        boolean success = bluetoothGatt.writeCharacteristic(characteristic);

        if(success) {
            Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Write failed. Check logcat.", Toast.LENGTH_LONG).show();
        }
    }
}