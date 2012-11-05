package apps.droidnotify.sms;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import apps.droidnotify.R;
import apps.droidnotify.common.Common;

/**
 * This is the "SMS Customize" applications preference Activity.
 * 
 * @author Camille S�vigny
 */
public class SMSCustomizePreferenceActivity extends PreferenceActivity{
	
	//================================================================================
	// Public Methods
	//================================================================================

	/**
	 * Called when the activity is created. Set up views and buttons.
	 * 
	 * @param bundle - Activity bundle.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle bundle){
	    super.onCreate(bundle);
	    Common.setApplicationLanguage(getApplicationContext(), this);
	    this.addPreferencesFromResource(R.xml.sms_customize_preferences);
	    this.setContentView(R.layout.customize_preferences);
	}
	
}