package jp.android.sahya.NicoLiveViewer;

import jp.android.sahya.NicoLiveViewer.NicoSocket.NicoLiveComment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class NicoMainviewActivity extends Activity {

	//ビデオ表示
	private NicoCommentListView _commentList;
	private NicoWebView _nicoWebView;
	private String _url = "";
	private String _liveID = "";
	private String _embed1 = "<embed type=\"application/x-shockwave-flash\" src=\"http://nl.nimg.jp/sp/swf/spplayer.swf?120501105350\" width=\"100%\" height=\"100%\" style=\"\" id=\"flvplayer\" name=\"flvplayer\" bgcolor=\"#FFFFFF\" quality=\"high\" allowscriptaccess=\"always\" flashvars=\"playerRev=120501105350_0&amp;playerTplRev=110721071458&amp;playerType=sp&amp;v=";
	private String _embed2 = "&amp;lcname=&amp;pt=community&amp;category=&amp;watchVideoID=&amp;videoTitle=&amp;gameKey=&amp;gameTime=&amp;isChannel=&amp;ver=2.5&amp;userOwner=false&amp;us=0\">";
	private String _css = "<style type=\"text/css\"> html, body { margin: 0; padding: 0; } </style>";
	//Comment Post
	private Button btnCommentPost;
	private NicoLiveComment nicoLiveComment;
	private EditText etCommentPost;

	private NicoMessage nicoMessage = null;
	private NicoRequest nicoRequest = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		etCommentPost = (EditText)findViewById(R.id.ed_response);
		btnCommentPost = (Button)findViewById(R.id.btn_commentpost);		
		btnCommentPost.setOnClickListener(new SendComment());
		_commentList = new NicoCommentListView((ListView)findViewById(R.id.commentListView2), getApplicationContext());

		nicoMessage = new NicoMessage();
		nicoRequest = new NicoRequest(nicoMessage);
		//クッキーを受け取る
		nicoRequest.setLoginCookie(getIntent().getStringExtra("LoginCookie"));
		_nicoWebView = new NicoWebView(nicoRequest.getLoginCookie(), (WebView)findViewById(R.id.webView1));

		//ニコ生ページをロードする
		_nicoWebView.loadUrl();
		_url = NicoWebView.CONNECT_URL;
		//WebViewがページを読み込みを開始した時のイベント通知ハンドラを設定
        _nicoWebView.setOnPageStartedHandler(new Handler(new ChangedUrlHandler()));
        //WebViewがページを読み込みを完了した時のイベント通知ハンドラを設定
        _nicoWebView.setOnPageFinishedHandler(new Handler(new OnPageFinishedHandler()));
	}
	/**
     * WebViewのURL変更時の処理
     */
	/**
     * WebViewのURL変更時の処理
     */
    private class ChangedUrlHandler implements Handler.Callback {
    	public boolean handleMessage(Message message) {
    		switch (message.what){
    		case NicoWebView.ON_PAGE_STARTED:
    			if (isChangedUrl(message.obj.toString())){
    				_liveID = nicoMessage.getLiveID(_url, true);
    	        	if (_liveID.equals("")){ return false; }
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
    private class OnPageFinishedHandler implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == NicoWebView.ON_PAGE_FINISHED){
	        	if (nicoMessage.getLiveID(msg.obj.toString(), true).equals("")){ return false; }
	        	_nicoWebView.loadDataWithBaseURL(_css +_embed1 + _liveID + _embed2);
	        	
	        	return true;
			}
			return false;
		}
	}
	

	/**
	 * 放送ページのコメント取得処理
	 */
	class GetComment implements Runnable, Handler.Callback, OnReceiveListener {
		final Handler handler = new Handler(this);
		//Replace Word
		//hb ifseetno=席移動, perm=表示なし,info 2＝表示なし
		private final Pattern _hb_ifseetnoPattern  = Pattern.compile("^/hb ifseetno.*");
		private final Pattern _permPattern  = Pattern.compile("^/perm.*");
		private final Pattern _info_2Pattern  = Pattern.compile("^/info 2.*");

		private void getComment() {
			_liveID = nicoMessage.getLiveID(_url, true);
			if (_liveID.equals("")){ return; }
			new Thread(this).start();
		}
		
		public void run() {
			nicoRequest.getPlayerStatus(_liveID);
			NicoSocket nicoSocket = new NicoSocket(nicoMessage);
            nicoSocket.setOnReceiveListener(this);
			nicoLiveComment = nicoSocket.startNicoLiveComment(nicoRequest, _liveID);
			Message message = handler.obtainMessage();
			handler.sendMessage(message);
		}
		
		public boolean handleMessage(Message msg) {
			if (nicoLiveComment.isConnected()){
				new Thread(nicoLiveComment).start();
			}else{
				_commentList.append(new String[]{"Live ID:",_liveID,"番組に接続できませんでした"});
			}
			return true;
		}

		public void onReceive(String[] receivedMessege){
        	if (hasDisplay(receivedMessege)){
        		handler.post(new ReceivedMessege(receivedMessege));
        	}
			if (receivedMessege[2].equals("/disconnect") && nicoLiveComment.isConnected()){
				nicoLiveComment.close();
			}
		}
		private class ReceivedMessege implements Runnable {
			private String[] receivedMessege;
			public ReceivedMessege(String[] receivedMessege){
				this.receivedMessege = receivedMessege;
			}
			@Override
			public void run() {
				_commentList.append(replaceCommentWord(receivedMessege));
			}
		}
		private boolean hasDisplay(String[] receivedMessege){
			if (receivedMessege[0].equals("chatresult")) {
				return false;
			}
			Matcher matcher = _permPattern.matcher(receivedMessege[2]);
			if(matcher.matches()){
				return false;
			}
			matcher = _info_2Pattern.matcher(receivedMessege[2]);
			if(matcher.matches()){
				return false;
			}
			return true;
		}
		private String[] replaceCommentWord(String[] receivedMessege){
			Matcher matcher = _hb_ifseetnoPattern.matcher(receivedMessege[2]);
			if(matcher.matches()){
				return new String[] { receivedMessege[0], receivedMessege[1], "席移動"};
			}
			
			return receivedMessege;
		}
	}
	class SendComment implements OnClickListener, Runnable, Handler.Callback {
		final Handler handler = new Handler(this);
		
		public void onClick(View v) {
			new Thread(this).start();
		}
		
		public void run() {
			if (nicoLiveComment == null || nicoLiveComment.isConnected() == false){
         		return;
         	}
         	nicoLiveComment.send(etCommentPost.getText().toString());
			Message message = handler.obtainMessage();
			handler.sendMessage(message);
		}
		
		public boolean handleMessage(Message msg) {
			return true;
		}

	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		_nicoWebView.getWebView().restoreState(savedInstanceState);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		_nicoWebView.getWebView().saveState(outState);
		}
	
	
	//戻るボタンをタップすると終了処理
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK){  

			//ダイアログの表示  
			AlertDialog.Builder ad=new AlertDialog.Builder(this);  
			ad.setMessage(getString(R.string.dialog_message01));  
			ad.setPositiveButton(getString(R.string.Yes),
					new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton) {  
					//OKならActivity終了  
					finish(); 
					
				}     
			});  
			ad.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton) {  
					//NOならそのまま何もしない  

				}     
			});  
			ad.create();  
			ad.show();  
			return false;  
		} 
		return true;
	}  
}
