package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);
        if(!message.getSentby().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_res, parent, false);
            TextView messageTextView = convertView.findViewById(R.id.MessageRes);
            TextView timeTextView = convertView.findViewById(R.id.MessageTimeRes);

            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());


            return convertView;
        }else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_send, parent, false);
            TextView messageTextView = convertView.findViewById(R.id.MessageSend);
            TextView timeTextView = convertView.findViewById(R.id.MessageTimeSend);
            ImageView status = convertView.findViewById(R.id.MessageStatues);

            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());
            if(message.getStatues() == 3)
            {
                status.setImageResource(R.drawable.read16);
            }

            return convertView;
        }

    }
}

