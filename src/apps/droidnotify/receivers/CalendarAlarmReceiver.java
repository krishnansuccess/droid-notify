package apps.droidnotify.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import apps.droidnotify.log.Log;
import apps.droidnotify.services.CalendarAlarmReceiverService;
import apps.droidnotify.services.WakefulIntentService;
import apps.droidnotify.common.Constants;

/**
 * This class listens for scheduled notifications to check the users calendars.
 * 
 * @author Camille S�vigny
 */
public class CalendarAlarmReceiver extends BroadcastReceiver {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	  
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Receives a notification of a Calendar Event.
	 * This function starts the service that will handle the work or reschedules the work if the phone is in use.
	 * 
	 * @param context - Application Context.
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("CalendarAlarmReceiver.onReceive()");
		try{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			//Read preferences and exit if app is disabled.
			if(!preferences.getBoolean(Constants.APP_ENABLED_KEY, true)){
				if (_debug) Log.v("CalendarAlarmReceiver.onReceive() App Disabled. Exiting...");
				return;
			}
			//Read preferences and exit if calendar notifications are disabled.
		    if(!preferences.getBoolean(Constants.CALENDAR_NOTIFICATIONS_ENABLED_KEY, true)){
				if (_debug) Log.v("CalendarAlarmReceiver.onReceive() Calendar Notifications Disabled. Exiting... ");
				return;
			}
			WakefulIntentService.sendWakefulWork(context, new Intent(context, CalendarAlarmReceiverService.class));
		}catch(Exception ex){
			Log.e("CalendarAlarmReceiver.onReceive() ERROR: " + ex.toString());
		}
	}
	
}