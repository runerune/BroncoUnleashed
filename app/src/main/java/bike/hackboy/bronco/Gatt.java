package bike.hackboy.bronco;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Gatt {

    public static void ensureHasCharacteristic(BluetoothGatt adapter, UUID serviceUuid, UUID characteristicUuid) throws Exception {
        List<BluetoothGattService> serviceList = adapter.getServices();

        for (BluetoothGattService service : serviceList) {
            if(service.getUuid().equals(serviceUuid)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d("characteristic", characteristic.getUuid().toString().toLowerCase());

                    if(characteristic.getUuid().equals(characteristicUuid)) {
                        return;
                    }
                }
            }
        }

        throw new Exception("characteristic/service combination not found");
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
