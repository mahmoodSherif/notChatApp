package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
           }
        ImageView imageViewMessage = convertView.findViewById(R.id.ImageMessage);
        TextView messageTextView = convertView.findViewById(R.id.Message);
        TextView timeTextView = convertView.findViewById(R.id.MessageTime);
        ImageView statuesTextView =convertView.findViewById(R.id.MessageStatues);

        Message Message = getItem(position);
        boolean isPhoto = Message.getPhotoUrl()!=null;

        if(isPhoto)
        {
            messageTextView.setVisibility(View.GONE);
            imageViewMessage.setVisibility(View.VISIBLE);
            Glide.with(imageViewMessage.getContext())
                    .load(Message.getPhotoUrl())
                    .into(imageViewMessage);
        }
        else
        {
            messageTextView.setVisibility(View.VISIBLE);
            imageViewMessage.setVisibility(View.GONE);
            messageTextView.setText(Message.getText());
        }
        timeTextView.setText(Message.getTime());

        return convertView;
    }
}
