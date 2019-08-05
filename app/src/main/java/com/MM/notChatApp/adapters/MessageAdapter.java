package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item, parent, false);
        }
        ImageView imageViewMessage = convertView.findViewById(R.id.ImageMessage);
        TextView messageTextView = convertView.findViewById(R.id.Message);
        TextView timeTextView = convertView.findViewById(R.id.MessageTime);
        ImageView statuesTextView = convertView.findViewById(R.id.MessageStatues);
        LinearLayout layout = convertView.findViewById(R.id.messagelayout);

        statuesTextView.setVisibility(View.GONE);
        if(message.getSentby().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
            layout.setGravity(Gravity.END);
            statuesTextView.setVisibility(View.VISIBLE);
        }

        boolean isPhoto = message.getPhotoUrl() != null;

        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            imageViewMessage.setVisibility(View.VISIBLE);
            Glide.with(imageViewMessage.getContext())
                    .load(message.getPhotoUrl())
                    .into(imageViewMessage);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            imageViewMessage.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        timeTextView.setText(message.getTime());

        return convertView;
    }
}

