package jp.android.sahya.NicoLiveViewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.os.Environment;
import android.app.Activity;


public class NicoFile {
	private final String FileNotFound = "ƒtƒ@ƒCƒ‹‚ª‚Ý‚Â‚©‚è‚Ü‚¹‚ñ";
	private String errStatus = null;
	private String saveFileName = null;
	
	public NicoFile(String saveFileName){
		this.saveFileName = saveFileName;
	}
	public String getErrorStatus()
	{
		return errStatus;
	}
	
	public boolean saveFile(Activity activity, Object saveData){
		ObjectOutputStream oos = null;
		
		try {
			oos = new ObjectOutputStream(activity.openFileOutput(saveFileName, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			errStatus = FileNotFound;
			return false;
		} catch (IOException e) {
			return false;
		}
		
		errStatus = "";
		return saveFile(oos, saveData);
	}
	/**
	 * @param saveFileOutputStream =new ObjectOutputStream(openFileOutput(saveFileName, Context.MODE_PRIVATE));
	 */
	public boolean saveFile(ObjectOutputStream saveFileOutputStream, Object saveData){
		try
		{			
			saveFileOutputStream.writeObject(saveData);
			saveFileOutputStream.flush();
			saveFileOutputStream.close();            
		}catch(IOException e){
			return false;
		}
		
		errStatus = "";
		return true;
	}
	/**
	 * FileInputStream fis = activity.openFileInput(filename);
	 * @return NicoInfoData
	 */
	public Object openFile(FileInputStream fis){
		Object fileData;
		
		try{
			ObjectInputStream ois = new ObjectInputStream(fis);
			fileData = ois.readObject();
			ois.close();
		}catch(IOException e){
			this.errStatus = "";
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
		
        return fileData;
	}
	
	/**
	 * AndroidManifest.xml‚É’Ç‰Á
	 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
	 */
	public boolean saveFileAtSDcard(){
		String sdCardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		errStatus = "";
		return true;
	}
	
	public boolean canReadFile(Activity activity){
		if (!activity.getFileStreamPath(saveFileName).exists()){ return false; }
		if (activity.getFileStreamPath(saveFileName).canRead()){ return true; }
		return false;
	}
}
