package bike.hackboy.bronco.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import bike.hackboy.bronco.utils.Converter;

public class Gatt {

    public static void ensureHasCharacteristic(BluetoothGatt adapter, UUID serviceUuid, UUID characteristicUuid) throws Exception {
        List<BluetoothGattService> serviceList = adapter.getServices();

        for (BluetoothGattService service : serviceList) {
            if(service.getUuid().equals(serviceUuid)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d("characteristic", characteristic.getUuid().toString().toLowerCase());

                    if(characteristic.getUuid().equals(characteristicUuid)) {
                        Log.d("has_characteristic", "characteristic found");
                        return;
                    }
                }
            }
        }

        throw new Exception("characteristic/service combination not found");
    }

    public static void enableNotifications(BluetoothGatt adapter, UUID serviceUuid, UUID characteristicUuid) {
        BluetoothGattService service = adapter. getService(serviceUuid);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

        for (BluetoothGattDescriptor descriptor : descriptors) {
            Log.d("descriptor", descriptor.getUuid().toString());

            BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(descriptor.getUuid());
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            adapter.writeDescriptor(clientConfig);

            adapter.setCharacteristicNotification(characteristic, true);
        }
    }

    public static void writeCharacteristic(BluetoothGatt adapter, UUID serviceUuid, UUID characteristicUuid, byte[] data) throws Exception {
        String debugCommand = Converter.byteArrayToHexString(data);
        Log.d("gatt_cmd_before_write", debugCommand);

        BluetoothGattService service = adapter.getService(serviceUuid);

        if (service == null) {
            Log.e("gatt", "service not found");
            throw new Exception("service not found");
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        characteristic.setValue(data);

        boolean success = adapter.writeCharacteristic(characteristic);
        if(!success) throw new Exception("write failed");
    }

    public static void requestReadCharacteristic(BluetoothGatt adapter, UUID serviceUuid, UUID characteristicUuid) throws Exception {
        BluetoothGattService service = adapter.getService(serviceUuid);

        if (service == null) {
            Log.e("gatt", "service not found");
            throw new Exception("service not found");
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        if (characteristic == null) {
            Log.e("gatt", "characteristic not found");
            throw new Exception("characteristic not found");
        }

        adapter.readCharacteristic(characteristic);
    }

    public static String getDeviceMacByName(BluetoothAdapter adapter, String name) throws Exception {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName().trim();
                Log.d("device found", deviceName);

                if(deviceName.equals(name)) {
                    return device.getAddress();
                }
            }
        }

        throw new Exception("could not find device by name");
    }
}
