package bike.hackboy.bronco;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.UUID;

import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.gatt.Gatt;
import bike.hackboy.bronco.utils.Converter;

public class BikeService extends Service {
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothGatt connection = null;
	private Notification notification = null;

	// --------------------------------------------------

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String event = intent.getStringExtra("event");
			//Log.d"event", event);

			switch (event) {
				case "disconnect":
					if (connection != null) {
						connection.close();
						connection.disconnect();
					}

					BikeService.this.notify("disconnected");
				break;

				case "connect":
					try {
						String mac = Gatt.getDeviceMacByName(bluetoothAdapter, "COWBOY");
						BikeService.this.toast(String.format("Found %s", mac));

						BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

						Handler handler = new Handler(Looper.getMainLooper());
						handler.post(new Runnable() {
							@Override
							public void run() {
									connection = device.connectGatt(getApplicationContext(), false, mGattCallback);
								}
							});

					} catch (Exception e) {
						Log.e("lookup_fail", e.getMessage());
						BikeService.this.toast("Could not find any bikes");
					}

				break;

				case "lights-off":
					try {
						byte[] command = Command.withChecksum(Command.LIGHT_OFF);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
					} catch (Exception e) {
						BikeService.this.toast("Failed to request lights off command");
						Log.e("read_lock", "failed to request lights off", e);
					}
				break;

				case "lights-on":
					try {
						byte[] command = Command.withChecksum(Command.LIGHT_ON);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, command);
					} catch (Exception e) {
						BikeService.this.toast("Failed to request lights on command");
						Log.e("read_lock", "failed to request lights on", e);
					}
				break;

				case "lock":
					try {
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.LOCK);
					} catch (Exception e) {
						BikeService.this.toast("Failed to request lock");
						Log.e("read_lock", "failed to request lock", e);
					}
				break;

				case "unlock":
					try {
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.UNLOCK);
					} catch (Exception e) {
						BikeService.this.toast("Failed to request unlock");
						Log.e("read_lock", "failed to request unlock", e);
					}
				break;

				case "set-speed":
					try {
						int value = intent.getIntExtra("value", 25);

						byte[] changedCommand = Command.withValue(Command.SET_SPEED, value);
						byte[] changedCommandWithChecksum = Command.withChecksum(changedCommand);

						Log.d("gatt_command", Converter.byteArrayToHexString(changedCommandWithChecksum));

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, changedCommandWithChecksum);
						BikeService.this.toast("Success");
					} catch (Exception e) {
						BikeService.this.toast("Failed to change speed");
						Log.e("read_lock", "failed to change speed", e);
					}
				break;

				case "enable-notifications":
					Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
					Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicDashboard);
				break;

				case "read-lock":
					try {
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.requestReadCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
					} catch (Exception e) {
						BikeService.this.toast("Failed to request read lock state");
						Log.e("read_lock", "failed to read lock state", e);
					}
				break;
			}
		}
	};

	private void toast(String message) {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "toast");
		intent.putExtra("message", message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void notify(String action) {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void notifyCharacteristicRead(UUID uuid, byte[] value) {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "on-characteristic-read");
		intent.putExtra("uuid", uuid.toString());
		intent.putExtra("value", value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void notifyDiscovery() {
		notify("on-discovered");
	}

	// --------------------------------------------------

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		LocalBroadcastManager.getInstance(this)
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		Intent notificationIntent = new Intent(this, BikeService.class);
		PendingIntent pendingIntent =
			PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification =
			new NotificationCompat.Builder(this, "default")
				.setContentTitle(getText(R.string.service_is_running))
				.setContentText("status goes here")
				.setSmallIcon(R.drawable.ic_fg3)
				.setContentIntent(pendingIntent)
				.build();

		startForeground(1, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
	}

	// --------------------------------------------------

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				gatt.discoverServices();
			}
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//Log.d("onServicesDiscovered", String.valueOf(status));
				notifyDiscovery();
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//Log.d("onCharacteristicRead", String.valueOf(status));
				notifyCharacteristicRead(characteristic.getUuid(), characteristic.getValue());

				//byte[] value = characteristic.getValue();
				//Log.d("TAG", "onCharacteristicRead: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			//Log.d("onCharacteristicChanged", characteristic.getUuid().toString());
			notifyCharacteristicRead(characteristic.getUuid(), characteristic.getValue());

			//byte[] value = characteristic.getValue();
			//Log.d("TAG", "onCharacteristicChanged: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
		}
	};
}