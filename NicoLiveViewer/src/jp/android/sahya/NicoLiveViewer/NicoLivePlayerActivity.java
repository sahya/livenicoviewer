package jp.android.sahya.NicoLiveViewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

public class NicoLivePlayerActivity extends Activity {
	private EditText email; 
	private EditText password;
	//通常のログインをする
	private Button btnLogin;
	//アラート受信用のログインをする（通常のログインしたアカウントはログアウトすることはない）
	private Button btnLoginAlert;
	//状態表示、コメント表示	
	private EditText etResponse;
	//表示をPasswordから番組IDに書き換えています
	private TextView tvPassword;
	//
	private CheckBox checkBox;
	
	private NicoMessage nicoMesssage = null;
	private NicoRequest nico = null;
	private NicoSocket nicosocket = null;
	private NicoFile saveDataFile = new NicoFile("NicoLiveViewerData.dat");
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.login);
        
        tvPassword = (TextView)findViewById(R.id.tv_password);
        email = (EditText)findViewById(R.id.et_mail);
        password = (EditText)findViewById(R.id.et_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new Login());
        btnLoginAlert = (Button)findViewById(R.id.btn_loginAlert);
        btnLoginAlert.setOnClickListener(new LoginAlert());
        etResponse = (EditText)findViewById(R.id.ed_response);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        //
        nicoMesssage = new NicoMessage();
        nico = new NicoRequest(nicoMesssage);
        // チェックボックスを設定します
        new SaveCheckBox();
    }

    class SaveCheckBox implements View.OnClickListener {
    	public SaveCheckBox(){
    		// チェックボックスのチェック状態を設定します
    		if (saveDataFile.canReadFile(getApplicationContext())){
    			//ログインデータが保存されていれば、チェックボックスの状態を復元する
    			checkBox.setChecked(((NicoInfoData)saveDataFile.openFile(getApplicationContext())).isStore);
    			//チェックボックスが付いれいれば、メールとパスワードを復元する
    			if (checkBox.isChecked()){
    				email.setText(NicoCrypt.decrypt(NicoKey.getKey(),
    						((NicoInfoData)saveDataFile.openFile(getApplicationContext())).mail));
    				password.setText(NicoCrypt.decrypt(NicoKey.getKey(),
    						((NicoInfoData)saveDataFile.openFile(getApplicationContext())).password));
    			}
    		} else {
    			checkBox.setChecked(true);
    		}
    		// チェックボックスがクリックされた時に呼び出されるコールバックリスナーを登録します
    		checkBox.setOnClickListener(this);
    	}
    	public void onClick(View v) {
    		// チェックボックスのチェック状態を取得します
    		Toast.makeText(v.getContext(),
    				"onClick():" + String.valueOf(checkBox.isChecked()),
    				Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
     * Login処理
     */
    class Login implements OnClickListener, Handler.Callback, Runnable {
		final Handler handler = new Handler(this);
		
    	public void onClick(View v) {
	    	//ログインボタンをdisableにする
	    	btnLogin.setEnabled(false);
	    	btnLoginAlert.setEnabled(false);
			key();    			
			new Thread(this).start();
		}
    	public void run() {
			nico.login(email.getText().toString(),password.getText().toString());
			Message message = handler.obtainMessage(R.id.btn_login);
			handler.sendMessage(message);
		}
    	
		public boolean handleMessage(Message arg0) {
			if (nico.isLogin()){
				//チェックが付いていればログインデータを保存する
				saveNicoInfoData();
				
				tvPassword.setText("番組ID");
				password.setText("lv");
				password.setInputType(InputType.TYPE_CLASS_NUMBER);
				btnLogin.setVisibility(View.GONE);
				btnLoginAlert.setVisibility(View.GONE);
				Toast.makeText(getApplicationContext(), "ログインしました", Toast.LENGTH_SHORT).show();
				// インテントのインスタンス生
				Intent intent = new Intent(getApplicationContext(), NicoMainviewActivity.class);
				// 次画面のアクティビティ起動
				NicoWebView nwv = new NicoWebView(nico.getCookieStore());
				intent.putExtra("LoginCookie", nwv.getLoginCookie());
				startActivity(intent);
				finish();
			}else{
				Toast.makeText(getApplicationContext(), "ログインできませんでした", Toast.LENGTH_SHORT).show();
				//ログインボタンをenableにする
		    	btnLogin.setEnabled(true);
		    	btnLoginAlert.setEnabled(true);
			}
			
			return true;
		}
    }
    
    private void saveNicoInfoData(){
    	NicoInfoData data = new NicoInfoData();
    	
    	if (checkBox.isChecked()){    		
    		data.mail = NicoCrypt.encrypt(NicoKey.getKey(), email.getText().toString());
    		data.password = NicoCrypt.encrypt(NicoKey.getKey(), password.getText().toString());
    		data.sessionCookie = NicoCrypt.encrypt(NicoKey.getKey(), nico.getLoginCookie());
    		data.lastUrl = NicoWebView.CONNECT_URL;
    		data.isStore = checkBox.isChecked();
    	} else {
    		data.mail = "0".getBytes();
    		data.password = "0".getBytes();
    		data.sessionCookie = NicoCrypt.encrypt(NicoKey.getKey(), nico.getLoginCookie());
    		data.lastUrl = NicoWebView.CONNECT_URL;
    		data.isStore = checkBox.isChecked();
    	}
    	
    	saveDataFile.saveFile(getApplicationContext(), data);
    }

    /**
     * LoginAlert処理
     */
    class LoginAlert implements OnClickListener, Handler.Callback ,OnReceiveListener, Runnable {
    	final Handler handler = new Handler(this);
    	
    	public void onClick(View v) {
    		key();
    		//ログインボタンをdisableにする
        	btnLogin.setEnabled(false);
        	btnLoginAlert.setEnabled(false);
 
    		nicosocket = new NicoSocket(nicoMesssage);
    		nicosocket.setOnReceiveListener(this);   		
    		new Thread (this).start();
		}
    	
    	public void run() {
			nico.loginAlert(email.getText().toString(),password.getText().toString());
			nicosocket.connectCommentServer(nico.getAlertAddress(), nico.getAlertPort(), nico.getAlertThread(), "1");
			Message message = handler.obtainMessage(R.id.btn_loginAlert);
			handler.sendMessage(message);
		}
    	
    	public boolean handleMessage(Message msg) {
    		if(nicosocket.isConnected()){
    			new Thread(nicosocket.getAlertSocketRunnable()).start();
    			btnLogin.setVisibility(View.GONE);
    			btnLoginAlert.setVisibility(View.GONE);
    		}else{
    			Toast.makeText(getApplicationContext(), "アラートログインに失敗しました", Toast.LENGTH_SHORT).show();
    			//ログインボタンをenableにする
    	    	btnLogin.setEnabled(true);
    	    	btnLoginAlert.setEnabled(true);
    		}
			return true;
		}
    	
		public void onReceive(String[] receivedMessege) {
			etResponse.append(receivedMessege[0] + ":" + receivedMessege[1] + ":" + receivedMessege[2] +"\n");
		}	
    }
    
    private void key(){
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if( intent != null ) {
			/** リンク先のURLを取得する。 */
			String data = intent.getDataString();
			if(data != null){
				//urlを元にごにょごにょ
			}
		}
	}
}