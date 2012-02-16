package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NicoMainviewActivity extends Activity implements OnClickListener, OnReceiveListener, Handler.Callback {
	//状態表示、コメント表示	
	private EditText etResponse;
	//ビデオ表示したい
	private WebView video;

	private int _senderID = 0;
	
		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//実機傾けた時
		if (savedInstanceState != null){
		video = new WebView(this);
		//
		video.restoreState(savedInstanceState);
		//
		setContentView(R.layout.main);
		return;
		}

		//
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		etResponse = (EditText)findViewById(R.id.ed_response);
		video = (WebView)findViewById(R.id.webView1);
		
		new NicoWebView(getIntent().getStringExtra("LoginCookie"), video).loadUrl();
	}

	public void onClick(View v){

	}

	public int getSenderID() {
		return this._senderID;
	}
	public void setSenderID(int senderID){
		this._senderID = senderID;
	}
	public void onReceive(String receivedMessege){
		
	}
	public boolean handleMessage(Message message) {
		return false;
	}
	protected void onSaveInstanceState(Bundle outState) {
	video.saveState(outState);
	}
}//クラスを閉じれない・・・