package bike.hackboy.bronco.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import bike.hackboy.bronco.BuildConfig;
import bike.hackboy.bronco.data.Command;
import bike.hackboy.bronco.data.Uuid;

public class FlashWriter {
	private final Context context;
	private final BroadcastReceiver messageReceiver;

	private int progress = 0;

	public FlashWriter(Context c) {
		this.context = c;

		messageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String event = intent.getStringExtra("event");
				String uuid = intent.getStringExtra("uuid");
				byte[] value = intent.getByteArrayExtra("value");

				switch(progress) {
					case 0:
						if(
							event.equals("on-characteristic-read")
							&& uuid.equals(Uuid.characteristicSettingsReadString)
							&& Converter.byteArrayToHexString(value).equals("011001FF00013005")
						) {
							//Log.w("flash_writer", "stage 1");
							closeFlash();
							progress = 1;
						}
					break;
					case 1:
						if(!event.equals("on-characteristic-write")) return;
						//Log.w("flash_writer", "before stage 2");
						//Log.d("flash_writer_cmd", Converter.byteArrayToHexString(value));

						if(
							event.equals("on-characteristic-write")
								&& uuid.equals(Uuid.characteristicSettingsWriteString)
								&& Converter.byteArrayToHexString(value).equals(Converter.byteArrayToHexString(Command.withChecksum(Command.CLOSE_FLASH)))
						) {
							//Log.w("flash_writer", "stage 2");
							progress = 2;
							end();
						}
					break;
				}
			}
		};
	}

	public void run() throws IllegalStateException {
		if(progress > 0) {
			throw new IllegalStateException("This writer has already run");
		}

		LocalBroadcastManager.getInstance(context)
			.registerReceiver(messageReceiver, new IntentFilter(BuildConfig.APPLICATION_ID));

		// GO GO GO
		//Log.w("flash_writer", "start");
		writeFlash();
	}

	protected void end() {
		//Log.w("flash_writer", "end");

		Intent intent = new Intent(BuildConfig.APPLICATION_ID);
		intent.putExtra("event", "toast");
		intent.putExtra("message", "Flash write OK");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		LocalBroadcastManager.getInstance(context)
			.unregisterReceiver(messageReceiver);
	}

	protected void writeFlash() {
		LocalBroadcastManager.getInstance(context)
			.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "write-flash"));
	}

	protected void closeFlash() {
		LocalBroadcastManager.getInstance(context)
			.sendBroadcast(new Intent(BuildConfig.APPLICATION_ID)
				.putExtra("event", "close-flash"));
	}
}
