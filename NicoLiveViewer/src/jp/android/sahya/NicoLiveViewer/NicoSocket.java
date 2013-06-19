package jp.android.sahya.NicoLiveViewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import jp.android.sahya.NicoLiveViewer.NicoMessage.ChatResult;
import jp.android.sahya.NicoLiveViewer.NicoMessage.CommentData;
import jp.android.sahya.NicoLiveViewer.NicoMessage.SendCommentData;
import jp.android.sahya.NicoLiveViewer.NicoMessage.ThreadData;
import android.annotation.SuppressLint;


public class NicoSocket implements Runnable{
	protected final String SERVER_ERR_MESSAGE = "エラー内容：サーバーとの接続に失敗しました。";
	protected final String SERVER_MESSAGE = "サーバーからのメッセージ：サーバーとの接続に成功しました。";
	private Socket _socket = null;
	private BufferedReader reader = null;
	private OutputStreamWriter osw = null;
	private NicoMessage nicoMessage = null;

	private OnReceiveListener onReceiveListener;
	private boolean isConnect = true;
	//

	public NicoSocket(NicoMessage nicoMesssage){
		this.nicoMessage = nicoMesssage;
	}

	protected Socket getSocket() {
		return _socket;
	}
	protected void setSocket(Socket socket) {
		this._socket = socket;
	}
	protected BufferedReader getCommentStream(){
		return this.reader;
	}
	protected void setCommentStream(BufferedReader reader){
		this.reader = reader;
	}
	public String sendMessage(String message){
		try {
			osw.write(message);
			osw.flush();
			return "";
		} catch (IOException e) {
			return message;
		}
	}

	public String connectCommentServer(String addr, int port, String thread, String res_from) {

		try {
			// サーバーへ接続
			_socket = new Socket(addr, port);
			_socket.setSoTimeout(1000);

			osw = new OutputStreamWriter(_socket.getOutputStream(), "UTF-8");
			osw.write(nicoMessage.getChatMessage(thread, res_from));
			osw.flush();

			// メッセージ取得オブジェクトのインスタンス化
			reader = new BufferedReader(new InputStreamReader(_socket.getInputStream(), "UTF-8"));

		} catch (UnknownHostException e) {
			return SERVER_ERR_MESSAGE;
		} catch (IOException e) {
			return SERVER_ERR_MESSAGE;
		}

		return SERVER_MESSAGE;
	}

