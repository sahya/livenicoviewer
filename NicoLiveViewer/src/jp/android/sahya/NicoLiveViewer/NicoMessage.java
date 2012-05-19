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
	private final String _chatMessage = "<thread thread=\"{0}\" version=\"20061206\" res_from=\"-1\"/>\0";
	private final Pattern chatpattern = Pattern.compile("<chat.*>(.*,.*,.*)</chat>");
	private final Pattern _commentChatpattern = Pattern.compile("<chat .* no=\"([0-9]*?)\" .* user_id=\"(.*?)\" .*>(.*)</chat>"); 
	private final Pattern _liveLvPattern = Pattern.compile("http://sp.live.nicovideo.jp/watch/(lv[0-9]+)");
	private final Pattern _liveComuPattern = Pattern.compile("http://sp.live.nicovideo.jp/watch/(co[0-9]+)");
	
	public NicoMessage(){
		
	}
	
	public Document getDocument(InputStream is) throws SAXException, IOException, ParserConfigurationException{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
	}
	
	public String[] getChatData(String chatdata) {
		Matcher matcher = chatpattern.matcher(chatdata);
		if(matcher.matches()){
			return matcher.group(1).split(",");
		}else{
			return new String[] { "", "", "" };
		}
	}
	
	public String getLiveID (String url){
		Matcher matcher = _liveLvPattern.matcher(url);
		if(matcher.matches()){
			return matcher.group(1);
		}
		
		matcher = _liveComuPattern.matcher(url);
		if (matcher.matches()){
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

	public String getChatMessage(String thread) {
		return MessageFormat.format(this._chatMessage, thread);
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
					String[] chat = getChatData(buff.toString());
					onReceive.onReceive(chat[0]+":"+chat[1]+":"+chat[2]);
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
	public String getChat(String chatdata){
		Matcher matcher = _commentChatpattern.matcher(chatdata);
		if(matcher.matches()){
			return matcher.group(1) + ":" + matcher.group(2) + ":" + matcher.group(3);
		}else{
			return "";
		}
	}
}