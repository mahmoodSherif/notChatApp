package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.MM.notChatApp.R;
import com.MM.notChatApp.activities.MainActivity;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.notChatApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class MessageAdapter extends ArrayAdapter<Message> {
    private List<Message> userList;
    private SparseBooleanArray mSelectedItemsIds;
    private MediaPlayer player = null;

    private boolean group = false;
    private int lastPos = -1;
    private Runnable runnable;
    private Handler handler = new Handler();
    private String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        userList = objects;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public MessageAdapter(Context context, int resource, List<Message> objects, boolean group) {
        super(context, resource, objects);
        userList = objects;
        mSelectedItemsIds = new SparseBooleanArray();
        this.group = group;
    }

    private TextView messageTextView;
    private TextView timeTextView;
    private ImageView photoMessage;
    private View audioView;
    private View docView;
    private ImageButton docImage;
    private TextView docText;
    private TextView senderName;

    private boolean byMe;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Message message = getItem(position);
        boolean isPhoto = message.getPhotoUrl() != null;
        boolean isAudio = message.getAudioUrl() != null;
        boolean isDoc = message.getDocUrl() != null;
        byMe = message.getSentby().equals(userPhone);
        if(isPhoto){
            return getPhotoMessage(message, parent);
        }else if(isAudio){
            return getAudioMessage(message, parent , position);
        }else if(isDoc){
            return getDocMessage(message, parent);
        }else{
            return getTextMessage(message, parent);
        }
    }

    private View getTextMessage(Message message, ViewGroup parent) {
        View convertView;
        if(byMe){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_send, parent, false);
            stut(convertView , message);
        }
        else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_res, parent, false);
            if(group){
                senderName = convertView.findViewById(R.id.Group_FriendName);
                User user = notChatApp.allUsers.get(message.getSentby());
                senderName.setText(user.getUserName());
            }
        }

        messageTextView = convertView.findViewById(R.id.Message);
        timeTextView = convertView.findViewById(R.id.MessageTime);

        messageTextView.setText(message.getText());
        timeTextView.setText(message.getTime());
        return convertView;
    }

    private View getPhotoMessage(Message message, ViewGroup parent){
        View convertView;
        if(byMe){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.photo_send_layout, parent, false); // TODO make UI
            stut(convertView , message);
        }
        else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.photo_res_layout, parent, false); // TODO make UI
            if(group){
                senderName = convertView.findViewById(R.id.Group_FriendName);
                User user = notChatApp.allUsers.get(message.getSentby());
                senderName.setText(user.getUserName());
            }
        }
        timeTextView = convertView.findViewById(R.id.MessageTime);
        timeTextView.setText(message.getTime());
        photoMessage = convertView.findViewById(R.id.image);
        messageTextView = convertView.findViewById(R.id.Message);
        messageTextView.setText(message.getText());
        Log.v("photo" , message.getText());
        Glide.with(photoMessage.getContext())
                .load(message.getPhotoUrl())
                .placeholder(R.drawable.user_empty_photo)
                .error(R.drawable.user_empty_photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(photoMessage);
        return convertView;
    }
    private View getAudioMessage(final Message message, ViewGroup parent , final int position)  {
        View convertView;
        if(byMe){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.audio_layout, parent, false); // TODO make UI
            stut(convertView , message);
        }
        else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.audio_layout_res, parent, false); // TODO make UI
            if(group){
                senderName = convertView.findViewById(R.id.Group_FriendName);
                User user = notChatApp.allUsers.get(message.getSentby());
                senderName.setText(user.getUserName());
            }
        }
        //audioView = convertView.findViewById(R.id.Group_resAudioInclue);
        final ImageButton playAudio = convertView.findViewById(R.id.btnPlay);
        final SeekBar seekBar = convertView.findViewById(R.id.seekBar);
        timeTextView = convertView.findViewById(R.id.MessageTime);
        timeTextView.setText(message.getTime());
        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler = new Handler();
                //if user clicks same record
                if (lastPos == position) {
                    if (player == null) {
                        Toast.makeText(getContext(), "same but null", Toast.LENGTH_LONG).show();
                        player = new MediaPlayer();
                        try {
                            player.setDataSource(message.getAudioUrl());

                        } catch (IOException e) {
                            Toast.makeText(getContext(), "HERE", Toast.LENGTH_LONG).show();
                        }
                        player.prepareAsync();
                        playAudio.setImageResource(R.drawable.pauseaudio);
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                seekBar.setMax(player.getDuration());
                                player.start();
                                seekBar.setProgress(player.getCurrentPosition());
                            }
                        });
                    } else if (player.isPlaying()) {
                        player.pause();
                        playAudio.setImageResource(R.drawable.playaudio);
                    } else {
                        player.start();
                        playAudio.setImageResource(R.drawable.pauseaudio);
                    }
                } else {
                    if (player != null) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                    player = new MediaPlayer();
                    Toast.makeText(getContext(), "new", Toast.LENGTH_LONG).show();
                    try {
                        player.setDataSource(message.getAudioUrl());

                    } catch (IOException e) {
                        Toast.makeText(getContext(), "HERE", Toast.LENGTH_LONG).show();
                    }
                    player.prepareAsync();
                    playAudio.setImageResource(R.drawable.pauseaudio);
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            seekBar.setMax(player.getDuration());
                            player.start();
                            seekBar.setProgress(player.getCurrentPosition());
                        }
                    });
                }
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playAudio.setImageResource(R.drawable.playaudio);
                        player.stop();
                        player.release();
                        player = null;
                    }
                });
                lastPos = position;
            }
        });
        return convertView;
    }
    private View getDocMessage(final Message message, ViewGroup parent ){
        View convertView;
        if(byMe){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.doc_layout, parent, false); // TODO make UI
            stut(convertView , message);
        }
        else{
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.doc_layout_res, parent, false); // TODO make UI
            if(group){
                senderName = convertView.findViewById(R.id.Group_FriendName);
                User user = notChatApp.allUsers.get(message.getSentby());
                senderName.setText(user.getUserName());
            }
        }
        //docView = convertView.findViewById(R.id.Group_resDocLayout);
        docImage = convertView.findViewById(R.id.docIcon);
        docText = convertView.findViewById(R.id.docText);
        timeTextView = convertView.findViewById(R.id.MessageTime);
        timeTextView.setText(message.getTime());
        docImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opening the upload file in browser using the upload url
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(message.getDocUrl()));
                getContext().startActivity(intent);
            }
        });
        return convertView;
    }


    private void stut(View convertView , Message message){
        ImageView MessageStatues = convertView.findViewById(R.id.MessageStatues);
        if(message.getStatues() == 3){
            Glide.with(MessageStatues.getContext())
                    .load(R.drawable.read16)
                    .placeholder(R.drawable.read16)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(MessageStatues);
        }
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
        if (value) {
            mSelectedItemsIds.put(position, true);
        } else {
            mSelectedItemsIds.delete(position);
        }

        // notifyDataSetChanged();
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

