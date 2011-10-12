package apps.droidnotify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import apps.droidnotify.common.Common;
import apps.droidnotify.common.Constants;
import apps.droidnotify.log.Log;

public class RescheduleBroadcastReceiverService extends WakefulIntentService {
	
	//================================================================================
    // Properties
    //================================================================================
	
	boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor.
	 */
	public RescheduleBroadcastReceiverService() {
		super("RescheduleBroadcastReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("RescheduleBroadcastReceiverService.RescheduleBroadcastReceiverService()");
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Do the work for the service inside this function.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		if (_debug) Log.v("RescheduleBroadcastReceiverService.doWakefulWork()");
		try{
			Context context = getApplicationContext();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			//Read preferences and exit if app is disabled.
		    if(!preferences.getBoolean(Constants.APP_ENABLED_KEY, true)){
				if (_debug) Log.v("RescheduleBroadcastReceiverService.doWakefulWork() App Disabled. Exiting...");
				return;
			}
		    Bundle bundle = intent.getExtras();
		    int notificationType = bundle.getInt("notificationType");
		    //Check the state of the users phone.
		    TelephonyManager telemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		    boolean rescheduleNotification = false;
		    boolean callStateIdle = telemanager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
		    boolean inMessagingApp = preferences.getBoolean(Constants.USER_IN_MESSAGING_APP_KEY, false);
		    boolean blockingAppRunning = Common.isBlockingAppRunning(context);
		    String blockingAppRuningAction = null;
		    boolean showBlockedNotificationStatusBarNotification = false;
		    switch(notificationType){
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_PHONE:{
			    	blockingAppRuningAction = preferences.getString(Constants.PHONE_BLOCKING_APP_RUNNING_ACTION_KEY, "0");
			    	showBlockedNotificationStatusBarNotification = preferences.getBoolean(Constants.PHONE_STATUS_BAR_NOTIFICATIONS_SHOW_WHEN_BLOCKED_ENABLED_KEY, true);
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_SMS:{
			    	blockingAppRuningAction = preferences.getString(Constants.SMS_BLOCKING_APP_RUNNING_ACTION_KEY, "0");
			    	showBlockedNotificationStatusBarNotification = preferences.getBoolean(Constants.SMS_STATUS_BAR_NOTIFICATIONS_SHOW_WHEN_BLOCKED_ENABLED_KEY, true);
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_MMS:{
			    	blockingAppRuningAction = preferences.getString(Constants.MMS_BLOCKING_APP_RUNNING_ACTION_KEY, "0");
			    	showBlockedNotificationStatusBarNotification = preferences.getBoolean(Constants.MMS_STATUS_BAR_NOTIFICATIONS_SHOW_WHEN_BLOCKED_ENABLED_KEY, true);
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_CALENDAR:{
			    	blockingAppRuningAction = preferences.getString(Constants.CALENDAR_BLOCKING_APP_RUNNING_ACTION_KEY, "0");
			    	showBlockedNotificationStatusBarNotification = preferences.getBoolean(Constants.CALENDAR_STATUS_BAR_NOTIFICATIONS_SHOW_WHEN_BLOCKED_ENABLED_KEY, true);
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_GMAIL:{
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_TWITTER:{
			    	break;
			    }
			    case Constants.NOTIFICATION_TYPE_RESCHEDULE_FACEBOOK:{
			    	break;
			    }
		    }
		    if(!callStateIdle || inMessagingApp){
		    	rescheduleNotification = true;
		    }else{	    	
		    	//Blocking App is running.
		    	if(blockingAppRunning){
		    		//Reschedule notification based on the users preferences.
				    rescheduleNotification = true;
		    	}
		    }
		    if(!rescheduleNotification){
				WakefulIntentService.acquireStaticLock(context);
				Intent rescheduleIntent = new Intent(context, RescheduleReceiverService.class);
				rescheduleIntent.putExtras(intent.getExtras());
				context.startService(rescheduleIntent);
		    }else{
		    	//Display the Status Bar Notification even though the popup is blocked based on the user preferences.
		    	if(showBlockedNotificationStatusBarNotification){
		    		//Get the notification info.
		    		Bundle rescheduleBundle = intent.getExtras();
		    		String[] rescheduleNotificationInfo = rescheduleBundle.getStringArray("rescheduleNotificationInfo");
					String sentFromAddress = rescheduleNotificationInfo[1];
					String messageBody = rescheduleNotificationInfo[2];
					String contactName = rescheduleNotificationInfo[6];
	    			String title = rescheduleNotificationInfo[8];
	    			//Display Status Bar Notification
					switch(notificationType){
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_PHONE:{
					    	Common.setStatusBarNotification(context, Constants.NOTIFICATION_TYPE_PHONE, callStateIdle, contactName, sentFromAddress, null);
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_SMS:{
					    	Common.setStatusBarNotification(context, Constants.NOTIFICATION_TYPE_SMS, callStateIdle, contactName, sentFromAddress, messageBody);
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_MMS:{
					    	Common.setStatusBarNotification(context, Constants.NOTIFICATION_TYPE_MMS, callStateIdle, contactName, sentFromAddress, messageBody);
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_CALENDAR:{
					    	Common.setStatusBarNotification(context, Constants.NOTIFICATION_TYPE_CALENDAR, callStateIdle, null, null, title);
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_GMAIL:{
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_TWITTER:{
					    	break;
					    }
					    case Constants.NOTIFICATION_TYPE_RESCHEDULE_FACEBOOK:{
					    	break;
					    }
					}
		    	}
		    	//Ignore notification based on the users preferences.
		    	if(blockingAppRuningAction.equals(Constants.BLOCKING_APP_RUNNING_ACTION_IGNORE)){
		    		return;
		    	}
		    	//Set alarm to go off x minutes from the current time as defined by the user preferences.
		    	if(preferences.getBoolean(Constants.RESCHEDULE_NOTIFICATIONS_ENABLED_KEY, true)){
			    	long rescheduleInterval = Long.parseLong(preferences.getString(Constants.RESCHEDULE_NOTIFICATION_TIMEOUT_KEY, "5")) * 60 * 1000;
		    		if (_debug) Log.v("RescheduleBroadcastReceiverService.doWakefulWork() Rescheduling notification. Rechedule in " + rescheduleInterval + "minutes.");
					AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
					Intent rescheduleIntent = new Intent(context, RescheduleReceiver.class);
					rescheduleIntent.putExtras(intent.getExtras());
					rescheduleIntent.setAction("apps.droidnotify.VIEW/RescheduleReschedule/" + System.currentTimeMillis());
					PendingIntent reschedulePendingIntent = PendingIntent.getBroadcast(context, 0, rescheduleIntent, 0);
					alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + rescheduleInterval, reschedulePendingIntent);
		    	}
		    }
		}catch(Exception ex){
			if (_debug) Log.e("RescheduleBroadcastReceiverService.doWakefulWork() ERROR: " + ex.toString());
		}
	}
		
}