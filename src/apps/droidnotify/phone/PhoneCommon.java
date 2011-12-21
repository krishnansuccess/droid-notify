package apps.droidnotify.phone;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;
import apps.droidnotify.NotificationActivity;
import apps.droidnotify.R;
import apps.droidnotify.common.Common;
import apps.droidnotify.common.Constants;
import apps.droidnotify.log.Log;

public class PhoneCommon {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private static boolean _debug = false;
	
	//================================================================================
	// Public Methods
	//================================================================================

	/**
	 * Function to query the call log and check for any missed calls.
	 * 
	 * @param context - The application context.
	 * 
	 * @return ArrayList<String> - Returns an ArrayList of Strings that contain the missed call information.
	 */
	public static ArrayList<String> getMissedCalls(Context context){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.getMissedCalls()");
		Boolean missedCallFound = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String missedCallPreference = preferences.getString(Constants.PHONE_DISMISS_BUTTON_ACTION_KEY, "0");
		ArrayList<String> missedCallsArray = new ArrayList<String>();
		final String[] projection = null;
		final String selection = null;
		final String[] selectionArgs = null;
		final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
		Cursor cursor = null;
		try{
		    cursor = context.getContentResolver().query(
		    		Uri.parse("content://call_log/calls"),
		    		projection,
		    		selection,
					selectionArgs,
					sortOrder);
	    	while (cursor.moveToNext()) { 
	    		String callLogID = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
	    		String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
	    		String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
	    		String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
	    		String isCallNew = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
	    		if(Integer.parseInt(callType) == Constants.PHONE_TYPE && Integer.parseInt(isCallNew) > 0){
    				if (_debug) Log.v("Common.getMissedCalls() Missed Call Found: " + callNumber);
    				String[] missedCallContactInfo = null;
    				if(isPrivateUnknownNumber(callNumber)){
    					if (_debug) Log.v("Common.getMissedCalls() Is a private or unknown number.");
    				}else{
    					missedCallContactInfo = Common.getContactsInfoByPhoneNumber(context, callNumber);
    				}
    				if(missedCallContactInfo == null){
    					missedCallsArray.add(callLogID + "|" + callNumber + "|" + callDate);
    				}else{
    					missedCallsArray.add(callLogID + "|" + callNumber + "|" + callDate + "|" + missedCallContactInfo[0] + "|" + missedCallContactInfo[1] + "|" + missedCallContactInfo[2] + "|" + missedCallContactInfo[3]);
    				}
    				if(missedCallPreference.equals(Constants.PHONE_GET_LATEST)){
    					if (_debug) Log.v("Common.getMissedCalls() Missed call found - Exiting");
    					break;
    				}
    				missedCallFound = true;
    			}else{
    				if(missedCallPreference.equals(Constants.PHONE_GET_RECENT)){
    					if (_debug) Log.v("Common.getMissedCalls() Found first non-missed call - Exiting");
    					break;
    				}
    			}
	    		if(!missedCallFound){
	    			if (_debug) Log.v("Common.getMissedCalls() Missed call not found - Exiting");
	    			break;
	    		}
	    	}
		}catch(Exception ex){
			Log.e("Common.getMissedCalls() ERROR: " + ex.toString());
		}finally{
			cursor.close();
		}
	    return missedCallsArray;
	}
	
	/**
	 * Delete a call long entry.
	 * 
	 * @param context - The current context of this Activity.
	 * @param callLogID - The call log ID that we want to delete.
	 * 
	 * @return boolean - Returns true if the call log entry was deleted successfully.
	 */
	public static boolean deleteFromCallLog(Context context, long callLogID){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.deleteFromCallLog()");
		try{
			if(callLogID == 0){
				if (_debug) Log.v("Common.deleteFromCallLog() Call Log ID == 0. Exiting...");
				return false;
			}
			String selection = android.provider.CallLog.Calls._ID + " = " + callLogID;
			String[] selectionArgs = null;
			context.getContentResolver().delete(
					Uri.parse("content://call_log/calls"),
					selection, 
					selectionArgs);
			return true;
		}catch(Exception ex){
			Log.e("Common.deleteFromCallLog() ERROR: " + ex.toString());
			return false;
		}
	}
	
