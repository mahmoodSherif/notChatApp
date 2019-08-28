package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.User;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesListAdapter extends ArrayAdapter<User> {
    private List<User>userList;
    public MessagesListAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        userList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.main_listview_item, parent, false);
        }
        CircleImageView friendImage = convertView.findViewById(R.id.MainfriendImage);
        TextView friendName = convertView.findViewById(R.id.MainfriendName);
        TextView friendLastMessage = convertView.findViewById(R.id.MainFriendMessage);
       // TextView LastMessageTime = convertView.findViewById(R.id.MainMessageTime);
        User user = getItem(position);
        Glide.with(friendImage.getContext())
                .load(user.getUserPhotoUrl())
                .into(friendImage);
        friendName.setText(user.getUserName());
        friendLastMessage.setText(user.getLastMessage());

        return convertView;
    }
}
