package apps.droidnotify.preferences;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import apps.droidnotify.common.Common;
import apps.droidnotify.log.Log;
import apps.droidnotify.R;

/**
 * The ImageArrayAdapterRounded is the array adapter used for displaying an image with a list preference item.
 * 
 * @author Camille S�vigny
 */
public class ImageArrayAdapterRounded extends ArrayAdapter<CharSequence> {
	
	//================================================================================
    // Properties
    //================================================================================

	private boolean _debug = false;
	private LayoutInflater _inflater = null;
	private int _index = 0;
	private int[] _resourceIds = null;

	//================================================================================
	// Constructors
	//================================================================================
	
	/**
	 * ImageArrayAdapterRounded constructor.
	 * 
	 * @param context - Context.
	 * @param textViewResourceId - Resource id of the text view.
	 * @param objects - Objects to be displayed.
	 * @param ids - Ids resource id of the images to be displayed.
	 * @param i - Index of the previous selected item.
	 */
	public ImageArrayAdapterRounded(Context context, int textViewResourceId, CharSequence[] objects, int[] ids, int i) {
		super(context, textViewResourceId, objects);
	    _debug = Log.getDebug();
		if (_debug) Log.v("ImageArrayAdapterRounded.ImageArrayAdapterRounded()");
		_inflater = ((Activity)context).getLayoutInflater();
		_index = i;
		_resourceIds = ids;
	}

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Returns a view.
	 * 
	 * @param position - int
	 * @param view - View
	 * @param parent - ViewGroup
	 */
	public View getView(int position, View currentView, ViewGroup parent) {
		if (_debug) Log.v("ImageArrayAdapterRounded.getView()");
		final ViewHolder viewHolder;
		if (currentView == null) {
			currentView = _inflater.inflate(R.layout.listitem, parent, false);
			// Creates a ViewHolder and store references to the children views we want to bind data to.
			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) currentView.findViewById(R.id.image);
			viewHolder.checkedTextView = (CheckedTextView) currentView.findViewById(R.id.check);
			// Store in tag
			currentView.setTag(viewHolder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView and CheckBox
			viewHolder = (ViewHolder) currentView.getTag();
		}
		//Set the data for the Views.
		viewHolder.imageView.setImageBitmap(Common.getRoundedCornerBitmap(BitmapFactory.decodeResource(getContext().getResources(), _resourceIds[position]), 5, false, 0, 0));
		viewHolder.checkedTextView.setText(getItem(position));
		if (position == _index) {
			viewHolder.checkedTextView.setChecked(true);
		} else {
			viewHolder.checkedTextView.setChecked(false);
		}
		return currentView;
	}
	
	// View holder to references to the views
	private static class ViewHolder {
		public ImageView imageView;
		public CheckedTextView checkedTextView;
	}
	
}
