package jp.android.sahya.NicoLiveViewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NicoMessage {
	private final String _chatMessage = "<thread thread=\"{0}\" version=\"20061206\" res_from=\"-{1}\"/>\0";
	private final String _sendComment = "<chat thread=\"{0}\" ticket=\"{1}\" vpos=\"{2}\" postkey=\"{3}\" mail=\"{4}\" user_id=\"{5}\" premium=\"{6}\" locale=\"jp\">{7}</chat>\0";
	private final Pattern _threadResult = Pattern.compile("<thread resultcode=\"0\".+thread=\".+ticket=\"(.+)?\" .+server_time=\"(.+)?\"/>");
	private final Pattern chatpattern = Pattern.compile("<chat.*>(.*,.*,.*)</chat>");
	private final Pattern chatOfficialpattern = Pattern.compile("<chat.*>(.*,.*)</chat>");
	private final Pattern _commentChatpattern = Pattern.compile("<chat .* no=\"([0-9]*?)\" .* user_id=\"(.*?)\" .*>(.*)</chat>"); 
	private final Pattern _chatresultpattern = Pattern.compile("<chat_result thread=\".+\" status=\"([0-9])\" .+/>");
	private final Pattern _liveLvPattern = Pattern.compile("http://sp.live.nicovideo.jp/watch/(lv[0-9]+)");
	private final Pattern _liveComuPattern = Pattern.compile("http://sp.live.nicovideo.jp/watch/(co[0-9]+)");
	private final Pattern _liveChannelPattern = Pattern.compile("http://sp.live.nicovideo.jp/watch/(ch[0-9]+).*");
	
	private final Pattern _nicoLiveLvPattern = Pattern.compile(".*(lv[0-9]+).*");
	private final Pattern _nicoLiveComuPattern = Pattern.compile(".*(co[0-9]+).*");
	private final Pattern _nicoLiveChannelPattern = Pattern.compile(".*(ch[0-9]+).*");
	//
	private final Pattern _communityOwnerPattern = Pattern.compile("[.\\s\\S]*オーナー：<a href=\"http://www.nicovideo.jp/user/([0-9]+)\"[.\\s\\S]*");
	private final Pattern _communityNamePattern = Pattern.compile("[.\\s\\S]*<h1.+>(.*)</h1>[.\\s\\S]*");
	
	private final Pattern _userNamePattern = Pattern.compile("[.\\s\\S]*<title>(.*)さんのユーザーページ ‐ niconico</title>[.\\s\\S]*");
	public NicoMessage(){
		
	}
	
	public PlayerStatusData getPlayerStatusData(Document getplayerstatusXML){
		PlayerStatusData playerStatusData = new PlayerStatusData(getNodeValue(getplayerstatusXML, PlayerStatusData.LIVEID));
		playerStatusData.setUrl(getNodeValue(getplayerstatusXML, PlayerStatusData.LIVE_STRAEM_URL));
		playerStatusData.setContents(getNodeValue(getplayerstatusXML, PlayerStatusData.CONTENTS_rtmp).substring(5));
		playerStatusData.setTicket(getNodeValue(getplayerstatusXML, PlayerStatusData.TICKET));
		playerStatusData.setTitle(getNodeValue(getplayerstatusXML, PlayerStatusData.TITLE));
		playerStatusData.setCommunityID(getNodeValue(getplayerstatusXML, PlayerStatusData.default_communityID));
		playerStatusData.setOwnerID(getNodeValue(getplayerstatusXML, PlayerStatusData.OwnerID));
		playerStatusData.setOwnerName(getNodeValue(getplayerstatusXML, PlayerStatusData.OwnerName));
		playerStatusData.setUserID(getNodeValue(getplayerstatusXML, PlayerStatusData.UserID));
		playerStatusData.setIsPremium(getNodeValue(getplayerstatusXML, PlayerStatusData.IS_PREMIUM));
		playerStatusData.set_base_time(getNodeValue(getplayerstatusXML, PlayerStatusData.BASETIME));
		playerStatusData.set_start_time(getNodeValue(getplayerstatusXML, PlayerStatusData.STARTTIME));
		playerStatusData.setThread(getNodeValue(getplayerstatusXML, PlayerStatusData.THREAD));
		return playerStatusData;
	}
	public String[] getThreadResult(String threadResult){
		Matcher matcher = _threadResult.matcher(threadResult);
		if(matcher.matches()){
			return new String[] { matcher.group(1), matcher.group(2) };
		}
		
		return new String[] { "", "" };
	}
	public Document getDocument(InputStream is) throws SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
	}
	public String getSendComment(String thread, String ticket, String vpos, String postkey, String mail, String userID, String premium, String message){
		return MessageFormat.format(_sendComment, thread, ticket, vpos, postkey, mail, userID, premium, escapeXML(message));
	}
	public String escapeXML(String xml){
		return xml.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
	}
	public String unescapeXML(String xml){
		return xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&");
	}
	public String get_vpos(String baseTime){
		return String.valueOf(System.currentTimeMillis()/10 - Long.valueOf(baseTime)*100);
	}
	public String getChatresultStatus(String chatresult){
		Matcher matcher = _chatresultpattern.matcher(chatresult);
		if(matcher.matches()){
			return matcher.group(1);
		}else{
			return "";
		}
	}
	public String[] getChatData(String chatdata) {
		Matcher matcher = chatpattern.matcher(chatdata);
		if(matcher.matches()){
			return matcher.group(1).split(",");
		}
		
		matcher = chatOfficialpattern.matcher(chatdata);
		if(matcher.matches()){
			String chat[] = matcher.group(1).split(",");
			return new String[] { chat[0], chat[1], "" };
		}
		
		return new String[] { "", "", "" };
	}
	
	public boolean isCommunityID(String data){
		Matcher matcher = _nicoLiveComuPattern.matcher(data);
		return matcher.matches();
	}
	public boolean isChannel(String data){
		Matcher matcher = _nicoLiveChannelPattern.matcher(data);
		return matcher.matches();
	}
	public String getLiveID (String url, boolean isSpMode){
		Matcher matcher = _liveLvPattern.matcher(url);
		if(matcher.matches()){
			return matcher.group(1);
		}
		
		matcher = _liveComuPattern.matcher(url);
		if (matcher.matches()){
			return matcher.group(1);
		}
		
		matcher = _liveChannelPattern.matcher(url);
		if (matcher.matches()){
			return matcher.group(1);
		}
		
		if (isSpMode) {
			return "";
		}
		
		matcher = _nicoLiveComuPattern.matcher(url);
		if (matcher.matches()){
			return matcher.group(1);
		}
		
		matcher = _nicoLiveLvPattern.matcher(url);
		if (matcher.matches()){
			return matcher.group(1);
		}
		
		matcher = _nicoLiveChannelPattern.matcher(url);
		if (matcher.matches()){
			return matcher.group(1);
		}
		
		return url;
	}
	public String gerUserName (String htmlDocument){
		Matcher matcher = _userNamePattern.matcher(htmlDocument);
		if(matcher.matches()){
			return matcher.group(1);
		}
		return "";
	}
	
	public String getNodeValue(InputStream is, String elementsName) throws SAXException, IOException, ParserConfigurationException{
		return getNodeValue(getDocument(is), elementsName);
	}
	public NodeList getNodeList(Document document, String elementsName){
		return document.getElementsByTagName(elementsName);
	}
	public String getNodeValue(Document document, String elementsName){
		return document.getElementsByTagName(elementsName).item(0).getTextContent();
	}
	public String getNodeValue(Document document, String elementsName, int index){
		return document.getElementsByTagName(elementsName).item(index).getTextContent();
	}

	public String getChatMessage(String thread, String res_from) {
		return MessageFormat.format(this._chatMessage, thread, res_from);
	}
	
	
	public boolean getCommentMessage(BufferedReader reader, final OnReceiveListener onReceive) {
		StringBuffer buff = new StringBuffer();
		int c = -1;
		try {
			while ((c = reader.read()) != -1) {
				if (c == '\0') {
					onReceive.onReceive(getChat(buff.toString()));
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
	public boolean getAlertMessage(BufferedReader reader, final OnReceiveListener onReceive) {
		StringBuffer buff = new StringBuffer();
		int c = -1;	
		try {
			while ((c = reader.read()) != -1) {
				if (c == '\0') {
					onReceive.onReceive(getChatData(buff.toString()));
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
	public String[] getChat(String chatdata){
		Matcher matcher = _commentChatpattern.matcher(chatdata);
		if(matcher.matches()){
			return new String[]{matcher.group(1), matcher.group(2),matcher.group(3)};
		}else{
			return new String[]{"","",chatdata};
		}
	}
	public ThreadData getThreadData(){
		return new ThreadData();
	}
	public class ThreadData {
		private final Pattern _ticketPattern = Pattern.compile("<thread resultcode=\"0\".+ticket=\"(.+?)\" .+/>");
		private final Pattern _last_resPattern = Pattern.compile("<thread resultcode=\"0\".+last_res=\"(.+?)\" .+/>");
		private final Pattern _server_timePattern = Pattern.compile("<thread resultcode=\"0\".+server_time=\"(.+?)\".*/>");
		private boolean _isThreadResult = false;
		private boolean _obtainedThreadResult = false;
		private String _ticket;
		private String _last_res;
		private String _server_time;
		public ThreadData (){
		}
		public void setTreadData(String threadData){
			Matcher matcher = _ticketPattern.matcher(threadData);
			if(matcher.matches()){
				_ticket = matcher.group(1);
				_isThreadResult = true;
				_obtainedThreadResult = true;
			}
			else {
				_isThreadResult = false;
				_obtainedThreadResult = false;
			}
			
			matcher = _last_resPattern.matcher(threadData);
			if(matcher.matches()){
				_last_res = matcher.group(1);
			}
			matcher = _server_timePattern.matcher(threadData);
			if(matcher.matches()){
				_server_time = matcher.group(1);
			}
		}
		public boolean obtainedThreadResult(){
			return _obtainedThreadResult;
		}
		public boolean isThreadResult(){
			return _isThreadResult;
		}
		public String getTicket() {
			return _ticket;
		}
		public String getLast_res() {
			return _last_res;
		}
		public String getServer_time() {
			return _server_time;
		}
	}
	
	public ChatResult getChatResult(){
		return new ChatResult();
	}
	public class ChatResult {
		private final Pattern _chatResultPattern = Pattern.compile("<chat_result thread=\"(.+)\" status=\"([0-9])\" no=\"([0-9])+\"/>");
		private String _thread;
		private String _status;
		private String _no;
		private boolean _isNextPostkey = true;
		private boolean _sendMessage = false;
		private boolean _isChatResult = false;
		public ChatResult(){
		}
		public void setChatResult(String chatResult){
			Matcher matcher = _chatResultPattern.matcher(chatResult);
			if(matcher.matches()){
				_isChatResult = true;
				_thread = matcher.group(1);
				_status = matcher.group(2);
				_no = matcher.group(3);
				if (_status.equals("0")){
					_sendMessage = true;
					_isNextPostkey = false;
					return;
				}
				if (_status.equals("4")){
					_sendMessage = false;
					_isNextPostkey = true;
					return;
				}
				_sendMessage = false;
				_isNextPostkey = false;
				return;
			}
			
			_isChatResult = false;
			_sendMessage = false;
			_isNextPostkey = true;
		}
		public boolean isChatResult(){
			return _isChatResult;
		}
		public boolean isNextPostkey(){
			return _isNextPostkey;
		}
		public boolean sendMessage(){
			return _sendMessage;
		}
		public String getThread(){
			return _thread;
		}
		public String getStatus(){
			return _status;
		}
		public String getNo(){
			return _no;
		}
	}
	
	public CommentData getCommentData(PlayerStatusData playerStatusData){
		return new CommentData(playerStatusData);
	}
	public class CommentData {
		private final Pattern _commentChatpattern = Pattern.compile("<chat (.*)>([.\\s\\S]*)</chat>"); 
		private final Pattern _userIDPattern = Pattern.compile(".*user_id=\"(.*?)\".*");
		private final Pattern _noPattern = Pattern.compile(".*no=\"(.*?)\".*");
		private final Pattern _premiumPattern = Pattern.compile(".*premium=\"(.*?)\".*");
		private final Pattern _vposPattern = Pattern.compile(".*vpos=\"(.*?)\".*");
		private String _attribute;
		private String _comment;
		private String _no;
		private String _userID;
		private String _premium;
		private String _vpos;
		
		private String _baseTime;
		private String _startTime;
		
		public CommentData(PlayerStatusData playerStatusData){
			_baseTime = playerStatusData.get_base_time();
			_startTime  = playerStatusData.get_start_time();
		}
		public String[] getComment(String chatData){
			Matcher matcher = _commentChatpattern.matcher(chatData);
			if(matcher.matches()){
				_attribute = matcher.group(1);
				_comment = unescapeXML(matcher.group(2));
			}
			else {
				_attribute = "";
				_no = "";
				_userID = "";
				_comment = chatData;
			}
			
			matcher = _userIDPattern.matcher(_attribute);
			if(matcher.matches()){
				_userID = matcher.group(1);
			}
			
			matcher = _premiumPattern.matcher(_attribute);
			if(matcher.matches()){
				_premium = matcher.group(1);
			}
			
			matcher = _vposPattern.matcher(_attribute);
			if(matcher.matches()){
				_vpos = getTimes(matcher.group(1));
			}
			
			matcher = _noPattern.matcher(_attribute);
			if(matcher.matches()){
				_no = matcher.group(1);
			}
			else {
				_no = _vpos;
			}
			
			
			return new String[]{ _no, _userID, _comment };
		}
		public String getComment() {
			return _comment;
		}
		public String getNo() {
			return _no;
		}
		public String getUserID() {
			return _userID;
		}
		public String getPremium() {
			return _premium;
		}
		public String get_vpos() {
			return _vpos;
		}
		private String getTimes(String vpos){
			int difference = (Integer.valueOf(_baseTime) - Integer.valueOf(_startTime))*100;
			int times = Integer.valueOf(vpos) + difference;
			
			String h = String.valueOf(times/360000);
			String m = getPrefix(String.valueOf(times%360000/6000));
			String s = getPrefix(String.valueOf(times%6000/100));
			if (h.equals("0")){
				return String.format("%1$s:%2$s", m,s);
			}
			return String.format("%1$s:%2$s:%3$s", h,m,s);
		}
		private String getPrefix(String data){
			if (data.length() == 1){
				return "0" + data;
			}
			return data;
		}
	}
	
	public SendCommentData getSendCommentData(PlayerStatusData playerStatusData){
		return new SendCommentData(playerStatusData);
	}
	public class SendCommentData {
		private final String _sendCommentFormat = "<chat thread=\"{0}\" ticket=\"{1}\" vpos=\"{2}\" postkey=\"{3}\" mail=\"{4}\" user_id=\"{5}\" premium=\"{6}\" locale=\"jp\">{7}</chat>\0";
		private String _thread;
		private String _ticket;
		private String _postkey;
		private String _mail;
		private String _user_id;
		private String _premium;
		private String _sendComment;
		private String _baseTime;
		private String _vpos;
		public SendCommentData(PlayerStatusData playerStatusData){
			_thread = playerStatusData.getThread();
			_baseTime = playerStatusData.get_base_time();
			_user_id = playerStatusData.getUserID();
			_premium = playerStatusData.getIsPremium();
		}
		public String getSendComment(String postkey, String mail, String sendComment){
			if (postkey == null || postkey.isEmpty()){

			}
			else {
				_postkey = postkey;
			}
			if (mail == null || mail.isEmpty()){ 
				_mail = "184";
			}
			else {
				_mail = mail;
			}
			_vpos = get_vpos(_baseTime);
			_sendComment = escapeXML(sendComment);
			return MessageFormat.format(_sendCommentFormat, _thread, _ticket, _vpos, _postkey, _mail, _user_id, _premium, _sendComment);
		}
		public String getResendComment(String postkey){
			if (postkey == null || postkey.isEmpty()){

			}
			else {
				_postkey = postkey;
			}
			return MessageFormat.format(_sendCommentFormat, _thread, _ticket, _vpos, _postkey, _mail, _user_id, _premium, _sendComment);
		}
		public void setTicket(ThreadData threadData){
			_ticket = threadData.getTicket();
			_baseTime = String.valueOf(Integer.valueOf(_baseTime) + Integer.valueOf(get_vpos(threadData.getServer_time()))/100);
		}
	}
}