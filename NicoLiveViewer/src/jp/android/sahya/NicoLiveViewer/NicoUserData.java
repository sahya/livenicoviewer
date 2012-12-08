package jp.android.sahya.NicoLiveViewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NicoUserData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2968280631104666217L;
	public String UserID;
	public String UserName;
	public NicoUserData(String userID, String userName){
		UserID = userID;
		UserName = userName;
	}
	@Override
	public String toString(){
		return String.format("%1$s : %2$s", UserID, UserName);
	}
	
	public static class NicoUserDataList extends ArrayList<Map.Entry> {
		public NicoUserDataList(HashMap<String, NicoUserData> nicoUserData){
			super(nicoUserData.entrySet());
		}
		public List<Map.Entry> sort(){
			Collections.sort(this, new ComparatorList());
			return this;
		}
	}
	
	private static class ComparatorList implements Comparator<Map.Entry> {
		public int compare(Entry lhs, Entry rhs) {
			Integer e1 = Integer.valueOf(lhs.getKey().toString());
			Integer e2 = Integer.valueOf(rhs.getKey().toString());
			return e1.compareTo(e2);
		}
	}
}