	/**
	 * Mark a call log entry as being viewed.
	 * 
	 * @param context - The current context of this Activity.
	 * @param callLogID - The call log ID that we want to delete.
	 * 
	 * @return boolean - Returns true if the call log entry was updated successfully.
	 */
	public static boolean setCallViewed(Context context, long callLogID, boolean isViewed){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.setCallViewed()");
		try{
			if(callLogID == 0){
				if (_debug) Log.v("Common.setCallViewed() Call Log ID == 0. Exiting...");
				return false;
			}
			ContentValues contentValues = new ContentValues();
			if(isViewed){
				contentValues.put(android.provider.CallLog.Calls.NEW, 0);
			}else{
				contentValues.put(android.provider.CallLog.Calls.NEW, 1);
			}
			String selection = android.provider.CallLog.Calls._ID + " = " + callLogID;
			String[] selectionArgs = null;
			context.getContentResolver().update(
					Uri.parse("content://call_log/calls"),
					contentValues,
					selection, 
					selectionArgs);
			return true;
		}catch(Exception ex){
			Log.e("Common.setCallViewed() ERROR: " + ex.toString());
			return false;
		}
	}
	
	/**
	 * Place a phone call.
	 * 
	 * @param context - Application Context.
	 * @param notificationActivity - A reference to the parent activity.
	 * @param phoneNumber - The phone number we want to send a message to.
	 * @param requestCode - The request code we want returned.
	 * 
	 * @return boolean - Returns true if the application can be launched.
	 */
	public static boolean makePhoneCall(Context context, NotificationActivity notificationActivity, String phoneNumber, int requestCode){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.makePhoneCall()");
		try{
			if(phoneNumber == null){
				Toast.makeText(context, context.getString(R.string.app_android_phone_number_format_error), Toast.LENGTH_LONG).show();
				Common.setInLinkedAppFlag(context, false);
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_CALL);
	        intent.setData(Uri.parse("tel:" + phoneNumber));
	        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	        notificationActivity.startActivityForResult(intent, requestCode);
	        Common.setInLinkedAppFlag(context, true);
		    return true;
		}catch(Exception ex){
			Log.e("Common.makePhoneCall() ERROR: " + ex.toString());
			Toast.makeText(context, context.getString(R.string.app_android_phone_app_error), Toast.LENGTH_LONG).show();
			Common.setInLinkedAppFlag(context, false);
			return false;
		}
	}
	
