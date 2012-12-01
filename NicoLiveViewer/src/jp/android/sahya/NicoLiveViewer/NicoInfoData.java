package jp.android.sahya.NicoLiveViewer;

import java.io.Serializable;

import android.net.Uri;

public class NicoInfoData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7093118036787392008L;
	
	public static final Uri NICO_INFO_CONTENT_URI = Uri.parse("content://jp.android.sahya/nicoliveviwer/nicoinfoprovider");
	public static final String MAIL = "mail";
	public static final String PASSWORD = "password";
	public static final String SESSION_COOKIE = "session_cookie";
	public static final String LAST_URL = "last_url";
	public static final String iS_STORE = "is_store";
	
	public byte[] mail;
	public byte[] password;
	public byte[] sessionCookie;
	public String lastUrl;
	public boolean isStore;
	
	public NicoInfoData(){
		this.lastUrl = NicoWebView.CONNECT_URL;
		this.isStore = false;
	}
}