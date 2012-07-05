package jp.android.sahya.NicoLiveViewer;

public final class PlayerStatusData {
	public static final String LIVEID = "id";
	private String _liveID;
	public static final String LIVE_STRAEM_URL = "url";
	private String _url;
	public static final String TICKET = "ticket";
	private String _ticket;
	public static final String CONTENTS_rtmp = "contents";
	private String _contents;
	public static final String TITLE = "title";
	private String _title;
	private String _datetime;
	public static final String default_communityID = "default_community";
	private String _communityID;
	public static final String CommunityName = "name";
	private String _communityName;
	public static final String OwnerID = "owner_id";
	private String _ownerID;
	public static final String UserID = "user_id";
	private String _userID;
	public static final String IS_PREMIUM = "is_premium";
	private String _is_premium;
	public static final String BASETIME = "base_time";
	private String _base_time;
	public static final String STARTTIME = "start_time";
	private String _start_time;
	public static final String THREAD = "thread";
	private String _thread;
	public static final String OwnerName = "owner_name";
	private String _ownerName;
	
	public PlayerStatusData(String liveID) {
		this._liveID = liveID;
	}
	public String getLiveID() {
		return _liveID;
	}
	public void setLiveID(String liveID) {
		this._liveID = liveID;
	}
	public String getUrl() {
		return _url;
	}
	public void setUrl(String url) {
		this._url = url;
	}
	public String getTicket() {
		return _ticket;
	}
	public void setTicket(String ticket) {
		this._ticket = ticket;
	}
	public String getContents() {
		return _contents;
	}
	public void setContents(String contents) {
		this._contents = contents;
	}
	public String getTitle() {
		 return this._title;
	}
	public void setTitle(String title) {
		this._title = title;
	}
	public String getDatetime() {
		 return this._datetime;
	}
	public void setDatetime(String datetime) {
		this._datetime = datetime;
	}
	public String getCommunityID() {
		 return this._communityID;
	}
	public void setCommunityID(String communityID) {
		this._communityID = communityID;
	}
	public String getCommunityName() {
		if (this._communityName == null || this._communityName.isEmpty()){
			return this._communityID;
		}
		else {
			return this._communityName;
		}
	}
	public void setCommunityName(String communityName) {
		this._communityName = communityName;
	}
	public String getOwnerID() {
		return _ownerID;
	}
	public void setOwnerID(String ownerID) {
		this._ownerID = ownerID;
	}
	public String getOwnerName() {
		if (this._ownerName == null || this._ownerName.isEmpty()){
			return _ownerID;
		}
		else {
			return _ownerName;
		}
	}
	public void setOwnerName(String ownerName) {
		this._ownerName = ownerName;
	}
	public String getUserID() {
		return _userID;
	}
	public void setIsPremium(String isPremium) {
		this._is_premium = isPremium;
	}
	public String getIsPremium() {
		return _is_premium;
	}
	public void setUserID(String userID) {
		this._userID = userID;
	}
	public String get_base_time() {
		return _base_time;
	}
	public void set_base_time(String base_time) {
		this._base_time = base_time;
	}
	public String get_start_time() {
		return _start_time;
	}
	public void set_start_time(String start_time) {
		this._start_time = start_time;
	}
	public String getThread() {
		return _thread;
	}
	public void setThread(String thread) {
		this._thread = thread;
	}
}
