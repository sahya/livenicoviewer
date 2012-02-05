package jp.android.sahya.NicoLiveViewer;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NicoVideoView {
	private final static String CONNECT_URL = "http://sp.live.nicovideo.jp/";
	private String _loadUrl = "";
	private WebView webview = null;
	private PlayerStatusData _playerStatusData = null;
	private CookieStore cookieStore = null;
	private CookieSyncManager cookieSyncManager = null;
	private CookieManager cookieManager = null;

	public NicoVideoView(WebView webview, CookieStore cookieStore) {
		this(webview, cookieStore, CONNECT_URL);
	}
	public NicoVideoView(WebView webview, CookieStore cookieStore, String loadUrl) {
		this.webview = webview;
		this.cookieStore = cookieStore;
		this._loadUrl = loadUrl;
		
		WebSettings settings=webview.getSettings();
		settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		settings.setBlockNetworkImage(true);
		
		cookieSyncManager = CookieSyncManager.createInstance(this.webview.getContext());
		cookieSyncManager.startSync();

		this.webview.setWebViewClient(new NicoWebViewClient());
	}
	public void loadUrl(){
		loadUrl(this._loadUrl);
	}
	public void loadUrl(String loadUrl){
		this.webview.loadUrl(loadUrl);
	}
	
	class NicoWebViewClient extends WebViewClient{
		String loginCookie = null;
		
		public NicoWebViewClient (){
			setCookieManeger();
    		setCookie();
		}
	
    	@Override
    	public void onLoadResource(WebView wv, String url){
    		setCookieManeger();
    		setCookie();
    	}
    	@Override
    	public void onPageFinished(WebView wv, String url){
    		setCookieManeger();
    		cookieManager.setCookie("nicovideo.jp", loginCookie);
    	}
    	
    	private void setCookie(){
    		Cookie cookie = null;
    		if ( cookieStore != null ) {
    			List<Cookie> cookies = cookieStore.getCookies();
    			if (!cookies.isEmpty()) {
    				for (int i = 0; i < cookies.size(); i++) {
    					cookie = cookies.get(i);
    				}
    			}
    			if (cookie != null) {
    				loginCookie = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
    				cookieManager.setCookie( "nicovideo.jp", loginCookie);
    				cookieSyncManager.sync();
    			}
    		}
    	}
    	
    	private void setCookieManeger(){
    		cookieManager = CookieManager.getInstance();
    		cookieManager.setAcceptCookie(true);
    		cookieManager.removeExpiredCookie();
    	}
    }
}
