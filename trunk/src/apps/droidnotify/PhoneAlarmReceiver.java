package apps.droidnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import apps.droidnotify.log.Log;

/**
 * This class listens for scheduled Missed Call notifications that we want to display.
 * 
 * @author Camille S�vigny
 */
public class PhoneAlarmReceiver extends BroadcastReceiver {
	
	//================================================================================
    // Properties
    //================================================================================

	private boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * This function starts the service that will handle the work or reschedules the work if the phone is in use.
	 * 
	 * @param context - Application Context.
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("PhoneAlarmReceiver.onReceive()");
		try{
			WakefulIntentService.acquireStaticLock(context);
		    Intent phoneAlarmBroadcastReceiverServiceIntent = new Intent(context, PhoneAlarmBroadcastReceiverService.class);
		    phoneAlarmBroadcastReceiverServiceIntent.putExtras(intent.getExtras());
			context.startService(phoneAlarmBroadcastReceiverServiceIntent);
		}catch(Exception ex){
			if (_debug) Log.e("PhoneAlarmReceiver.onReceive() ERROR: " + ex.toString());
		}
	}

}