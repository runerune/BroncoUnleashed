package bike.hackboy.bronco.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class SequencedWriter {
	protected List<WriteRequestBean> list = new ArrayList<>();
	protected BluetoothGatt adapter = null;

	private static final int THROTTLE_MILIS = 1000;
	private long lastCommandTime = 0;

	public static class WriteRequestBean implements Serializable {
		private UUID service;
		private UUID characteristic;
		private byte[] data;

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

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
			return "WriteRequestBean{" +
				"service=" + service +
				", characteristic=" + characteristic +
				", data=" + Arrays.toString(data) +
				'}';
		}
	}

	public void setConnection(BluetoothGatt adapter) {
		this.adapter = adapter;
	}

	public void add(UUID service, UUID characteristic, byte[] data) {
		SequencedWriter.WriteRequestBean request = new SequencedWriter.WriteRequestBean();
		request.setCharacteristic(characteristic);
		request.setService(service);
		request.setData(data);

		this.list.add(request);
	}

	public void run() throws Exception {
		if(adapter == null) throw new Exception("adapter not set");
		if(list.isEmpty()) throw new Exception("nothing to run");

		do {
			if(lastCommandTime + THROTTLE_MILIS > System.currentTimeMillis()) {
				continue;
			}

			lastCommandTime = System.currentTimeMillis();

			SequencedWriter.WriteRequestBean item = list.get(0);
			Log.d("writer_loop", item.toString());

			boolean success = write(item.getService(), item.getCharacteristic(), item.getData());
			if (success) list.remove(0);
		} while (!list.isEmpty());

		end();
	}

	public void end() {
		Log.d("writer", "completed");
	}

	protected boolean write(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
		BluetoothGattService service = adapter.getService(serviceUuid);
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
		characteristic.setValue(data);

		return adapter.writeCharacteristic(characteristic);
	}
}
