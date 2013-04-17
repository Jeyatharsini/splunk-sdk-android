/*
 * Copyright 2012 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk.android;

import java.util.List;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

/**
 * This class has several utility methods for logging events from Android and also
 * logging useful Android device metrics (wifi, telephony, battery etc...)
 * 
 * @author ddallimore
 *
 */
public class SplunkLogger {

	private static String SOURCE = "splogger";
	private static String INDEX = "main";

	private static boolean alreadyloggingBattery = false;

	public static void setSource(String source) {
		SOURCE = source;
	}

	public static void setIndex(String index) {
		INDEX = index;
	}

	private static SplunkLogEvent addAndroidDeviceFields(SplunkLogEvent event) {

		event.addPair("android_brand", android.os.Build.BRAND);
		event.addPair("android_device", android.os.Build.DEVICE);
		event.addPair("android_display", android.os.Build.DISPLAY);
		event.addPair("android_host", android.os.Build.HOST);
		event.addPair("android_id", android.os.Build.ID);
		event.addPair("android_manufacturer", android.os.Build.MANUFACTURER);
		event.addPair("android_model", android.os.Build.MODEL);
		event.addPair("android_product", android.os.Build.PRODUCT);
		event.addPair("android_serial", android.os.Build.SERIAL);
		event.addPair("android_user", android.os.Build.USER);

		return event;
	}

	public static void log(String message) {

		SplunkLogEvent event = new SplunkLogEvent();
		event.addPair("message", message);
		addAndroidDeviceFields(event);
		Splunk.getInstance().logEvent(event, "logevent", SOURCE, INDEX);

	}

	public static void logLocation() {

		LocationManager locationManager = (LocationManager) baseContext
				.getSystemService(Context.LOCATION_SERVICE);

		Location location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location == null)
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (location == null)
			return;

		SplunkLogEvent event = new SplunkLogEvent();
		event.addPair("lat", location.getLatitude());
		event.addPair("long", location.getLongitude());
		event.addPair("speed", location.getSpeed());
		event.addPair("time", location.getTime());
		event.addPair("bearing", location.getBearing());
		event.addPair("altitude", location.getAltitude());
		event.addPair("accuracy", location.getAccuracy());
		addAndroidDeviceFields(event);
		Splunk.getInstance().logEvent(event, "location", SOURCE, INDEX);

	}

	/**
	 * Only need to call this once , the listener will handle it from there
	 * 
	 * @param activity
	 */
	public static void logBattery() {

		if (!alreadyloggingBattery) {
			baseContext.registerReceiver(new BatteryInfoReceiver(),
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			alreadyloggingBattery = true;
		}
	}

	static class BatteryInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			SplunkLogEvent event = new SplunkLogEvent();

			int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);

			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			boolean present = intent.getExtras().getBoolean(
					BatteryManager.EXTRA_PRESENT);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			String technology = intent.getExtras().getString(
					BatteryManager.EXTRA_TECHNOLOGY);
			int temperature = intent.getIntExtra(
					BatteryManager.EXTRA_TEMPERATURE, 0);
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

			event.addPair("health", health);

			event.addPair("level", level);
			event.addPair("plugged", plugged);
			event.addPair("present", present);
			event.addPair("scale", scale);
			event.addPair("status", status);
			event.addPair("technology", technology);
			event.addPair("temperature", temperature);
			event.addPair("voltage", voltage);

			addAndroidDeviceFields(event);
			Splunk.getInstance().logEvent(event, "battery", SOURCE, INDEX);

		}

	}

	public static void logTelephony() {

		TelephonyManager telephonyManager = (TelephonyManager) baseContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (telephonyManager == null)
			return;

		SplunkLogEvent event = new SplunkLogEvent();
		event.addPair("call_state", telephonyManager.getCallState());
		event.addPair("data_activity", telephonyManager.getDataActivity());
		event.addPair("data_state", telephonyManager.getDataState());
		event.addPair("device_id", telephonyManager.getDeviceId());
		event.addPair("device_soft_version",
				telephonyManager.getDeviceSoftwareVersion());
		event.addPair("line_number", telephonyManager.getLine1Number());
		event.addPair("network_country_iso",
				telephonyManager.getNetworkCountryIso());
		event.addPair("network_operator", telephonyManager.getNetworkOperator());
		event.addPair("network_operator_name",
				telephonyManager.getNetworkOperatorName());
		event.addPair("network_type", telephonyManager.getNetworkType());
		event.addPair("phone_type", telephonyManager.getPhoneType());
		event.addPair("sim_country_iso", telephonyManager.getSimCountryIso());
		event.addPair("sim_operator", telephonyManager.getSimOperator());
		event.addPair("sim_operator_name",
				telephonyManager.getSimOperatorName());
		event.addPair("sim_serial_number",
				telephonyManager.getSimSerialNumber());
		event.addPair("sim_state", telephonyManager.getSimState());
		event.addPair("subscriber_id", telephonyManager.getSubscriberId());

		addAndroidDeviceFields(event);

		Splunk.getInstance().logEvent(event, "telephony", SOURCE, INDEX);

	}

	public static void logWifi() {

		WifiManager wifiManager = (WifiManager) baseContext
				.getSystemService(Context.WIFI_SERVICE);

		if (wifiManager == null)
			return;

		SplunkLogEvent event = new SplunkLogEvent();

		event.addPair("state", wifiManager.getWifiState());
		event.addPair("enabled", wifiManager.isWifiEnabled());

		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		event.addPair("dhcp_dns1", dhcp.dns1);
		event.addPair("dhcp_dns2", dhcp.dns2);
		event.addPair("dhcp_gateway", dhcp.gateway);
		event.addPair("dhcp_ip", dhcp.ipAddress);
		event.addPair("dhcp_lease_duration", dhcp.leaseDuration);
		event.addPair("dhcp_server_address", dhcp.serverAddress);
		event.addPair("dhcp_netmask", dhcp.netmask);

		WifiInfo info = wifiManager.getConnectionInfo();
		event.addPair("bssid", info.getBSSID());
		event.addPair("ip", info.getIpAddress());
		event.addPair("link_speed", info.getLinkSpeed());
		event.addPair("mac_address", info.getMacAddress());
		event.addPair("network_id", info.getNetworkId());
		event.addPair("ssid", info.getSSID());
		event.addPair("rssi", info.getRssi());

		addAndroidDeviceFields(event);

		Splunk.getInstance().logEvent(event, "wifi", SOURCE, INDEX);

	}

	public static void logActivity() {

		ActivityManager activityManager = (ActivityManager) baseContext
				.getSystemService(Context.ACTIVITY_SERVICE);

		if (activityManager == null)
			return;

		SplunkLogEvent event = new SplunkLogEvent();

		List<ActivityManager.RunningAppProcessInfo> apps = activityManager
				.getRunningAppProcesses();

		StringBuffer runningApps = new StringBuffer();
		for (ActivityManager.RunningAppProcessInfo app : apps) {
			runningApps.append(app.processName).append(",");
		}
		event.addPair("running_apps", runningApps.toString());

		addAndroidDeviceFields(event);

		Splunk.getInstance().logEvent(event, "activity", SOURCE, INDEX);

	}

	private static Context baseContext;

	public static void setContext(Context context) {
		baseContext = context;

	}

}
