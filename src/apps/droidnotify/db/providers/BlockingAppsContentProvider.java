package apps.droidnotify.db.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import apps.droidnotify.db.DBConstants;
import apps.droidnotify.db.SQLiteHelperBlockingApps;

public class BlockingAppsContentProvider extends ContentProvider{

    private static final int URI_MATCH = 1;
	
	private Context _context = null;
    private SQLiteHelperBlockingApps _dbHelper;
    private UriMatcher _uriMatcher;
    private static HashMap<String, String> _projectionMap;

    @Override
    public boolean onCreate(){
    	_context = this.getContext();
    	_dbHelper = new SQLiteHelperBlockingApps(_context);
    	//Setup URIMatcher.
    	_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	_uriMatcher.addURI(DBConstants.AUTHORITY_BLOCKINGAPPS, DBConstants.TABLE_NAME_BLOCKINGAPPS, URI_MATCH);
    	//Setup Projection Map.
    	_projectionMap = new HashMap<String, String>();
    	_projectionMap.put(DBConstants.COLUMN_ID, DBConstants.COLUMN_ID);
    	_projectionMap.put(DBConstants.COLUMN_PACKAGE, DBConstants.COLUMN_PACKAGE);
    	return true;
    }

    @Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (_uriMatcher.match(uri)){
            case URI_MATCH:
                qb.setTables(DBConstants.TABLE_NAME_BLOCKINGAPPS);
                qb.setProjectionMap(_projectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(_context.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues){
        if (_uriMatcher.match(uri) != URI_MATCH){ throw new IllegalArgumentException("Unknown URI: " + uri); }
        ContentValues values;
        if(initialValues == null){
            values = new ContentValues();
        }else{
            values = new ContentValues(initialValues);
        }
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        long rowId = db.insert(DBConstants.TABLE_NAME_BLOCKINGAPPS, DBConstants.COLUMN_PACKAGE, values);
        if(rowId > 0){
            Uri currentUri = ContentUris.withAppendedId(DBConstants.CONTENT_URI_BLOCKINGAPPS, rowId);
            _context.getContentResolver().notifyChange(currentUri, null);
            db.close();
            return currentUri;
        }
        throw new SQLException("Failed to insert row into: " + uri);
    }

    @Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs){
    	SQLiteDatabase db = _dbHelper.getWritableDatabase();
		int count;
        switch (_uriMatcher.match(uri)){
            case URI_MATCH:{
                count = db.update(DBConstants.TABLE_NAME_BLOCKINGAPPS, values, where, whereArgs);
                break;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        _context.getContentResolver().notifyChange(uri, null);
        db.close();
        return count;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs){
        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        int count;
        switch(_uriMatcher.match(uri)){
            case URI_MATCH:{
                count = db.delete(DBConstants.TABLE_NAME_BLOCKINGAPPS, where, whereArgs);
                break;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        _context.getContentResolver().notifyChange(uri, null);
        db.close();
        return count;
    }
    
    @Override
    public String getType(Uri uri){
        switch (_uriMatcher.match(uri)){
        	case URI_MATCH:{
                return DBConstants.CONTENT_TYPE_BLOCKINGAPPS;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }	
    }
    
}