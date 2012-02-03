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
}
