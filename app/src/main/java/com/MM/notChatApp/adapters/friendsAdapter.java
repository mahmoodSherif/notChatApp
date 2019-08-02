package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.User;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class friendsAdapter extends ArrayAdapter<User> {
    public friendsAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.friends_list_item, parent, false);
        }
        CircleImageView friendImage = convertView.findViewById(R.id.friendImage);
        TextView friendName = convertView.findViewById(R.id.friendName);
        TextView Bio = convertView.findViewById(R.id.FriendBio);

        User user = getItem(position);

        Glide.with(friendImage.getContext())
                .load(user.getUserPhotoUrl())
                .into(friendImage);
        friendName.setText(user.getUserName());
        Bio.setText(user.getUserBio());
        return convertView;
    }
}
