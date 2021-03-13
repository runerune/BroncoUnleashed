package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

public class GattConnection {
    private BluetoothAdapter bluetoothAdapter;
    private Deployable onDiscoveryCallback;

    public GattConnection() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothGatt connect(Context context, String name) throws Exception {
        // why does this even need Context...
        String mac = Gatt.getDeviceMacByName(bluetoothAdapter, name);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

        return device.connectGatt(context, false, mGattCallback);
    }

    public GattConnection setOnDiscoveryCallback(Deployable callback) {
        onDiscoveryCallback = callback;
        return this;
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
                Log.d("onServicesDiscovered", "");

                if (onDiscoveryCallback instanceof Deployable) {
                    onDiscoveryCallback.deploy();
                }
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
}
