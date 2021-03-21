package bike.hackboy.bronco.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bike.hackboy.bronco.data.Uuid;

public class NotificationEnabler {
	protected List<notificationRequestBean> list = new ArrayList<notificationRequestBean>();
	protected BluetoothGatt adapter = null;

	private static final int THROTTLE_MILIS = 200;
	private long lastCommandTime = 0;

	public static class notificationRequestBean implements Serializable {
		private UUID service;
		private UUID characteristic;

		public UUID getService() {
			return service;
		}

		public void setService(UUID service) {
			this.service = service;
		}

		public UUID getCharacteristic() {
			return characteristic;
		}

		public void setCharacteristic(UUID characteristic) {
			this.characteristic = characteristic;
		}

		@Override
		public String toString() {
			return "notificationRequestBean{" +
				"service=" + service +
				", characteristic=" + characteristic +
				'}';
		}
	}

	public void setConnection(BluetoothGatt adapter) {
		this.adapter = adapter;
	}

	public void add(UUID service, UUID characteristic) {
		notificationRequestBean request = new notificationRequestBean();
		request.setCharacteristic(characteristic);
		request.setService(service);

		this.list.add(request);
	}

	public void run() throws Exception {
		if(adapter == null) throw new Exception("adapter not set");
		if(list.isEmpty()) throw new Exception("nothing to run");

		Thread thread = new Thread(() -> {
			do {
				if(lastCommandTime + THROTTLE_MILIS > System.currentTimeMillis()) {
					continue;
				}

				lastCommandTime = System.currentTimeMillis();
				notificationRequestBean item = list.get(0);
				//Log.d("notification_enabler_loop", item.toString());

				if (enableNotifications(item.getService(), item.getCharacteristic())) {
					list.remove(0);
				}
			} while (!list.isEmpty());

			end();
		});
		thread.start();
	}

	public void end() {
		//Log.d("notification_enabler", "completed");
	}

	protected boolean enableNotifications(UUID serviceUuid, UUID characteristicUuid) {
		BluetoothGattService service = adapter.getService(serviceUuid);
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

		//Log.d("notification_enabler", service.getUuid().toString());
		//Log.d("notification_enabler", characteristic.getUuid().toString());

		adapter.setCharacteristicNotification(characteristic, true);
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(Uuid.notificationDescriptor);

		descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

		return adapter.writeDescriptor(descriptor);
	}
}
