package jp.android.sahya.NicoLiveViewer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;

import java.io.*;

public class NicoInfoProvider extends ContentProvider {
	private String filename = "test";
	private NicoInfoData nicoinfodata = new NicoInfoData();
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		if (!NicoInfoData.NICO_INFO_CONTENT_URI.equals(arg0)){
			throw new SecurityException();
		}
		nicoinfodata.mail = "".getBytes();
		nicoinfodata.password = "".getBytes();
		nicoinfodata.sessionCookie = "user_session".getBytes();
		nicoinfodata.lastUrl = NicoWebView.CONNECT_URL;
		nicoinfodata.isStore = false;
		
		return 1;
	}

	@Override
	public String getType(Uri uri) {
		if (!NicoInfoData.NICO_INFO_CONTENT_URI.equals(uri)){
			throw new SecurityException();
		}
		return "vnd.android.cursor.item/vnd.jp.android.sahya.provider";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (!NicoInfoData.NICO_INFO_CONTENT_URI.equals(uri)){
			throw new SecurityException();
		}
		nicoinfodata.mail = values.getAsString(NicoInfoData.MAIL).getBytes();
		nicoinfodata.password = values.getAsString(NicoInfoData.PASSWORD).getBytes();
		nicoinfodata.sessionCookie = values.getAsString(NicoInfoData.SESSION_COOKIE).getBytes();
		nicoinfodata.lastUrl = values.getAsString(NicoInfoData.LAST_URL);
		nicoinfodata.isStore = values.getAsBoolean(NicoInfoData.iS_STORE);
		
		try {
			ObjectOutputStream oos =new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(nicoinfodata);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
	    //enforcePermission(Binder.getCallingPid());
		if (!NicoInfoData.NICO_INFO_CONTENT_URI.equals(uri)){
			throw new SecurityException();
		}
		
		try
        {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            nicoinfodata = (NicoInfoData) ois.readObject();
            ois.close();
        }
        catch (Exception ex)
        {
            return null;
        }
		
		return new NicoInfoCursor();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (!NicoInfoData.NICO_INFO_CONTENT_URI.equals(uri)){
			throw new SecurityException();
		}
		
		nicoinfodata.mail = values.getAsString(NicoInfoData.MAIL).getBytes();
		nicoinfodata.password = values.getAsString(NicoInfoData.PASSWORD).getBytes();
		nicoinfodata.sessionCookie = values.getAsString(NicoInfoData.SESSION_COOKIE).getBytes();
		nicoinfodata.lastUrl = values.getAsString(NicoInfoData.LAST_URL);
		nicoinfodata.isStore = values.getAsBoolean(NicoInfoData.iS_STORE);
		
		try {
			ObjectOutputStream oos =new ObjectOutputStream(new FileOutputStream(""));
			oos.writeObject(nicoinfodata);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}

		return 1;
	}
	
	private String getPackageNameFromPid(int pid) {
	    ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningAppProcessInfo info : am.getRunningAppProcesses()) {
	        if (info.pid == pid) {
	            return info.processName;
	        }
	    }
	    
	    return null;
	}

	private void enforcePermission(int pid) {
	    String packageName = getPackageNameFromPid(pid);
	    if (!packageName.equals("jp.android.sahya.nicoinfoprovider")) {
	        throw new SecurityException();
	    }
	} 
	
	class NicoInfoCursor implements Cursor {

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deactivate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public byte[] getBlob(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnIndex(String columnName) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnIndexOrThrow(String columnName)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getColumnName(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getColumnNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public double getDouble(int columnIndex) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Bundle getExtras() {
			Bundle bundle = new Bundle();
			
			bundle.putByteArray(NicoInfoData.MAIL, nicoinfodata.mail);
			bundle.putByteArray(NicoInfoData.PASSWORD, nicoinfodata.password);
			bundle.putByteArray(NicoInfoData.SESSION_COOKIE, nicoinfodata.sessionCookie);
			bundle.putString(NicoInfoData.LAST_URL, nicoinfodata.lastUrl);
			bundle.putBoolean(NicoInfoData.iS_STORE, nicoinfodata.isStore);
			
			return bundle;
		}

		@Override
		public float getFloat(int columnIndex) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getInt(int columnIndex) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getLong(int columnIndex) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getPosition() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public short getShort(int columnIndex) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getString(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getWantsAllOnMoveCalls() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isAfterLast() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isBeforeFirst() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isClosed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isFirst() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isLast() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isNull(int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean move(int offset) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToFirst() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToLast() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToPosition(int position) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean moveToPrevious() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void registerContentObserver(ContentObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean requery() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Bundle respond(Bundle extras) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setNotificationUri(ContentResolver cr, Uri uri) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unregisterContentObserver(ContentObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
