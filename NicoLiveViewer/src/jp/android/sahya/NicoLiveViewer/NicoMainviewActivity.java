package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NicoMainviewActivity extends Activity {

	private EditText email; 
	private EditText password;
	//通常のログインをする
	private Button btnLogin;
	//アラート受信用のログインをする（通常のログインしたアカウントはログアウトすることはない）
	private Button btnLoginAlert;
	//番組ID:lv000000000から番組情報を取得してコメントサーバに接続します
	//今後、放送Videoも取得したい
	private Button btnLiveNo;
	//コメントサーバまたはアラートコメントサーバからの接続を切ります
	private Button btnDisconnect;
	//番組ID入力欄、じつはパスワード欄を再利用しています
	private EditText etLiveNo;
	//状態表示、コメント表示	
	private EditText etResponse;
	//表示をPasswordから番組IDに書き換えています
	private TextView tvPassword;
	//ビデオ表示
	private WebView video;
	private String _url = "";
	private String _liveID = "";

	private NicoMessage nicoMesssage = null;
	private NicoRequest nicoRequest = null;
	private NicoSocket nicosocket = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		etLiveNo = (EditText)findViewById(R.id.et_password);
		etResponse = (EditText)findViewById(R.id.ed_response);
		video = (WebView)findViewById(R.id.webView1);

		nicoMesssage = new NicoMessage();
		nicoRequest = new NicoRequest(nicoMesssage);
		//クッキーを受け取る
		nicoRequest.setLoginCookie(getIntent().getStringExtra("LoginCookie"));
		NicoWebView nwv = new NicoWebView(nicoRequest.getLoginCookie(), video);

		//ニコ生ページをロードする
		nwv.loadUrl();
		_url = NicoWebView.CONNECT_URL;
		//WebViewがページを読み込みを開始した時のイベント通知ハンドラを設定
		nwv.setOnPageStartedHandler(new Handler(new ChangedUrlHandler()));
	}
	/**
	 * WebViewのURL変更時の処理
	 */
	class ChangedUrlHandler implements Handler.Callback {
		public boolean handleMessage(Message message) {
			switch (message.what){
			case NicoWebView.ON_PAGE_STARTED:
				if (isChangedUrl(message.obj.toString())){
					new GetComment().getComment();
					return true;
				}
				break;
			}
			return false;
		}
		private boolean isChangedUrl(String url){
			if (!_url.equals(url)){
				_url = url;
				return true;
			}
			return false;
		}
	}
	

	/**
	 * 放送ページのコメント取得処理
	 */
	class GetComment implements Handler.Callback, OnReceiveListener, Runnable {
		final Handler handler = new Handler(this);

		private void getComment() {
			_liveID = nicoMesssage.getLiveID(_url);
			if (_liveID.equals("")){ return; }
			nicosocket = new NicoSocket(nicoMesssage);
			nicosocket.setOnReceiveListener(this);
			new Thread(this).start();
		}
		
		public void run() {
			nicoRequest.getPlayerStatus(_liveID);
			nicosocket.connectCommentServer(nicoRequest.getAddress(), nicoRequest.getPort(), nicoRequest.getThread());
			Message message = handler.obtainMessage();
			handler.sendMessage(message);
		}
		
		public boolean handleMessage(Message msg) {
			if (nicosocket.isConnected()){
				new Thread(nicosocket).start();	
			}else{
				etResponse.setText("番組に接続できませんでした");
			}
			return true;
		}

		public void onReceive(String receivedMessege){
			etResponse.append(receivedMessege + "\n");
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		video.restoreState(savedInstanceState);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		video.saveState(outState);
	}
}