	//isConnect &&
	public void run(){
		while (isConnect && _socket.isConnected()){
			try{
				nicoMessage.getCommentMessage(reader, onReceiveListener);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public Runnable getAlertSocketRunnable(){
		return new Runnable(){
			public void run() {
				while (isConnect && _socket.isConnected()){
					try{
						nicoMessage.getAlertMessage(reader, onReceiveListener);
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		};
	}
	public Runnable getMessegeRunnable(){
		return new Runnable(){
			public void run() {
				StringBuffer buff = new StringBuffer();
				int c = -1;
				while (isConnect && _socket.isConnected()){
					try {
						while ((c = reader.read()) != -1) {
							if (c == '\0') {
								onReceiveListener.onReceive(new String[]{buff.toString(),"\n"});
								buff = new StringBuffer();
							} else {
								buff.append((char) c);
							}
						}
					} catch (IOException e) {
						onReceiveListener.onReceive(new String[]{e.toString(),"\n"});
					}
				}
			}
		};
	}

	public void setOnReceiveListener(final OnReceiveListener onReceiveListener){
		this.onReceiveListener = onReceiveListener;
	}
	protected OnReceiveListener getOnReceive() {
		return this.onReceiveListener;
	}

	public boolean isConnected() {
		if (_socket == null){
			return false;
		}
		return this._socket.isConnected();
	}

	public boolean closeSocket(){
		isConnect = false;
		try {
			// 接続終了処理
			_socket.shutdownOutput();
			_socket.shutdownInput();
			osw.close();
			reader.close();
			_socket.close();
		} catch (IOException e) {
			return false;
		}
		finally {
			osw = null;
			reader = null;
			_socket = null;
			isConnect = true;
		}

		return true;
	}

	private class HeartBeat extends TimerTask {
		private String _commentCount = "0";
		private String _liveID;
		private NicoRequest nicoRequest = null;
		private String _cookie;

		public HeartBeat(NicoRequest nicoRequest, String cookie, String liveID){
			_liveID = liveID;
			this.nicoRequest = nicoRequest;
			_cookie = cookie;
		}
		@Override
		public void run() {
			getCommentNo();
		}

		@SuppressLint({ "NewApi", "NewApi" })
		public String getCommentNo(){
			nicoRequest.setLoginCookie(_cookie);
			String commentNo = nicoRequest.getHeartbeat(_liveID);
			System.out.println("Heartbeat:"+ _commentCount + "->" + commentNo);
			if (! commentNo.equals("")){
				_commentCount = commentNo;
			}
			return _commentCount;
		}
	}

	public NicoLiveComment startNicoLiveComment(NicoRequest nicoRequest, String liveID){
		return new NicoLiveComment(nicoRequest, liveID);
	}
	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
	public class NicoLiveComment implements Runnable {
		private final String res_from = "1000";

		private String _liveID;
		private PlayerStatusData playerStatusData;
		private String _postkey = "";
		private String _sendComment = "";
		private Timer _heartbeat = null;
		private HeartBeat heartBeat;

		private ThreadData threadData = null;
		private ChatResult chatResult = null;
		private CommentData commentData = null;
		private SendCommentData sendCommentData = null;

		private NicoRequest nicoRequest = null;
		private String _cookie;

		public NicoLiveComment(NicoRequest nicoRequest, String liveID){
			this.nicoRequest = nicoRequest;
			_cookie = nicoRequest.getLoginCookie();;

			nicoRequest.setLoginCookie(_cookie);
			playerStatusData = nicoRequest.getPlayerStatus(liveID);
			if (playerStatusData != null){
				_liveID = playerStatusData.getLiveID();
				connectCommentServer(nicoRequest.getAddress(), nicoRequest.getPort(), nicoRequest.getThread(), res_from);
				if(isConnected()){
					_heartbeat= new Timer("HeartBeat");
					heartBeat = new HeartBeat(this.nicoRequest, _cookie, _liveID);
					_heartbeat.schedule(heartBeat, 20*1000, 2*60*1000);
					initData();
				}
			}
		}
		private void initData(){
			if (threadData == null){
				threadData = nicoMessage.getThreadData();
			}
			if (chatResult == null){
				chatResult = nicoMessage.getChatResult();
			}
			if (commentData == null){
				commentData = nicoMessage.getCommentData(playerStatusData);
			}
			if (sendCommentData == null){
				sendCommentData = nicoMessage.getSendCommentData(playerStatusData);
			}
		}
		public String getTitle(){
			return playerStatusData.getTitle();
		}
		public boolean isConnected() {
			if (_socket == null){
				return false;
			}
			return _socket.isConnected();
		}
		public String send(String sendComment){
			_sendComment = sendComment;
			if (_postkey.equals("")){
				getPostkey();
			}
			if (! _postkey.equals("")){
				System.out.println(sendCommentData.getSendComment(_postkey, null, _sendComment));
				return sendMessage(sendCommentData.getSendComment(_postkey, null, _sendComment));
			}
			System.out.println(sendCommentData.getSendComment(_postkey, null, _sendComment));
			return _sendComment;
		}
		private void resend(){
			getPostkey();
			if ( ! _postkey.equals("")){
				sendMessage(sendCommentData.getResendComment(_postkey));
			}
			System.out.println(sendCommentData.getResendComment(_postkey));
		}
		private void getPostkey(){
			String postkey = nicoRequest.getPostkey(playerStatusData.getThread(), heartBeat.getCommentNo());
			if (postkey == null || postkey.equals("")){
				_postkey = "";
				return;
			}
			if (postkey.equals(_postkey)){
				_postkey = "";
				return;
			}
			_postkey = postkey;
		}

		public void close(){
			if(isConnected()){
				closeSocket();
			}
			_liveID = null;
			playerStatusData = null;
			_postkey  = null;
			_sendComment  = null;

			if (_heartbeat != null){
				_heartbeat.cancel();
			}
			_heartbeat = null;
			heartBeat = null;

			threadData = null;
			chatResult = null;
			commentData = null;
			sendCommentData = null;
		}
		public String getComment(){
			return _sendComment;
		}
		public boolean isChatResult(){
			return chatResult.isChatResult();
		}
		@Override
		public void run(){
			while (isConnect && _socket.isConnected()){
				try{
					getCommentMessage();
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		private boolean getCommentMessage() {
			StringBuffer buff = new StringBuffer();
			int c = -1;
			try {
				while ((c = reader.read()) != -1) {
					if (c == '\0') {
						String data = buff.toString();
						if  (isComment(data)){
							onReceiveListener.onReceive(commentData.getComment(data));
						}
						else {
							onReceiveListener.onReceive(new String[] {"chatresult","",data});
						}
						buff = new StringBuffer();
					} else {
						buff.append((char) c);
					}
				}
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		private boolean isComment(String data){
			if ( ! threadData.obtainedThreadResult()){
				threadData.setTreadData(data);
				sendCommentData.setTicket(threadData);
				System.out.println(data);
				return false;
			}

			chatResult.setChatResult(data);
			if (chatResult.isChatResult()){
				System.out.println(data);
				if (chatResult.sendMessage()){
					_sendComment = "";
					return false;
				}
				else {
					if (chatResult.isNextPostkey()){
						resend();
						return false;
					}
				}
			}

			return true;
		}
	}
}