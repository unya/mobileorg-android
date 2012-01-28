package com.matburt.mobileorg.Gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.matburt.mobileorg.R;
import com.matburt.mobileorg.Parsing.NodeWrapper;
import com.matburt.mobileorg.Parsing.OrgDatabase;

public class OutlineCursorAdapter extends SimpleCursorAdapter {

	private OrgDatabase db;
	private Cursor cursor;
	
	public OutlineCursorAdapter(Context context, Cursor cursor, OrgDatabase db) {
		super(context, R.layout.outline_item, cursor, new String[] {}, new int[] {});
		this.cursor = cursor;
		this.db = db;
	}
	
	@Override
	public long getItemId(int position) {
		cursor.moveToPosition(position);
		return cursor.getInt(cursor.getColumnIndex("_id"));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Cursor c = getCursor();

		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.outline_item, parent, false);

		bindView(v, context, cursor);

		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		super.bindView(v, context, c);
		
		ViewHolder holder = (ViewHolder) v.getTag();
		
		if(holder == null) {
			holder = new ViewHolder();
			holder.orgItem = (TextView) v.findViewById(R.id.orgItem);
			holder.tagsLayout = (TextView) v.findViewById(R.id.tagsLayout);
		}

		NodeWrapper node = new NodeWrapper(c);

		String todo = node.getTodo();
		String name = node.getName();
		String priority = node.getPriority();
		String tags = node.getTags();
		
		SpannableStringBuilder itemText = new SpannableStringBuilder(name);
		
		if (name.startsWith("COMMENT"))
			itemText.setSpan(new ForegroundColorSpan(Color.GRAY), 0,
					"COMMENT".length(), 0);
		
		Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
		Matcher matcher = urlPattern.matcher(itemText);
		while(matcher.find()) {
			itemText.delete(matcher.start(), matcher.end());
			itemText.insert(matcher.start(), matcher.group(1));
		
			itemText.setSpan(new ForegroundColorSpan(Color.argb(255, 6, 69, 173)),
					matcher.start(), matcher.start() + matcher.group(1).length(), 0);	
			
			matcher = urlPattern.matcher(itemText);
		}
	
		if (priority != null && priority.isEmpty() == false) {
			Spannable prioritySpan = new SpannableString(priority + " ");
			prioritySpan.setSpan(new ForegroundColorSpan(Color.YELLOW), 0,
					priority.length(), 0);
			itemText.insert(0, prioritySpan);
		}
		
		if(todo.isEmpty() == false) {
			Spannable todoSpan = new SpannableString(todo + " ");
			
			if(db.isTodoActive(todo))
				todoSpan.setSpan(new ForegroundColorSpan(Color.RED), 0,
						todo.length(), 0);
			else
				todoSpan.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
						todo.length(), 0);
			itemText.insert(0, todoSpan);
		}
			
		holder.orgItem.setText(itemText);

		
		if(tags != null && tags.isEmpty() == false) {
			holder.tagsLayout.setTextColor(Color.GRAY);
			holder.tagsLayout.setText(tags);
		} else
			holder.tagsLayout.setVisibility(View.GONE);
		
		// TextView dateInfo = (TextView) v.findViewById(R.id.dateInfo);

		// // Setup date view
		// //if (TextUtils.isEmpty(dateInfo)) {
		// holder.dateInfo.setVisibility(View.GONE);
		// // } else {
		// // holder.dateInfo.setText(dateInfo);
		// // holder.dateInfo.setVisibility(View.VISIBLE);
		// // }
	}

	/**
	 * Used as part of the holding pattern.
	 * 
	 * The idea is to save the findViewById()'s into this container object to
	 * speed up the list adapter. setTag() and getTag() are used to bind and
	 * retrieve the container.
	 * 
	 */
	private static class ViewHolder {
		TextView orgItem;
		TextView tagsLayout;
//		TextView dateInfo;
	}
}
