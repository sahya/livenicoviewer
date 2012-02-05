package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
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
	//番組ID入力欄、じつはパスワード欄を再利用しています
	private EditText etLiveNo;
	//状態表示、コメント表示	
	private EditText etResponse;
	//表示をPasswordから番組IDに書き換えています
	private TextView tvPassword;
	
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
        etLiveNo = (EditText)findViewById(R.id.et_password);
        etResponse = (EditText)findViewById(R.id.ed_response);

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
    			login();
    			return;
			}
    		
    		case R.id.btn_loginAlert : {
    			loginAlert();
    			return;
    		}
    	}
    }
    
    //Login
    private void login(){
    	setSenderID(R.id.btn_login);
		key();    			
		final Handler handler = new Handler(this);
		
		new Thread((new Runnable(){
			public void run() {
				nico.login(email.getText().toString(),password.getText().toString());
				Message message = handler.obtainMessage(R.id.btn_login);
				handler.sendMessage(message);
			}})).start();
		
    }
    private void loginAlert(){
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
				loginMessage();
				return true;
			}
			case R.id.btn_loginAlert : {
				loginAlertMessage();
				return true;
			}
		}
		
		return false;
	}
	
	//Login Message
	private void loginMessage(){
		if (nico.isLogin()){
			tvPassword.setText("番組ID");
			password.setText("lv");
			password.setInputType(InputType.TYPE_CLASS_NUMBER);
			btnLogin.setVisibility(View.GONE);
			btnLoginAlert.setVisibility(View.GONE);
			Toast.makeText(this, "ログインしました", Toast.LENGTH_SHORT).show();
			// インテントのインスタンス生成
			Intent intent = new Intent(NicoLivePlayerActivity.this, NicoMainviewActivity.class);
			// 次画面のアクティビティ起動
			startActivity(intent);
		}else{
			Toast.makeText(this, "ログインできませんでした", Toast.LENGTH_SHORT).show();
		}
	}
	//Login Alert Message
	private void loginAlertMessage(){
		if(nicosocket.isConnected()){
			new Thread(nicosocket.getAlertSocketRun()).start();
			btnLogin.setVisibility(View.GONE);
			btnLoginAlert.setVisibility(View.GONE);
		}else{
			Toast.makeText(this, "アラートログインに失敗しました", Toast.LENGTH_SHORT).show();
		}
		
	}
	
}