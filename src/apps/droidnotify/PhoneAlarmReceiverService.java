package apps.droidnotify;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * This class handles scheduled Missed Call notifications that we want to display.
 * 
 * @author Camille S�vigny
 */
public class PhoneAlarmReceiverService extends WakefulIntentService {

	//================================================================================
    // Constants
    //================================================================================
	
	private static final int NOTIFICATION_TYPE_PHONE = 0;
	private static final int MISSED_CALL_TYPE = android.provider.CallLog.Calls.MISSED_TYPE;
	
	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	
	//================================================================================
	// Constructors
	//================================================================================
	
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor
	 */
	public PhoneAlarmReceiverService() {
		super("PhoneAlarmReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("PhoneAlarmReceiverService.PhoneAlarmReceiverService()");
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Display the notification for this Missed Call.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("PhoneAlarmReceiverService.doWakefulWork()");
		ArrayList<String> missedCallsArray = getMissedCalls();
		if(missedCallsArray.size() > 0){
			Context context = getApplicationContext();
			Bundle bundle = new Bundle();
			bundle.putInt("notificationType", NOTIFICATION_TYPE_PHONE);
			bundle.putStringArrayList("missedCallsArrayList", missedCallsArray);
	    	Intent phoneNotificationIntent = new Intent(context, NotificationActivity.class);
	    	phoneNotificationIntent.putExtras(bundle);
	    	phoneNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	    	context.startActivity(phoneNotificationIntent);
		}else{
			if (_debug) Log.v("PhoneAlarmReceiverService.doWakefulWork() No missed calls were found. Exiting...");
		}
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Function to query the call log and check for any missed calls.
	 * 
	 * @return ArrayList<String> - Returns an ArrayList of Strings that contain the missed call information.
	 */
	private ArrayList<String> getMissedCalls(){
		if (_debug) Log.v("PhoneAlarmReceiverService.getMissedCalls()");
		ArrayList<String> missedCallsArray = new ArrayList<String>();
		final String[] projection = null;
		final String selection = null;
		final String[] selectionArgs = null;
		final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
		Cursor cursor = null;
		try{
		    cursor = getApplicationContext().getContentResolver().query(
		    		Uri.parse("content://call_log/calls"),
		    		projection,
		    		selection,
					selectionArgs,
					sortOrder);
	    	while (cursor.moveToNext()) { 
	    		String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
	    		String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
	    		String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
	    		String isCallNew = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
	    		if(Integer.parseInt(callType) == MISSED_CALL_TYPE && Integer.parseInt(isCallNew) > 0){
    				if (_debug) Log.v("PhoneAlarmReceiverService.getMissedCalls() Missed Call Found: " + callNumber);
    				//Store missed call numbers and dates in an array.
    				missedCallsArray.add(callNumber + "|" + callDate);
    				//TODO - Add preferences to decide what to do here. Check ALL calls or just recent calls or just the latest calls?
    				//if(){
    					break;
    				//}
    			}else{
    				break;
    			}
	    	}
		}catch(Exception ex){
			if (_debug) Log.e("PhoneAlarmReceiverService.getMissedCalls() ERROR: " + ex.toString());
		}finally{
			cursor.close();
		}
	    return missedCallsArray;
	}
	
}