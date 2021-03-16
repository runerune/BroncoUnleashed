package bike.hackboy.bronco.hal;

import android.app.NotificationManager;
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

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.UUID;

import bike.hackboy.bronco.BuildConfig;
import bike.hackboy.bronco.MainActivity;
import bike.hackboy.bronco.R;
import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;
import bike.hackboy.bronco.gatt.Gatt;
import bike.hackboy.bronco.utils.Converter;

public class BikeService extends Service {
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothGatt connection = null;
	private NotificationCompat.Builder notification = null;

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

					updateNotification(intent);
					BikeService.this.notify("disconnected");
				break;

				case "connect":
					try {
						String mac = Gatt.getDeviceMacByName(bluetoothAdapter, "COWBOY");
						BikeService.this.toast(String.format("Found %s", mac));

						BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

						Handler handler = new Handler(Looper.getMainLooper());
						handler.post(() -> connection = device.connectGatt(getApplicationContext(), false, mGattCallback));

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

						//Log.d("gatt_command", Converter.byteArrayToHexString(changedCommandWithChecksum));

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, changedCommandWithChecksum);
						BikeService.this.toast("Success");
					} catch (Exception e) {
						BikeService.this.toast("Failed to change speed");
						Log.e("read_lock", "failed to change speed", e);
					}
				break;

				case "reset-speed":
					Log.w("cmd", "reset_speed");
					try {
						if (connection == null) {
							throw new Exception("not connected");
						}

						byte[] changedCommandWithChecksum = Command.withChecksum(
							Command.withValue(Command.SET_SPEED, 25)
						);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsPcb, changedCommandWithChecksum);
						BikeService.this.toast("Success");
					} catch (Exception e) {
						BikeService.this.toast("Failed to reset speed");
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

				case "dashboard_notification":
					// don't pollute service with dashboard parsing logic, just accept
					// whatever and put it in the notification that we are forced to have anyway
					updateNotification(intent);
				break;
			}
		}
	};

	private void updateNotification(Intent intent) {
		String status;

		if (intent.getStringExtra("event").equals("disconnect")) {
			status = (String) getText(R.string.not_connected);
		} else {
			boolean isLocked = intent.getBooleanExtra("locked", true);
			String uptime = intent.getStringExtra("uptime");
			String distance = intent.getStringExtra("distance");

			if(isLocked) {
				status = (String) getText(R.string.bike_is_locked);
			} else {
				status = String.format(
					"%s • %s %s • %s %s",
					R.string.unlocked,
					getText(R.string.uptime),
					uptime,
					getText(R.string.distance),
					distance
				);
			}
		}

		NotificationManager nm = getSystemService(NotificationManager.class);
		notification.setContentTitle(status);
		nm.notify(666, notification.build());
	}

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

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent bringAppToFrontPendingIntent =
			PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification = new NotificationCompat.Builder(this, "default")
			.setContentText(getText(R.string.service_is_running))
			.setContentTitle(getText(R.string.not_connected))
			.setSmallIcon(R.drawable.ic_fg3)
			.setContentIntent(bringAppToFrontPendingIntent);

		startForeground(666, notification.build());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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