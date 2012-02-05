package jp.android.sahya.NicoLiveViewer;

import java.util.List;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NicoWebView {
	private final static String CONNECT_URL = "http://sp.live.nicovideo.jp/";
	private String _loadUrl = "";
	private WebView webview = null;
	private CookieSyncManager cookieSyncManager = null;
	private CookieManager cookieManager = null;
	private WebViewClient client = null;
	private String _loginCookie =null;
	
	public NicoWebView(String loginCookie) {
		this._loginCookie = loginCookie;
	}
	public NicoWebView(String loginCookie, WebView webview) {
		this._loginCookie = loginCookie;
		this._loadUrl = CONNECT_URL;
		setWebView(webview);
	}
	public NicoWebView(CookieStore cookieStore) {
		this(null, cookieStore);
	}
	public NicoWebView(WebView webview, CookieStore cookieStore) {
		this(webview, cookieStore, CONNECT_URL);
	}
	public NicoWebView(WebView webview, CookieStore cookieStore, String loadUrl) {
		this._loginCookie = getLoginCookie(cookieStore);
		this._loadUrl = loadUrl;
		//
		if (webview == null){ return; }
		
		setWebView(webview);
	}
	//
	private void setWebView(WebView webview){
		this.webview = webview;
		
		WebSettings settings=webview.getSettings();
		settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		settings.setBlockNetworkImage(true);
		
		cookieSyncManager = CookieSyncManager.createInstance(this.webview.getContext());
		cookieSyncManager.startSync();

		this.client = new NicoWebViewClient();
		this.webview.setWebViewClient(this.client);
	}
	public WebViewClient getWebViewClient(){
		return this.client;
	}
	public void loadUrl(){
		loadUrl(this._loadUrl);
	}
	public void loadUrl(String loadUrl){
		this.webview.loadUrl(loadUrl);
	}
	public String getLoginCookie(){
		return this._loginCookie;
	}
	public String getLoginCookie(CookieStore cookieStore){
		if ( cookieStore != null ) {
			List<Cookie> cookies = cookieStore.getCookies();
			if (!cookies.isEmpty()) {
				for (int i = 0; i < cookies.size(); i++) {
					if(isNicoVideoUserSession(cookies.get(i))){
						Cookie cookie = cookies.get(i);
						return cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
					}
				}
			}
		}
		return null;
	}
	private boolean isNicoVideoUserSession(Cookie cookie){
		if (cookie != null) {
			if (cookie.getDomain().equals(".nicovideo.jp") && cookie.getName().equals("user_session")){
				return true;
			}
		}
		return false;
	}
	
	/*
	 *    NicoWebViewClient
	 */
	class NicoWebViewClient extends WebViewClient {
		
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
    		setCookie();
    	}
    	
    	private void setCookie(){
    		cookieManager.setCookie( "nicovideo.jp", _loginCookie);
    		cookieSyncManager.sync();
    	}
    	
    	private void setCookieManeger(){
    		cookieManager = CookieManager.getInstance();
    		cookieManager.setAcceptCookie(true);
    		cookieManager.removeExpiredCookie();
    	}
    }
}