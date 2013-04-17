package com.splunk.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LoggingService extends Service {

	private static boolean running = false;

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {

		super.onCreate();

		SplunkLogger.setContext(this.getBaseContext());
		SplunkLogger.setIndex("android");

		new Logger().start();
		running = true;

	}

	class Logger extends Thread {

		public void run() {

			while (running) {

				SplunkLogger.logLocation();
				SplunkLogger.logTelephony();
				SplunkLogger.logWifi();
				SplunkLogger.logActivity();

				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		running = false;
	}

}
