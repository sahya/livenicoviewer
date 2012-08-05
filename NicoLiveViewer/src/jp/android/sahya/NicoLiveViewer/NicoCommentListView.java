package jp.android.sahya.NicoLiveViewer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NicoCommentListView {
	private ListView listView;
	private CommentAdapter adapter;
	
	public NicoCommentListView(ListView listView, Context context){
		this.listView = listView;
		adapter = new CommentAdapter(context, R.layout.comment_list_row);
		setNicoCommentListView();
	}
	private void setNicoCommentListView(){
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);
	}
	public void append(String[] comment){
		adapter.add(new CommentData(comment[0], comment[1], comment[2]));
		adapter.notifyDataSetChanged();
		listView.smoothScrollToPosition(listView.getCount());
	}
	
	private class CommentData {
		public String No;
		public String UserID;
		public String Comment;
		public CommentData(String no, String userID, String comment){
			No = no;
			UserID = userID;
			Comment = comment;
		}
	}
	
	private class CommentAdapter extends BaseAdapter {
		private final int oddColor = Color.rgb(0, 0, 0);
		private final int evenColor = Color.rgb(50, 50, 50);
		
		private Context context;
		private int textViewResourceId;
		private List<CommentData> list = new ArrayList<CommentData>();
		
		public CommentAdapter(Context context, int textViewResourceId) {
			this.context = context;
			this.textViewResourceId = textViewResourceId;
		}
		public void add(CommentData data){
			list.add(data);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(textViewResourceId, null);
			TextView tvNo = (TextView)view.findViewById(R.id.commentNo);
			TextView tvUserID= (TextView)view.findViewById(R.id.commentUserID);
			TextView tvComment= (TextView)view.findViewById(R.id.commentText);
			
			tvNo.setText(list.get(position).No);
			tvUserID.setText(list.get(position).UserID);
			tvComment.setText(list.get(position).Comment);
			
			if (position%2 == 0) {
				view.setBackgroundColor(evenColor);
			}
			else {
				view.setBackgroundColor(oddColor);
			}
			
			return view;
		}
		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object getItem(int position) {
			return list.get(position);
		}
		@Override
		public long getItemId(int position) {
			return list.indexOf(list.get(position));
		}
	}
}