package jp.android.sahya.NicoLiveViewer;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class NicoVideoView {
	private final String CONNECT_URL = "http://sp.live.nicovideo.jp/";
	private WebView webview = null;
	private PlayerStatusData _playerStatusData = null;
	private CookieStore cookieStore = null;
	private CookieSyncManager cookieSyncManager = null;
	private CookieManager cookieManager = null;

	public NicoVideoView(WebView webview, CookieStore cookieStore) {
		this.webview = webview;
		this.cookieStore = cookieStore;
		
		WebSettings settings=webview.getSettings();
		settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		settings.setBlockNetworkImage(true);
		
		cookieSyncManager = CookieSyncManager.createInstance(this.webview.getContext());
		cookieSyncManager.startSync();
		cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeExpiredCookie();
		
		setCookie();
		this.webview.loadUrl(CONNECT_URL);
	}
	
	private void setCookie(){
		Cookie cookie = null;
		if ( this.cookieStore != null ) {
			List<Cookie> cookies = this.cookieStore.getCookies();
			if (!cookies.isEmpty()) {
				for (int i = 0; i < cookies.size(); i++) {
					cookie = cookies.get(i);
				}
			}
			if (cookie != null) {
				String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
				cookieManager.setCookie( "nicovideo.jp", cookieString);
				cookieSyncManager.getInstance().sync();
			}
		}
	}
}
