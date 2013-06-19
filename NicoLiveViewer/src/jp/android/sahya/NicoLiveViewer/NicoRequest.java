package jp.android.sahya.NicoLiveViewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class NicoRequest {
	//ログインAPI
	private final String _nicoHost = "secure.nicovideo.jp";
	private final String _nicoPath = "/secure/login";
	//認証API
	private final String _apiHost = "live.nicovideo.jp";
	private final String _getalertstatus = "/api/getalertstatus";
	//ユーザー認証を行わないAPI
	private final String _getalertinfo = "/api/getalertinfo";
	//番組情報取得API
	private final String _getstreaminfo = "/api/getstreaminfo/";
	//番組情報
	private final String _getplayerstatus = "/api/getplayerstatus";
	//getpostkey?thread=1187316452&block_no=0
	private final String _getpostkey = "/api/getpostkey?thread=%1$s&block_no=%2$s";
	//NGリストを取得[XML] (放送主のみ)
	private final String _configurengword = "/configurengword?mode=get&video=";
	//http://live.nicovideo.jp/api/getpublishstatus
	//NicoNiCommunity
	private final String _niconiCommunityHost = "com.nicovideo.jp";
	private final String _getCommunityInfo = "/community/";
	//http://www.nicovideo.jp/user/
	private final String _wwwhost = "www.nicovideo.jp";
	private final String _userid = "/user/";
	//heartbeat
	private final String _heartbeat = "/api/heartbeat?v=";

	private SchemeRegistry schemeRegistry = new SchemeRegistry();
	private HttpParams httpParams = new BasicHttpParams();
	private NicoMessage nicoMessage = null;
	//Login -> getplayerstatus
	private CookieStore _cookieStore;
	private String _loginCookie = "";
	private final Pattern _nicoLoginCookiePattern = Pattern.compile("user_session=user_session_.+;domain=.nicovideo.jp;Path=/");
	private final Pattern _nicoLoginCookieShortPattern = Pattern.compile("user_session_.+");

	//Alert server
	private String _alertaddr = null;
	private int _alertport;
	private String _alertthread = null;
	//getplayerstatus
	private PlayerStatusData _playerStatusData = null;
	private String _addr;
	private int _port;
	private String _thread;
	private String _ticket;
	private String _base_time;
	public String getTicket() {
		return _ticket;
	}
	public String getBaseTime() {
		return _base_time;
	}
	//Login
	private boolean _isLogin = false;
	//Login Alert
	private boolean _isLoginAlert = false;
	private NodeList _community_id = null;
	//
	private Document document = null;

	/**
	 * NicoRequest
	 * @param nicoMesssage NicoMessageのインスタンス
	 */
	public NicoRequest (NicoMessage nicoMesssage){
		this.nicoMessage = nicoMesssage;

		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);

		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", socketFactory, 443));

		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
	}
	/**
	 * ニコニコ動画にログイン成功
	 */
	public boolean isLogin(){
		return this._isLogin;
	}
	/**
	 * ニコ生放送アラートにログイン成功
	 */
	public boolean isLoginAlert(){
		return this._isLoginAlert;
	}
	public CookieStore getCookieStore(){
		return this._cookieStore;
	}

	/**
	 * ログインクッキーを返します
	 * @return 例）user_session=user_session_18180000_265723068462200000;domain=.nicovideo.jp;Path=/
	 */
	public String getLoginCookie(){
		if (isLogin() && _loginCookie.toString().equals("")){
			_loginCookie = getLoginCookie(this._cookieStore);
		}
		return _loginCookie;
	}
	/**
	 * ログインクッキーを設定します
	 * 例1）user_session=user_session_18180000_265723068462200000;domain=.nicovideo.jp;Path=/
	 * 例2）user_session_18180000_265723068462200000
	 */
	public void setLoginCookie(String loginCookie){
		Matcher matcher = _nicoLoginCookiePattern.matcher(loginCookie);
		if (matcher.matches()){
			setCookieStore(loginCookie.split(";")[0].split("=")[1]);
			setLoginCookie();
		}

		matcher = _nicoLoginCookieShortPattern.matcher(loginCookie);
		if (matcher.matches()){
			setCookieStore(loginCookie);
			setLoginCookie();
		}
	}
	private void setLoginCookie(){
		_loginCookie = getLoginCookie(this._cookieStore);
		_isLogin = true;
	}
	private void setCookieStore(String loginCookie){
		_cookieStore = new BasicCookieStore();
		_cookieStore.addCookie(getCookie(loginCookie));
	}
	private Cookie getCookie(String loginCookie){
		// Cookieを作成
		BasicClientCookie cookie = new BasicClientCookie("user_session", loginCookie);
		cookie.setDomain(".nicovideo.jp");
		cookie.setPath("/");

		return cookie;
	}
	/**
	 *
	 * @return HttpResponse Document
	 */
	public Document getHttpResponse(){
		return document;
	}

	/**
	 * ニコニコ動画へのログイン処理
	 * @param mail
	 * @param password
	 * @return
	 */
	public String login (String mail, String password) {

		try{

			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);

			/*
				Uri.Builder uriBuilder = new Uri.Builder();
				uriBuilder.path(_nicoPath);
				uriBuilder.appendQueryParameter("site","nicolive");
			 */
			HttpPost post = new HttpPost(_nicoPath + "?site=nicolive");//uriBuilder.build().toString());

			// POST データの設定
			List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
			postParams.add(new BasicNameValuePair("mail", mail));
			postParams.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));

			client.execute(new HttpHost(_nicoHost, 443, "https"), post);
			_cookieStore = client.getCookieStore();

			/*HttpResponse response =
			 * if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					//
					if (! _cookieStore.getCookies().equals("")){
						return _cookieStore.getCookies().get(0).getValue();
					}
					return "";
				}*/
		}
		catch (Exception e){
			_isLogin = false;
			return e.getMessage();//"ログインに失敗しました";
		}

		if (! _cookieStore.getCookies().equals("")) {
			_isLogin = true;
			return "ログインが成功しました";
		}else{
			_isLogin = false;
			return "ログインに失敗しました";
		}
	}

	/**
	 * ログインクッキーを返します
	 * @param cookieStore ニコニコ動画サイトのCookie
	 * @return 例）user_session=user_session_18180000_265723068462200000;domain=.nicovideo.jp;Path=/
	 */
	public String getLoginCookie(CookieStore cookieStore){
		if ( cookieStore != null ) {
			List<Cookie> cookies = cookieStore.getCookies();
			if (!cookies.equals("")) {
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
	 * ニコ生アラートへのログイン処理
	 * @param mail
	 * @param password
	 * @return
	 */
	public String loginAlert(String mail, String password){
		String ticket = null;

		try{

			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);

			HttpPost post = new HttpPost(_nicoPath + "?site=nicolive_antenna");

			// POST データの設定
			List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
			postParams.add(new BasicNameValuePair("mail", mail));
			postParams.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));

			HttpResponse response = client.execute(new HttpHost(_nicoHost, 443, "https"), post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					ticket = nicoMessage.getNodeValue(getInputStream(response), "ticket");
				}
			}

			if(ticket != null){
				client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
				post = new HttpPost(_getalertstatus);
				postParams = new ArrayList<BasicNameValuePair>();
				postParams.add(new BasicNameValuePair("ticket", ticket));
				post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
				response = client.execute(new HttpHost(_apiHost, 80, "http"), post);
			}

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				document = nicoMessage.getDocument(getInputStream(response));
				_alertaddr = nicoMessage.getNodeValue(document, "addr");
				set_alertport(nicoMessage.getNodeValue(document, "port"));
				_alertthread = nicoMessage.getNodeValue(document, "thread");
				_isLoginAlert = true;
				return nicoMessage.getNodeValue(document, "user_name");
			}

		}catch (Exception e){
			_isLoginAlert = false;
			return "errer " + e.getMessage();//"ログインに失敗しました";
		}

		this._isLoginAlert = false;
		return "アラートログインに失敗しました";
	}
	/**
	 * ユーザー認証を行わない アラートソケットの取得
	 * @return user_id
	 */
	public String getAlertInfo(){
		try{
			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			HttpPost post = new HttpPost(_getalertinfo);
			HttpResponse response = client.execute(new HttpHost(_apiHost, 80, "http"), post);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					document = nicoMessage.getDocument(getInputStream(response));
					_alertaddr = nicoMessage.getNodeValue(document, "addr");
					set_alertport(nicoMessage.getNodeValue(document, "port"));
					_alertthread = nicoMessage.getNodeValue(document, "thread");
					return nicoMessage.getNodeValue(document, "user_id");
				}
			}
		} catch (Exception e){
			return "errer " + e.getMessage();//"ログインに失敗しました";
		}

		return "アラートログインに失敗しました";
	}

	/**
	 * 番組情報取得API
	 * @param lv
	 * @return Title
	 */
	public PlayerStatusData getStreamInfo(String lv){
		try{
			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			HttpPost post = new HttpPost(_getstreaminfo + lv);
			post.addHeader("User-Agent", "NicoLiveAlert 1.2.0");
			HttpResponse response = client.execute(new HttpHost(_apiHost, 80, "http"), post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					Document document = nicoMessage.getDocument(getInputStream(response));
					PlayerStatusData playerStatusData = new PlayerStatusData(lv);
					//playerStatusData.setTitle(getResponceContents(response));
					playerStatusData.setTitle(nicoMessage.getNodeValue(document, PlayerStatusData.TITLE));
					playerStatusData.setCommunityID(nicoMessage.getNodeValue(document, PlayerStatusData.default_communityID));
					playerStatusData.setCommunityName(nicoMessage.getNodeValue(document, PlayerStatusData.CommunityName));
					//System.out.println(playerStatusData.getTitle());
					return playerStatusData;
				}
			}
		} catch (Exception e){
			PlayerStatusData playerStatusData = new PlayerStatusData(lv);
			playerStatusData.setTitle("errer:" + e.getMessage());
			return playerStatusData;
		}
		PlayerStatusData playerStatusData = new PlayerStatusData("アラートログインに失敗しました");
		return playerStatusData;
	}

	/**
	 * ユーザIDからユーザー名を取得します
	 * NicoRequest nicoRequest = new NicoRequest(nicoMessage);
	 * nicoRequest.setLoginCookie(user_session_cookie);
	 * NicoUserData userData =  new NicoUserData("userID","");
	 * nicoRequest.setUserData(userData);
	 * @param userData
	 */
	public void setUserData(NicoUserData userData){
		try{
			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			client.setCookieStore(_cookieStore);
			HttpGet post = new HttpGet(_userid + userData.UserID);
			post.addHeader("User-Agent", "NicoLiveAlert 1.2.0");
			post.addHeader("Host", "www.nicovideo.jp");
			HttpResponse response = client.execute(new HttpHost(_wwwhost, 80, "http"), post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					userData.UserName = nicoMessage.gerUserName(getResponceContents(response));
				}
			}
		} catch (Exception e){
			System.out.println("errer " + e.getMessage());
		}
	}

	private InputStream getInputStream(HttpResponse response) throws IllegalStateException, IOException{
		return response.getEntity().getContent();
	}

	private String getResponceContents(HttpResponse response){
		try {
			return EntityUtils.toString( response.getEntity(), "UTF-8" );
		} catch (ParseException e) {
			e.getMessage();
		} catch (IOException e) {
			e.getMessage();
		}

		return null;
	}

	/**
	 * @return アラートサーバのアドレス
	 */
	public String getAlertAddress() {
		return _alertaddr;
	}
	/**
	 * @return アラートサーバのポート番号
	 */
	public int getAlertPort() {
		return _alertport;
	}
	/**
	 * @return アラートサーバのスレッド番号
	 */
	public String getAlertThread() {
		return _alertthread;
	}

	/**
	 * @param lv 放送番号またはコミュニティ番号
	 * @return PlayerStatusのデータ
	 */
	public PlayerStatusData getPlayerStatus(String lv) {

		try {

			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			client.setCookieStore(_cookieStore);

			HttpGet get = new HttpGet(_getplayerstatus + "?v=" + lv);

			HttpResponse response = client.execute(new HttpHost(_apiHost, 80, "http"), get);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				Document doc = nicoMessage.getDocument(getInputStream(response));
				this._playerStatusData  = nicoMessage.getPlayerStatusData(doc);
				this._addr = nicoMessage.getNodeValue(doc, "addr");
				this.set_port(nicoMessage.getNodeValue(doc, "port"));
				this._thread = nicoMessage.getNodeValue(doc, "thread");
				//this._community_id = nicoMessage.getNodeList(doc, "community_id");
				this._ticket = nicoMessage.getNodeValue(doc, "ticket");
				this._base_time = nicoMessage.getNodeValue(doc, "base_time");
			}

		}catch (Exception e){
			return null;
		}

		return this._playerStatusData;
	}

	public String getPostkey(String thread, String commentNo){
		String block_no = "0";
		if (commentNo == null || commentNo.equals("")){
			block_no = "0";
		}
		else {
			block_no = String.valueOf(Integer.valueOf(commentNo) / 100);
		}

		try{
			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			client.setCookieStore(_cookieStore);
			HttpGet post = new HttpGet(String.format(_getpostkey, thread, block_no));
			HttpResponse response = client.execute(new HttpHost(_apiHost, 80, "http"), post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					return getResponceContents(response).split("=")[1];
				}
			}
		} catch (Exception e){
			return "";
		}

		return "";
	}
	public String getHeartbeat(String lv){
		try{
			DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
			client.setCookieStore(_cookieStore);
			HttpGet post = new HttpGet(_heartbeat + lv);
			HttpResponse response = client.execute(new HttpHost(_apiHost, 80, "http"), post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				//
				if (response.getEntity().isStreaming()){
					Document doc = nicoMessage.getDocument(getInputStream(response));
					return nicoMessage.getNodeValue(doc, "commentCount");
				}
			}
		} catch (Exception e){
			return "";
		}
		return "";
	}

	/**
	 * @return コメントサーバのアドレス
	 */
	public String getAddress() {
		return _addr;
	}
	/**
	 * @return コメントサーバのポート番号
	 */
	public int getPort() {
		return _port;
	}
	private void set_port(String port) {
		this._port = Integer.parseInt(port);
	}
	private void set_alertport(String port) {
		this._alertport = Integer.parseInt(port);
	}
	/**
	 * @return コメントサーバのスレッド
	 */
	public String getThread() {
		return _thread;
	}
}