	/**
	 * Start the intent to view the phones call log.
	 * 
	 * @param context - Application Context.
	 * @param notificationActivity - A reference to the parent activity.
	 * @param requestCode - The request code we want returned.
	 * 
	 * @return boolean - Returns true if the activity can be started.
	 */
	public static boolean startCallLogViewActivity(Context context, NotificationActivity notificationActivity, int requestCode){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.startCallLogViewActivity()");
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("vnd.android.cursor.dir/calls");
	        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			notificationActivity.startActivityForResult(intent, requestCode);
			Common.setInLinkedAppFlag(context, true);
			return true;
		}catch(Exception ex){
			Log.e("Common.startCallLogViewActivity() ERROR: " + ex.toString());
			Toast.makeText(context, context.getString(R.string.app_android_call_log_error), Toast.LENGTH_LONG).show();
			Common.setInLinkedAppFlag(context, false);
			return false;
		}
	}
	
	/**
	 * Remove all non-numeric items from the phone number.
	 * 
	 * @param phoneNumber - String of original phone number.
	 * 
	 * @return String - String of phone number with no formatting.
	 */
	public static String removeFormatting(String phoneNumber){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.removeFormatting()");
		phoneNumber = phoneNumber.replace("-", "");
		phoneNumber = phoneNumber.replace("+", "");
		phoneNumber = phoneNumber.replace("(", "");
		phoneNumber = phoneNumber.replace(")", "");
		phoneNumber = phoneNumber.replace(" ", "");
		return phoneNumber.trim();
	}
	
	/**
	 * Determines if the incoming number is a Private or Unknown number.
	 * 
	 * @param incomingNumber - The incoming phone number.
	 * 
	 * @return boolean - Returns true if the number is a Private number or Unknown number.
	 */
	public static boolean isPrivateUnknownNumber(String incomingNumber){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.isPrivateUnknownNumber() incomingNumber: " + incomingNumber);
		try{
			if(incomingNumber.length() > 4){
				return false;
			}
			int convertedNumber = Integer.parseInt(incomingNumber);
			if(convertedNumber < 1) return true;
		}catch(Exception ex){
			if (_debug) Log.v("Common.isPrivateUnknownNumber() Integer Parse Error");
			return false;
		}
		return false;
	}
	
	/**
	 * Cancel the stock missed call notification.
	 * 
	 * @return boolean - Returns true if the stock missed call notification was cancelled.
	 */
	public static boolean cancelStockMissedCallNotification(){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.cancelStockMissedCallNotification()");
		try{
	        Class serviceManagerClass = Class.forName("android.os.ServiceManager");
	        Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
	        Object phoneService = getServiceMethod.invoke(null, "phone");
	        Class ITelephonyClass = Class.forName("com.android.internal.telephony.ITelephony");
	        Class ITelephonyStubClass = null;
	        for(Class clazz : ITelephonyClass.getDeclaredClasses()){
	            if (clazz.getSimpleName().equals("Stub")){
	                ITelephonyStubClass = clazz;
	                break;
	            }
	        }
	        if (ITelephonyStubClass != null) {
	            Class IBinderClass = Class.forName("android.os.IBinder");
	            Method asInterfaceMethod = ITelephonyStubClass.getDeclaredMethod("asInterface", IBinderClass);
	            Object iTelephony = asInterfaceMethod.invoke(null, phoneService);
	            if (iTelephony != null){
	                Method cancelMissedCallsNotificationMethod = iTelephony.getClass().getMethod("cancelMissedCallsNotification");
	                cancelMissedCallsNotificationMethod.invoke(iTelephony);
	            }else{
	            	Log.e("Telephony service is null, can't call cancelMissedCallsNotification.");
	    	    	return false;
	            }
	        }else{
	            if (_debug) Log.v("Unable to locate ITelephony.Stub class.");
		    	return false;
	        }
	        return true;
	    }catch (Exception ex){
	    	Log.e("Common.cancelStockMissedCallNotification() ERROR: " + ex.toString());
	    	return false;
	    }
	}
	
	/**
	 * Function to format phone numbers.
	 * 
	 * @param context - The current context of this Activity.
	 * @param inputPhoneNumber - Phone number to be formatted.
	 * 
	 * @return String - Formatted phone number string.
	 */
	public static String formatPhoneNumber(Context context, String inputPhoneNumber){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.formatPhoneNumber()");
		try{
			if(inputPhoneNumber.equals("Private Number")){
				return inputPhoneNumber;
			}
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			inputPhoneNumber = removeFormatting(inputPhoneNumber);
			StringBuilder outputPhoneNumber = new StringBuilder("");		
			int phoneNumberFormatPreference = Integer.parseInt(preferences.getString(Constants.PHONE_NUMBER_FORMAT_KEY, Constants.PHONE_NUMBER_FORMAT_DEFAULT));
			String numberSeparator = "-";
			if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_6 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_7 | phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_8){
				numberSeparator = ".";
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_9 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_10 | phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_11){
				numberSeparator = " ";
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_4){
				numberSeparator = "";
			}
			if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_1 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_6 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_9){
				if(inputPhoneNumber.length() >= 10){
					//Format ###-###-#### (e.g.123-456-7890)
					//Format ###-###-#### (e.g.123.456.7890)
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 4, inputPhoneNumber.length()));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 7, inputPhoneNumber.length() - 4));
					outputPhoneNumber.insert(0, numberSeparator);
					if(inputPhoneNumber.length() == 10){
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 7));
					}else{
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 10, inputPhoneNumber.length() - 7));
						outputPhoneNumber.insert(0, numberSeparator);
						if(preferences.getBoolean(Constants.PHONE_NUMBER_FORMAT_10_DIGITS_ONLY_KEY , false)){
							outputPhoneNumber.insert(0, "0");
						}else{
							outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 10));
						}
					}
				}else{
					outputPhoneNumber.append(inputPhoneNumber);
				}
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_2 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_7 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_10){
				if(inputPhoneNumber.length() >= 10){
					//Format ##-###-##### (e.g.12-345-67890)
					//Format ##-###-##### (e.g.12.345.67890)
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 5, inputPhoneNumber.length()));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 8, inputPhoneNumber.length() - 5));
					outputPhoneNumber.insert(0, numberSeparator);
					if(inputPhoneNumber.length() == 10){
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 8));
					}else{
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 10, inputPhoneNumber.length() - 8));
						outputPhoneNumber.insert(0, numberSeparator);
						if(preferences.getBoolean(Constants.PHONE_NUMBER_FORMAT_10_DIGITS_ONLY_KEY , false)){
							outputPhoneNumber.insert(0, "0");
						}else{
							outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 10));
						}
					}
				}else{
					outputPhoneNumber.append(inputPhoneNumber);
				}
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_3 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_8 || phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_11){
				if(inputPhoneNumber.length() >= 10){
					//Format ##-##-##-##-## (e.g.12-34-56-78-90)
					//Format ##-##-##-##-## (e.g.12.34.56.78.90)
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 2, inputPhoneNumber.length()));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 4, inputPhoneNumber.length() - 2));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 6, inputPhoneNumber.length() - 4));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 8, inputPhoneNumber.length() - 6));
					outputPhoneNumber.insert(0, numberSeparator);
					if(inputPhoneNumber.length() == 10){
						outputPhoneNumber.insert(0,inputPhoneNumber.substring(0, inputPhoneNumber.length() - 8));
					}else{
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 10, inputPhoneNumber.length() - 8));
						outputPhoneNumber.insert(0, numberSeparator);
						if(preferences.getBoolean(Constants.PHONE_NUMBER_FORMAT_10_DIGITS_ONLY_KEY , false)){
							outputPhoneNumber.insert(0, "0");
						}else{
							outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 10));
						}
					}
				}else{
					outputPhoneNumber.append(inputPhoneNumber);
				}
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_4){
				//Format ########## (e.g.1234567890)
				outputPhoneNumber.append(inputPhoneNumber);
			}else if(phoneNumberFormatPreference == Constants.PHONE_NUMBER_FORMAT_5){
				if(inputPhoneNumber.length() >= 10){
					//Format (###) ###-#### (e.g.(123) 456-7890)
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 4, inputPhoneNumber.length()));
					outputPhoneNumber.insert(0, numberSeparator);
					outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 7, inputPhoneNumber.length() - 4));
					outputPhoneNumber.insert(0, ") ");
					if(inputPhoneNumber.length() == 10){
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 7));
						outputPhoneNumber.insert(0, "(");
					}else{
						outputPhoneNumber.insert(0, inputPhoneNumber.substring(inputPhoneNumber.length() - 10, inputPhoneNumber.length() - 7));
						outputPhoneNumber.insert(0, " (");
						if(preferences.getBoolean(Constants.PHONE_NUMBER_FORMAT_10_DIGITS_ONLY_KEY , false)){
							outputPhoneNumber.insert(0, "0");
						}else{
							outputPhoneNumber.insert(0, inputPhoneNumber.substring(0, inputPhoneNumber.length() - 10));
						}
					}
				}else{
					outputPhoneNumber.append(inputPhoneNumber);
				}
			}else{
				outputPhoneNumber.append(inputPhoneNumber);
			}
			return outputPhoneNumber.toString();
		}catch(Exception ex){
			Log.e("Common.formatPhoneNumber() ERROR: " + ex.toString());
			return inputPhoneNumber;
		}
	}
	
	/**
	 * Compares the two strings. 
	 * If the second string is larger and ends with the first string, return true.
	 * If the first string is larger and ends with the second string, return true.
	 * 
	 * @param contactNumber - The address books phone number.
	 * @param incomingNumber - The incoming phone number.
	 * 
	 * @return - boolean - 	 If the second string is larger ends with the first string, return true.
	 *                       If the first string is larger ends with the second string, return true.
	 */
	public static boolean isPhoneNumberEqual(String contactNumber, String incomingNumber){
		if (_debug) Log.v("Common.isPhoneNumberEqual()");
		//Remove any formatting from each number.
		contactNumber = removeFormatting(contactNumber);
		incomingNumber = removeFormatting(incomingNumber);
		//Remove any leading zero's from each number.
		contactNumber = removeLeadingZero(contactNumber);
		incomingNumber = removeLeadingZero(incomingNumber);	
		int contactNumberLength = contactNumber.length();
		int incomingNumberLength = incomingNumber.length();
		//Iterate through the ends of both strings...backwards from the end of the string.
		if(contactNumberLength <= incomingNumberLength){
			for(int i = 0; i < contactNumberLength; i++){
				if(contactNumber.charAt(contactNumberLength - 1 - i) != incomingNumber.charAt(incomingNumberLength - 1 - i)){
					return false;
				}
			}
		}else{
			for(int i = incomingNumberLength - 1; i >= 0 ; i--){
				if(contactNumber.charAt(contactNumberLength - 1 - i) != incomingNumber.charAt(incomingNumberLength - 1 - i)){
					return false;
				}
			}
		}
		return true;
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Remove the leading zero from a string.
	 * 
	 * @param inputNumber - The number to remove the leading zero from.
	 * 
	 * @return String - The number after we have removed the leading zero.
	 */
	private static String removeLeadingZero(String inputNumber){
		if (_debug) Log.v("Common.removeLeadingZero() InputNumber: " + inputNumber);
		if(inputNumber.subSequence(0, 1).equals("0")){
			return inputNumber.substring(1);
		}
		return inputNumber;
	}
	
}