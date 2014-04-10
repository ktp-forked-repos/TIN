package pl.edu.pw.elka.tin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.edu.pw.elka.tin.activity.R;
import pl.edu.pw.elka.tin.activity.StartingActivity;
import pl.edu.pw.elka.tin.data.Zone;
import pl.edu.pw.elka.tin.network.HttpModule;
import pl.edu.pw.elka.tin.others.AppConstants;
import pl.edu.pw.elka.tin.others.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/*
 http://developer.android.com/guide/topics/location/obtaining-user-location.html
 */

/**
 * Listener, which checks if position is changed. Using GPS, BTS and Wi-Fi
 * signal process out user's coordinates. After each certain period of time,
 * notification is updated. If position changed, different state is set.
 * According to this state, different methods collection is called. <li>OK -
 * silent state, when service is idle</li><li>BAD - phone vibrates quickly,
 * makes sound, show red exclamation mark and notification to call user back to
 * an allowed zone</li><li>NOT_KNOW - vibration is slower, phone still sound but
 * notification show to user incompleteness of received data</li>
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class LocationService extends Service implements LocationListener {
	private Timer timer = new Timer();
	private NotificationManager notificationManager;
	private LocationManager locationManager;
	private String locationText = new String();
	private Location location;
	private UserLocationState userLocationState;
	private Location currentBestLocation;

	private final int NOTIFICATION_ID = 1335;

	private List<Zone> zoneList;

	public void onCreate() {
		super.onCreate();
		zoneList = new ArrayList<Zone>();
		TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		AppConstants.IMEI = telephonyManager.getDeviceId();
		Toast.makeText(getApplicationContext(), "Tracing started.",
				Toast.LENGTH_SHORT).show();
		locationManager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, AppConstants.MIN_TIME,
				AppConstants.MIN_DIST, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				AppConstants.MIN_TIME, AppConstants.MIN_DIST, this);

		userLocationState = UserLocationState.NOT_KNOWN;
		location = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			this.onLocationChanged(location);
		}
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		updateNotification();
	}

	private void updateNotification() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {

				Notification notification = new Notification(R.drawable.icon,
						locationText, System.currentTimeMillis());
				Intent notIntent = new Intent(getApplicationContext(),
						StartingActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						getApplicationContext(), 0, notIntent, 0);

				switch (userLocationState) {

				// __________# ICON # LIGHT # VIBRATION # SOUND # ACTION #
				// __________#______#_______#___________#_______#________#
				// ____OK____#_icon_#__NO___#__NO_______#__NO___#__??____#
				// ____BAD___#_warn_#_orange#__vibFast__#default#__??____#
				// NOT_KNOWN_#error_#_green_#__vibSlow__#default#__??____#

				case OK:
					// icon
					notification.icon = R.drawable.icon;

					// msg
					notification.setLatestEventInfo(getApplicationContext(),
							"Good boy, stay where you are!",
							"Kliknij aby cos tam.", contentIntent);

					// flags
					notification.flags |= Notification.FLAG_NO_CLEAR;
					notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

					notificationManager.notify(NOTIFICATION_ID, notification);

					break;

				case BAD:
					// icon
					notification.icon = R.drawable.warn;

					// vibrate
					long[] vibFast = { 0, 100, 100, 100, 100, 100, 100, 100,
							100, 100, 100, };
					notification.vibrate = vibFast;

					// flash
					notification.ledARGB = 0xFFFFFF00; // orange
					notification.ledOnMS = 500;
					notification.ledOffMS = 500;
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;

					// sound
					notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;

					// msg
					notification.setLatestEventInfo(getApplicationContext(),
							"Get back to work!", "Kliknij aby cos tam.",
							contentIntent);

					// flags
					notification.flags |= Notification.FLAG_NO_CLEAR;

					notification.flags |= Notification.FLAG_ONGOING_EVENT;
					// notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

					notificationManager.notify(NOTIFICATION_ID, notification);
					break;

				case NOT_KNOWN:
					// icon
					notification.icon = R.drawable.error;

					// vibrate
					long[] vibrateSlow = { 0, 1000 };
					notification.vibrate = vibrateSlow;

					// flash
					notification.ledARGB = 0xFF00FF00; // green
					notification.ledOnMS = 500;
					notification.ledOffMS = 500;
					notification.flags |= Notification.FLAG_SHOW_LIGHTS;

					// sound
					notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;

					// msg
					notification.setLatestEventInfo(getApplicationContext(),
							"Please wait to get accurate location",
							"Kliknij aby cos tam.", contentIntent);

					// flags
					notification.flags |= Notification.FLAG_NO_CLEAR;
					notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

					notificationManager.notify(NOTIFICATION_ID, notification);
					break;

				default:
					break;
				}

			} // run()
		}, 100, AppConstants.NOTIFICATION_UPDATE_INTERVAL);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
	}

	public void onLocationChanged(Location location) {
		Log.i("Position changed.");

		// checks if newer location is more accurate than previous one
		if (isBetterLocation(location, currentBestLocation))
			currentBestLocation = location;

		double lat = currentBestLocation.getLatitude(); // szerokosc - y
		double lon = currentBestLocation.getLongitude(); // dlugosc - x
		double acc = currentBestLocation.getAccuracy();

		HttpModule.sendReceiveData(lat, lon, acc, AppConstants.IMEI, zoneList);

		if (checkZones()) {
			return; // zones are incomplete
		}

		// check in each zone
		for (Zone zone : zoneList) {
			if (zone.pointInsideZone(lon, lat)) { // phone found in zone
				userInsideZones(lat, lon, acc);
				break;
			} else {
				userOutsideZones(lat, lon, acc);
			}
		}
	}

	/**
	 * Checks if allowed zones are correct.
	 * 
	 * @return TRUE if zones are not correct
	 */
	private synchronized boolean checkZones() {
		if (zoneList.isEmpty()) {
			userLocationState = UserLocationState.NOT_KNOWN;
			locationText = "Zones are empty";
			Log.w("zoneList are empty!");
			return true;
		}
		for (Zone z : zoneList) {
			if (z.getLocationList().size() < 3) {
				userLocationState = UserLocationState.NOT_KNOWN;
				locationText = "One of the zone is incomplete";
				Log.w("One of the zone is incomplete");
				return true;
			}
		}
		return false;

	}

	/**
	 * Sets flag and text if user is INside allowed zone.
	 * 
	 * @param lat
	 * @param lon
	 * @param acc
	 */
	private synchronized void userInsideZones(double lat, double lon, double acc) {
		Log.i("In allowed zone");
		userLocationState = UserLocationState.OK;
		locationText = String.format("OK! Lat: %.2f Long: %.2f Acc: %.2f", lat,
				lon, acc);
	}

	/**
	 * Sets flag and text if user is OUTside allowed zone.
	 * 
	 * @param lat
	 * @param lon
	 * @param acc
	 */
	private synchronized void userOutsideZones(double lat, double lon,
			double acc) {
		Log.i("Outside allowed zone.");
		userLocationState = UserLocationState.BAD;
		locationText = String.format("BAD! Lat: %.2f Long: %.2f Acc: %.2f",
				lat, lon, acc);
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	private boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > AppConstants.MIN_TIME;
		boolean isSignificantlyOlder = timeDelta < -AppConstants.MIN_TIME;
		boolean isNewer = timeDelta > 0;

		// If it's been more than one minute since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than one minute older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	// TODO Auto-generated method stub
	public void onProviderDisabled(String provider) {

	}

	public void onProviderEnabled(String provider) {

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
