package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import bike.hackboy.bronco.gatt.Gatt;
import bike.hackboy.bronco.utils.Converter;

public class GattConnection {
    private final BluetoothAdapter bluetoothAdapter;
    private DeployableVoid onDiscoveryCallback;
    private DeployableCharacteristicRead onCharacteristicRead;

    public GattConnection() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothGatt connect(Context context, String name) throws Exception {
        String mac = Gatt.getDeviceMacByName(bluetoothAdapter, name);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

        return device.connectGatt(context, false, mGattCallback);
    }

    public GattConnection setOnCharacteristicRead(DeployableCharacteristicRead callback) {
        onCharacteristicRead = callback;
        return this;
    }

    public GattConnection setOnDiscoveryCallback(DeployableVoid callback) {
        onDiscoveryCallback = callback;
        return this;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Log.d("onConnectionStateChange", "");
                gatt.discoverServices();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Log.d("onServicesDiscovered", "");

                if (onDiscoveryCallback != null) {
                    onDiscoveryCallback.deploy();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Log.d("onCharacteristicRead", "");

                if(onCharacteristicRead != null) {
                    onCharacteristicRead.deploy(characteristic.getUuid(), characteristic.getValue());
                }

                byte[] value = characteristic.getValue();
                //Log.d("TAG", "onCharacteristicRead: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //Log.d("onCharacteristicChanged", "");

            if(onCharacteristicRead != null) {
                onCharacteristicRead.deploy(characteristic.getUuid(), characteristic.getValue());
            }

            byte[] value = characteristic.getValue();
            //Log.d("TAG", "onCharacteristicChanged: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
        }
    };
}
