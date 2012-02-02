package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class NicoLivePlayerActivity extends Activity implements OnClickListener, OnReceiveListener, Handler.Callback {
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
	//ビデオ表示したい
	private VideoView video;
	
	private NicoMessage nicoMesssage = null;
	private NicoRequest nico = null;
	private NicoSocket nicosocket = null;
	private int _senderID = 0;
	//private final int ON_ARERT_ID = -1;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
                
        email = (EditText)findViewById(R.id.et_mail);
        password = (EditText)findViewById(R.id.et_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        btnLoginAlert = (Button)findViewById(R.id.btn_loginAlert);
        btnLoginAlert.setOnClickListener(this);
        btnLiveNo = (Button)findViewById(R.id.btnLive);
        btnLiveNo.setOnClickListener(this);
        btnDisconnect = (Button)findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(this);
        etLiveNo = (EditText)findViewById(R.id.et_password);
        etResponse = (EditText)findViewById(R.id.ed_response);
        tvPassword = (TextView)findViewById(R.id.tv_password);
        video = (VideoView)findViewById(R.id.videoview);
        
        //
        nicoMesssage = new NicoMessage();
        nico = new NicoRequest(nicoMesssage);
        
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        // チェックボックスのチェック状態を設定します
        checkBox.setChecked(true);
        // チェックボックスがクリックされた時に呼び出されるコールバックリスナーを登録します
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                // チェックボックスのチェック状態を取得します
                boolean checked = checkBox.isChecked();
                Toast.makeText(v.getContext(),
                        "onClick():" + String.valueOf(checked),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    public void onClick(View v){
    	switch (v.getId()) {
    		case R.id.btn_login :{
    			setSenderID(R.id.btn_login);
    			key();    			
    			final Handler handler = new Handler(this);
    			
    			new Thread((new Runnable(){
    				public void run() {
    					nico.login(email.getText().toString(),password.getText().toString());
    					Message message = handler.obtainMessage(R.id.btn_login);
    					handler.sendMessage(message);
    				}})).start();
    			
    			return;
			}
    		
    		case R.id.btnLive : {
    			setSenderID(R.id.btnLive);
    			key();
    			
    			final Handler handler = new Handler(this);
				nicosocket = new NicoSocket(nicoMesssage);
				nicosocket.setOnReceiveListener(this);
    			
    			new Thread(new Runnable(){
					public void run() {
						nico.getPlayerStatus(etLiveNo.getText().toString());
						nicosocket.connectCommentServer(nico.getAddress(), nico.getPort(), nico.getThread());
		    			Message message = handler.obtainMessage(R.id.btnLive);
    					handler.sendMessage(message);
					}}).start();
    			
    			return;
    		}
    		
    		case R.id.btn_loginAlert : {
    			key();
    			setSenderID(R.id.btn_loginAlert);
    				
    			final Handler handler = new Handler(this);
    			nicosocket = new NicoSocket(nicoMesssage);
				nicosocket.setOnReceiveListener(this);
    			
    			new Thread (new Runnable(){
					public void run() {
						nico.loginAlert(email.getText().toString(),password.getText().toString());
						nicosocket.connectCommentServer(nico.getAlertAddress(), nico.getAlertPort(), nico.getAlertThread());
						Message message = handler.obtainMessage(R.id.btn_loginAlert);
    					handler.sendMessage(message);
					}}).start();
    			
    			return;
    		}
    		
    		case R.id.btnDisconnect : {
    			switch (getSenderID()){
    			
    			case R.id.btnLive : {
    				if(nicosocket.isConnected()){
        				if(nicosocket.closeSockt()){
        					etResponse.setText("disconnected");
        					btnLiveNo.setVisibility(View.VISIBLE);
        					btnDisconnect.setVisibility(View.GONE);
        					video.stopPlayback();
        				}
        			}
        			return;
    			}
    			case R.id.btn_loginAlert : {
    				if(nicosocket.isConnected()){
        				if(nicosocket.closeSockt()){
        					etResponse.setText("disconnected");
        					btnLogin.setVisibility(View.VISIBLE);
        					btnLoginAlert.setVisibility(View.VISIBLE);
        					btnDisconnect.setVisibility(View.GONE);
        				}
        			}
        			return;
    			}
    			}
    			
    		}
    	}
    }

    private void playVideo(Uri uri){
       video.requestFocus();
       video.setMediaController(new MediaController(this));
       video.setVideoURI(uri);
       video.start();
    }
    public int getSenderID() {
		return this._senderID;
	}
    public void setSenderID(int senderID){
    	this._senderID = senderID;
    }
    public void onReceive(String receivedMessege){
    	switch (this.getSenderID()){
    		case R.id.btnLive :{
    			etResponse.append(receivedMessege + "\n");
    			return;
    		}
    		
    		case R.id.btn_loginAlert : {
    			etResponse.append(receivedMessege + "\n");
    			return;
    		}
    	}
    }
    
    private void key(){
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

	public boolean handleMessage(Message message) {
		switch (message.what){
			case R.id.btn_login :{
				if (nico.isLogin()){
					tvPassword.setText("番組ID");
					password.setText("lv");
					password.setInputType(InputType.TYPE_CLASS_NUMBER);
					btnLogin.setVisibility(View.GONE);
					btnLoginAlert.setVisibility(View.GONE);
					btnLiveNo.setVisibility(View.VISIBLE);
					Toast.makeText(this, "ログインしました", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this, "ログインできませんでした", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
			
			case R.id.btnLive : {
    			if (nicosocket.isConnected()){
    				new Thread(nicosocket).start();	
        			btnLiveNo.setVisibility(View.GONE);
        			btnDisconnect.setVisibility(View.VISIBLE);
        			//playVideo(uri);
    			}else{
    				Toast.makeText(this, "番組に接続できませんでした", Toast.LENGTH_SHORT).show();
    				
    			}
    			
    			return true;
    		}
			
			case R.id.btn_loginAlert : {
				if(nicosocket.isConnected()){
					new Thread(nicosocket.getAlertSocketRun()).start();
					btnLiveNo.setVisibility(View.GONE);
					btnLogin.setVisibility(View.GONE);
					btnLoginAlert.setVisibility(View.GONE);
        			btnDisconnect.setVisibility(View.VISIBLE);
				}else{
					Toast.makeText(this, "アラートログインに失敗しました", Toast.LENGTH_SHORT).show();
    			}
				
				return true;
			}
		}
		
		return false;
	}
}