package jp.android.sahya.NicoLiveViewer;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NicoWebView {
	public final static String CONNECT_URL = "http://sp.live.nicovideo.jp/";
	private String _loadUrl = "";
	private WebView webview = null;
	private CookieSyncManager cookieSyncManager = null;
	private CookieManager cookieManager = null;
	private WebViewClient client = null;
	private String _loginCookie =null;
	private Handler onPageStartedHandler = null;
	private Handler onPageFinishedHandler = null;
	public final static int ON_PAGE_STARTED = 0;
	public final static int ON_PAGE_FINISHED = 1;
	
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
		//settings.setBlockNetworkImage(true);
		
		cookieSyncManager = CookieSyncManager.createInstance(this.webview.getContext());
		cookieSyncManager.startSync();

		this.client = new NicoWebViewClient();
		this.webview.setWebViewClient(this.client);
	}
	
	/**
	 * ページの読み込みを通知するハンドラを登録する
	 * @param onPageStartedHandler
	 */
	public void setOnPageStartedHandler(final Handler onPageStartedHandler){
		this.onPageStartedHandler = onPageStartedHandler;
	}
	/**
	 * ページの読み込み完了を通知するハンドラを登録する
	 * @param onPageStartedHandler
	 */
	public void setOnPageFinishedHandler(final Handler onPageFinishedHandler){
		this.onPageFinishedHandler = onPageFinishedHandler;
	}
	public WebViewClient getWebViewClient(){
		return this.client;
	}
	public WebView getWebView(){
		return this.webview;
	}
	public void loadUrl(){
		loadUrl(this._loadUrl);
	}
	public void loadUrl(String loadUrl){
		this.webview.loadUrl(loadUrl);
	}
	public void loadData(String lodaData){
		this.webview.loadData(lodaData, "text/html", null);
	}
	public void loadDataWithBaseURL (String lodaData){
		this.webview.loadDataWithBaseURL("http://sp.live.nicovideo.jp/watch/" ,lodaData, "text/html", null, "http://sp.live.nicovideo.jp/");
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
						return cookie.getName() + "=" + cookie.getValue() + ";domain=" + cookie.getDomain() + ";Path=/";
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
	
	/**
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
    	
    	/** 
    	 * ページの読み込みを通知する
    	 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
    	 */
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon){
    		Message message = onPageStartedHandler.obtainMessage(ON_PAGE_STARTED, url);
    		onPageStartedHandler.sendMessage(message);
    	}
    	
    	/** 
    	 * ページの読み込み完了を通知する
    	 * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
    	 */
    	@Override
    	public void onPageFinished(WebView view, String url){
    		setCookieManeger();
    		setCookie();
    		if (onPageFinishedHandler != null){
    			Message message = onPageFinishedHandler.obtainMessage(ON_PAGE_FINISHED, url);
    			onPageFinishedHandler.sendMessage(message);
    		}
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