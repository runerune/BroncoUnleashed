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

			try {
				switch (event) {
					//<editor-fold desc="connection">
					case "disconnect":
						if (connection != null) {
							connection.close();
							connection.disconnect();
						}

						updateNotification(intent);
						BikeService.this.notify("disconnected");
					break;

					case "connect":
						String mac = Gatt.getDeviceMacByName(bluetoothAdapter, "COWBOY");
						BikeService.this.toast(String.format("Connecting...", mac));

						BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);

						Handler handler = new Handler(Looper.getMainLooper());
						handler.post(() -> connection = device.connectGatt(getApplicationContext(), false, mGattCallback));
					break;
					//</editor-fold>

					//<editor-fold desc="lights">
					case "lights-off":
						byte[] lightOffCommand = Command.withChecksum(Command.LIGHT_OFF);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, lightOffCommand);
					break;

					case "lights-on":
						byte[] lightOnCommand = Command.withChecksum(Command.LIGHT_ON);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, lightOnCommand);

					break;
					//</editor-fold>

					//<editor-fold desc="lock">
					case "read-lock":
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.requestReadCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
					break;

					case "lock":
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.LOCK);
					break;

					case "unlock":
						Gatt.ensureHasCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.writeCharacteristic(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock, Command.UNLOCK);
					break;
					//</editor-fold>

					//<editor-fold desc="speed">
					case "set-speed":
						int newSpeedValue = intent.getIntExtra("value", 25);

						byte[] setSpeedCommand = Command.withValue(Command.SET_SPEED, newSpeedValue);
						byte[] setSpeedCommandWithChecksum = Command.withChecksum(setSpeedCommand);

						//Log.d("gatt_command", Converter.byteArrayToHexString(changedCommandWithChecksum));

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, setSpeedCommandWithChecksum);
					break;

					case "reset-speed":
						if (connection == null) {
							throw new Exception("not connected");
						}

						byte[] resetSpeedCommandWithChecksum = Command.withChecksum(
							Command.withValue(Command.SET_SPEED, 25)
						);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, resetSpeedCommandWithChecksum);
						BikeService.this.toast("Success");
					break;

					case "read-speed":
						byte[] readSpeedCommandWithChecksum = Command.withChecksum(Command.READ_SPEED);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, readSpeedCommandWithChecksum);
					break;
					//</editor-fold>

					//<editor-fold desc="motor">
					case "read-motor-mode":
						byte[] readMotorModeCommand = Command.withChecksum(Command.READ_MOTOR_MODE);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, readMotorModeCommand);
					break;

					case "set-motor-mode-torque":
						byte[] setModeTorqueCommand = Command.withChecksum(Command.SET_MOTOR_MODE_TORQUE);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, setModeTorqueCommand);
					break;

					case "set-motor-mode-torque-with-limit":
						byte[] setModeTorqueWithLimit = Command.withChecksum(Command.SET_MOTOR_MODE_TORQUE_WITH_LIMIT);

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, setModeTorqueWithLimit);
					break;
					//</editor-fold>

					//<editor-fold desc="flash">
					case "write-flash":
						byte[] writeFlashCommand = Command.withChecksum(Command.WRITE_FLASH);
						//Log.d("gatt_command", Converter.byteArrayToHexString(writeFlashCommand));

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, writeFlashCommand);
					break;

					case "close-flash":
						byte[] closeFlashCommand = Command.withChecksum(Command.CLOSE_FLASH);
						//Log.d("gatt_command", Converter.byteArrayToHexString(closeFlashCommand));

						Gatt.ensureHasCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite);
						Gatt.writeCharacteristic(connection, Uuid.serviceSettings, Uuid.characteristicSettingsWrite, closeFlashCommand);
					break;
					//</editor-fold>

					case "enable-notifications":
						Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicUnlock);
						Gatt.enableNotifications(connection, Uuid.serviceUnlock, Uuid.characteristicDashboard);
						Gatt.enableNotifications(connection, Uuid.serviceSettings, Uuid.characteristicSettingsRead);
					break;

					case "dashboard_notification":
						// don't pollute service with dashboard parsing logic, just accept
						// whatever and put it in the notification that we are forced to have anyway
						updateNotification(intent);
					break;
				}
			} catch (Exception e) {
				Log.e("cmd_fail", e.getMessage(), e);
			}
		}
	};

	private void updateNotification(Intent intent) {
		String status;
		String subStatus;

		if (intent.getStringExtra("event").equals("disconnect")) {
			status = (String) getText(R.string.not_connected);
			subStatus = (String) getText(R.string.service_is_running);
		} else {
			boolean isLocked = intent.getBooleanExtra("locked", true);
			String uptime = intent.getStringExtra("uptime");
			String distance = intent.getStringExtra("distance");
			String battery = intent.getStringExtra("battery");

			if(isLocked) {
				status = (String) getText(R.string.bike_is_locked);
				subStatus = (String) getText(R.string.service_is_running);
			} else {
				status = String.format(
					"%s • %s %s",
					getText(R.string.unlocked),
					battery,
					getText(R.string.battery)
				);

				subStatus = String.format("%s %s • %s %s",
					getText(R.string.uptime),
					uptime,
					distance,
					getText(R.string.cycled)
				);
			}
		}

		NotificationManager nm = getSystemService(NotificationManager.class);
		notification.setContentTitle(status);
		notification.setContentText(subStatus);
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

	private void notifyCharacteristicWrite(UUID uuid, byte[] value) {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "on-characteristic-write");
		intent.putExtra("uuid", uuid.toString().toUpperCase());
		intent.putExtra("value", value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	private void notifyCharacteristicRead(UUID uuid, byte[] value) {
		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "on-characteristic-read");
		intent.putExtra("uuid", uuid.toString().toUpperCase());
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
			.setSmallIcon(R.drawable.ic_unleashed)
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
				byte[] value = characteristic.getValue();
				notifyCharacteristicRead(characteristic.getUuid(), value);

				//Log.d("gatt", "onCharacteristicRead: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//Log.d("onCharacteristicWrite", String.valueOf(status));
				byte[] value = characteristic.getValue();
				notifyCharacteristicWrite(characteristic.getUuid(), value);

				//Log.d("gatt", "onCharacteristicWrite: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			//Log.d("onCharacteristicChanged", characteristic.getUuid().toString());
			byte[] value = characteristic.getValue();
			notifyCharacteristicRead(characteristic.getUuid(), value);

			//Log.d("gatt", "onCharacteristicRead: " + Converter.byteArrayToHexString(value) + " UUID " + characteristic.getUuid().toString() );
		}
	};
}