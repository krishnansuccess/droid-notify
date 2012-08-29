package apps.droidnotify.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import apps.droidnotify.R;
import apps.droidnotify.common.Common;
import apps.droidnotify.log.Log;

public class ReschedulePreferenceActivity extends PreferenceActivity{
	
	//================================================================================
    // Properties
    //================================================================================

    private boolean _debug = false;
    private Context _context = null;
	
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
	    _debug = Log.getDebug();
	    if (_debug) Log.v("ReschedulePreferenceActivity.onCreate()");
	    _context = this;
	    Common.setApplicationLanguage(_context, this);
	    this.addPreferencesFromResource(R.xml.reschedule_preferences);
	    this.setContentView(R.layout.reschedule_preferences);
	}
	
}