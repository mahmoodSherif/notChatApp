package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    private List<Message>userList;
    private SparseBooleanArray mSelectedItemsIds;
    private  MediaPlayer player;
    private int click = 0;
    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        userList = objects;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Message message = getItem(position);
        boolean isPhoto = message.getPhotoUrl()!=null && !message.getPhotoUrl().equals("IsDOC");
        boolean isDoc = false;
        boolean isAudio = message.getAudioUrl()!=null;
        if(message.getPhotoUrl()!=null && message.getPhotoUrl().equals("IsDOC") )
        {
             isDoc = true;   
        }
        if(!message.getSentby().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_res, parent, false);
            TextView messageTextView = convertView.findViewById(R.id.MessageRes);
            TextView timeTextView = convertView.findViewById(R.id.MessageTimeRes);
            ImageView photoMessage = convertView.findViewById(R.id.imageRes);
            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());
            CardView cardView = null;
            if(isPhoto)
            {
                if(message.getText()==null)
                {
                    messageTextView.setVisibility(View.GONE);
                }
                else {
                    messageTextView.setVisibility(View.VISIBLE);
                }
                photoMessage.setVisibility(View.VISIBLE);
                Glide.with(photoMessage.getContext())
                        .load(message.getPhotoUrl())
                        .placeholder(R.drawable.user_empty_photo)
                        .error(R.drawable.user_empty_photo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(photoMessage);
            }
           /* else if(isDoc)
            {
                 cardView = convertView.findViewById(R.id.resCardView);
                TextView docTxt = convertView.findViewById(R.id.resDoctxt);
                docTxt.setText(message.getText());
             //   cardView.setVisibility(View.VISIBLE);
            }*/
            else {
                photoMessage.setVisibility(View.GONE);
              //  cardView.setVisibility(View.GONE);
            }

            return convertView;
        }else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_send, parent, false);
            TextView messageTextView = convertView.findViewById(R.id.MessageSend);
            TextView timeTextView = convertView.findViewById(R.id.MessageTimeSend);
            ImageView status = convertView.findViewById(R.id.MessageStatues);
            ImageView photoMessage = convertView.findViewById(R.id.image);
            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());
            View audioView = convertView.findViewById(R.id.audioInclue);
           ImageButton playAudio = audioView.findViewById(R.id.btnPlay);
            SeekBar seekBar = audioView.findViewById(R.id.seekBar);
            if(message.getStatues() == 3)
            {
                status.setImageResource(R.drawable.read16);
            }
            if(isPhoto)
            {
                if(message.getText()==null)
                {
                    messageTextView.setVisibility(View.GONE);
                }
                else {
                    messageTextView.setVisibility(View.VISIBLE);
                }
                photoMessage.setVisibility(View.VISIBLE);
                Glide.with(photoMessage.getContext())
                        .load(message.getPhotoUrl())
                        .placeholder(R.drawable.user_empty_photo)
                        .error(R.drawable.user_empty_photo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(photoMessage);
            }
            else if(isAudio)
            {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                //mediaMetadataRetriever.setDataSource(message.getAudioUrl());
                //String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                //mediaMetadataRetriever.release();
                player = new MediaPlayer();
                seekBar.setMax(player.getDuration());
                playAudio.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
               // seekBar.setProgress(seekBar.getProgress()+1);
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        click++;
                        if(click%2!=0) {
                            startPlaying(message.getAudioUrl());
                        }
                        if(click%2==0)
                        {
                            stopPlaying();
                        }
                    }
                });
            }
           /* else if(isDoc)
            {
                cardView = convertView.findViewById(R.id.resCardView);
                TextView docTxt = convertView.findViewById(R.id.resDoctxt);
                docTxt.setText(message.getText());
                //   cardView.setVisibility(View.VISIBLE);
            }*/
            else {
                photoMessage.setVisibility(View.GONE);
                playAudio.setVisibility(View.GONE);
                audioView.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
            }

            return convertView;
        }

    }
    private void startPlaying(String fileName) {
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }
    }
    private void stopPlaying() {
        player.release();
        player = null;
    }

    @Override
    public void add(Message object) {
        userList.add(object);
        notifyDataSetChanged();
    }

    @Override
    public void remove(Message object) {
        userList.remove(object);
        notifyDataSetChanged();
    }
    public void selectView(int position, boolean value) {
        if(value) {
            mSelectedItemsIds.put(position, true);
        }
        else {
            mSelectedItemsIds.delete(position);
        }

        notifyDataSetChanged();
    }
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }
}

