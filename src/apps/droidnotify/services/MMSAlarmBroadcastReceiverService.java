package apps.droidnotify.services;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import apps.droidnotify.common.Common;
import apps.droidnotify.common.Constants;
import apps.droidnotify.log.Log;
import apps.droidnotify.receivers.MMSAlarmReceiver;

public class MMSAlarmBroadcastReceiverService extends WakefulIntentService {
	
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
	public MMSAlarmBroadcastReceiverService() {
		super("MMSAlarmBroadcastReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("MMSAlarmBroadcastReceiverService.MMSAlarmBroadcastReceiverService()");
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
		if (_debug) Log.v("MMSAlarmBroadcastReceiverService.doWakefulWork()");
		try{
			Context context = getApplicationContext();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			//Read preferences and exit if app is disabled.
		    if(!preferences.getBoolean(Constants.APP_ENABLED_KEY, true)){
				if (_debug) Log.v("MMSAlarmBroadcastReceiverService.doWakefulWork() App Disabled. Exiting...");
				return;
			}
			//Block the notification if it's quiet time.
			if(Common.isQuietTime(context)){
				if (_debug) Log.v("MMSAlarmBroadcastReceiverService.doWakefulWork() Quiet Time. Exiting...");
				return;
			}
			//Read preferences and exit if MMS notifications are disabled.
		    if(!preferences.getBoolean(Constants.MMS_NOTIFICATIONS_ENABLED_KEY, true)){
				if (_debug) Log.v("MMSAlarmBroadcastReceiverService.doWakefulWork() MMS Notifications Disabled. Exiting...");
				return;
			}
		    //Check the state of the users phone.
			TelephonyManager telemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		    boolean notificationIsBlocked = false;
		    boolean rescheduleNotification = true;
		    boolean callStateIdle = telemanager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
		    boolean blockingAppRunning = Common.isBlockingAppRunning(context);
		    String blockingAppRuningAction = preferences.getString(Constants.MMS_BLOCKING_APP_RUNNING_ACTION_KEY, Constants.BLOCKING_APP_RUNNING_ACTION_SHOW);
		    //Reschedule notification based on the users preferences.
		    if(!callStateIdle){
		    	notificationIsBlocked = true;		    	
		    	if(preferences.getBoolean(Constants.IN_CALL_RESCHEDULING_ENABLED_KEY, false)){
		    		rescheduleNotification = true;
		    	}else{
		    		rescheduleNotification = false;
		    	}
		    }else if(blockingAppRuningAction.equals(Constants.BLOCKING_APP_RUNNING_ACTION_RESCHEDULE) && blockingAppRunning){ 
		    	//Blocking App is running.
		    	notificationIsBlocked = true;
		    }else if(blockingAppRuningAction.equals(Constants.BLOCKING_APP_RUNNING_ACTION_SHOW)){
		    	notificationIsBlocked = false;
		    }
		    if(!notificationIsBlocked){
				WakefulIntentService.sendWakefulWork(context, new Intent(context, MMSService.class));
		    }else{	    	
		    	//Display the Status Bar Notification even though the popup is blocked based on the user preferences.
		    	if(preferences.getBoolean(Constants.MMS_STATUS_BAR_NOTIFICATIONS_SHOW_WHEN_BLOCKED_ENABLED_KEY, true)){
			    	//Get the mms message info.
					String messageAddress = null;
					String messageBody = null;
					String contactName = null;
		    		ArrayList<String> mmsArray = Common.getMMSMessagesFromDisk(context);
		    		if((mmsArray != null) && (mmsArray.size() > 0)){
			    		String mmsArrayItem = mmsArray.get(0);
						String[] mmsInfo = mmsArrayItem.split("\\|");
		    			int arraySize = mmsInfo.length;
		    			if(arraySize > 0){
							if(arraySize >= 1) messageAddress = mmsInfo[0];
							if(arraySize >= 2) messageBody = mmsInfo[1];
							if(arraySize >= 7) contactName = mmsInfo[6];
		    			}
		    		}
					//Display Status Bar Notification
				    Common.setStatusBarNotification(context, Constants.NOTIFICATION_TYPE_MMS, 0, callStateIdle, contactName, messageAddress, messageBody, null);
			    }
		    	//Ignore notification based on the users preferences.
		    	if(blockingAppRuningAction.equals(Constants.BLOCKING_APP_RUNNING_ACTION_IGNORE)){
		    		rescheduleNotification = false;
		    		return;
		    	}
		    	if(rescheduleNotification){
			    	// Set alarm to go off x minutes from the current time as defined by the user preferences.
			    	long rescheduleInterval = Long.parseLong(preferences.getString(Constants.RESCHEDULE_BLOCKED_NOTIFICATION_TIMEOUT_KEY, Constants.RESCHEDULE_BLOCKED_NOTIFICATION_TIMEOUT_DEFAULT)) * 60 * 1000;
		    		if (_debug) Log.v("MMSAlarmBroadcastReceiverService.doWakefulWork() Rescheduling notification. Rechedule in " + rescheduleInterval + "minutes.");					
					String intentActionText = "apps.droidnotify.alarm/MMSAlarmReceiverAlarm/" + String.valueOf(System.currentTimeMillis());
					long alarmTime = System.currentTimeMillis() + rescheduleInterval;
					Common.startAlarm(context, MMSAlarmReceiver.class, null, intentActionText, alarmTime);
		    	}
		    }
		}catch(Exception ex){
			if (_debug) Log.e("MMSAlarmBroadcastReceiverService.doWakefulWork() ERROR: " + ex.toString());
		}
	}
		
}