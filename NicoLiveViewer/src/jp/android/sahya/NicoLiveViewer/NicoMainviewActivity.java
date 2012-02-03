package jp.android.sahya.NicoLiveViewer;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class NicoMainviewActivity extends Activity implements OnClickListener, OnReceiveListener, Handler.Callback {
		
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
		private NicoRequest nico ;
		private NicoSocket nicosocket;
		private int _senderID = 0;
		public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		    	setContentView(R.layout.main);
				
				btnLiveNo = (Button)findViewById(R.id.btnLive);
		        btnLiveNo.setOnClickListener(this);
		        btnDisconnect = (Button)findViewById(R.id.btnDisconnect);
		        btnDisconnect.setOnClickListener(this);
		        etLiveNo = (EditText)findViewById(R.id.et_password);
		        etResponse = (EditText)findViewById(R.id.ed_response);
		        //tvPassword = (TextView)findViewById(R.id.tv_password);
		        video = (VideoView)findViewById(R.id.videoview);
		}
		
		    public void onClick(View v){
	    	switch (v.getId()) {
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
	    	}
	  }
	  public boolean handleMessage(Message message) {
	    	switch (message.what){
	    		case R.id.btnLive : {
	    	if (nicosocket.isConnected()){
	    		new Thread(nicosocket).start();	
	    		btnLiveNo.setVisibility(View.GONE);
	    		btnDisconnect.setVisibility(View.VISIBLE);
	    	//playVideo(uri);
	    	}else{
	    		Toast.makeText(this, "番組に接続できませんでした", Toast.LENGTH_SHORT).show();
	    			    				
	    	}