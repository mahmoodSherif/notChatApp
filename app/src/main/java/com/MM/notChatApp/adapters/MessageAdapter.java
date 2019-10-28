package com.MM.notChatApp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.notChatApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<Message> {
    private List<Message> userList;
    private SparseBooleanArray mSelectedItemsIds;
    private MediaPlayer player = null;

    private boolean group = false;
    private int lastPos = -1;
    private Runnable runnable;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        userList = objects;
        mSelectedItemsIds = new SparseBooleanArray();
    }
    public MessageAdapter(Context context, int resource, List<Message> objects,boolean group) {
        super(context, resource, objects);
        userList = objects;
        mSelectedItemsIds = new SparseBooleanArray();
        this.group = group;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Message message = getItem(position);
        if (message.getPhotoUrl() != null) {
            Log.e("photo :: ", message.getPhotoUrl());
        } else {
            Log.e("photo :: ", "nooooo ");
        }
        boolean isPhoto = message.getPhotoUrl() != null;
        boolean isAudio = message.getAudioUrl() != null;
        boolean isDoc = message.getDocUrl()!=null;

        //item views
        TextView messageTextView ;
        TextView timeTextView;
        ImageView photoMessage;
        View audioView;
        final ImageButton playAudio;
        View docView ;
        ImageButton docImage ;
        final TextView docText ;

        //is group
        if(group &&!message.getSentby().equals(userPhone))
        {
         convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.group_message_item, parent, false);
         TextView senderName = convertView.findViewById(R.id.Group_FriendName);
         messageTextView = convertView.findViewById(R.id.Group_MessageRes);
             timeTextView = convertView.findViewById(R.id.Group_MessageTimeRes);
             photoMessage = convertView.findViewById(R.id.Group_imageRes);
            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());
            User user = notChatApp.allUsers.get(message.getSentby());
            senderName.setText(user.getUserName());
            audioView = convertView.findViewById(R.id.Group_resAudioInclue);
            playAudio = audioView.findViewById(R.id.btnPlay);
            seekBar = audioView.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    seekBar.getProgress();
                    if(b)
                    {
                        player.seekTo(i*1000);
                        seekBar.setProgress(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            if (isPhoto) {
                if (message.getText() == null) {
                    messageTextView.setVisibility(View.GONE);
                } else {
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
            else if(isDoc)
            {
                docView = convertView.findViewById(R.id.Group_resDocLayout);
                docImage = docView.findViewById(R.id.docIcon);
                docText = docView.findViewById(R.id.docText);
                docView.setVisibility(View.VISIBLE);
                docImage.setVisibility(View.VISIBLE);
                docText.setVisibility(View.VISIBLE);
                docImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Message selectedm = getItem(position);
                        //Opening the upload file in browser using the upload url
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(selectedm.getDocUrl()));
                        getContext().startActivity(intent);
                    }
                });
            }
            else if(isAudio)
            {
                playAudio.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handler = new Handler();
                        Message mymessage = getItem(position);
                        //if user clicks same record
                        if(lastPos == position)
                        {
                            if (player==null)
                            {
                                Toast.makeText(getContext(),"same but null",Toast.LENGTH_LONG).show();
                                player = new MediaPlayer();
                                try {
                                    player.setDataSource(mymessage.getAudioUrl());

                                } catch (IOException e) {
                                    Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
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
                            else if(player.isPlaying())
                            {
                                player.pause();
                                playAudio.setImageResource(R.drawable.playaudio);
                            }
                            else {
                                player.start();
                                playAudio.setImageResource(R.drawable.pauseaudio);
                            }
                        }
                        else {
                            if (player!=null)
                            {
                                player.stop();
                                player.release();
                                player =null;
                            }
                            player = new MediaPlayer();
                            Toast.makeText(getContext(),"new",Toast.LENGTH_LONG).show();
                            try {
                                player.setDataSource(mymessage.getAudioUrl());

                            } catch (IOException e) {
                                Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
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
            }
            else {
                photoMessage.setVisibility(View.GONE);
            }
            if(getSelectedIds().get(position))
            {
                convertView.setBackgroundColor(Color.parseColor("#4C525A"));
            }
            else {
                convertView.setBackgroundColor(Color.parseColor("#242424"));
            }
        }
        else if (!message.getSentby().equals(userPhone)) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_res, parent, false);
             messageTextView = convertView.findViewById(R.id.MessageRes);
             timeTextView = convertView.findViewById(R.id.MessageTimeRes);
             photoMessage = convertView.findViewById(R.id.imageRes);
            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());

            audioView = convertView.findViewById(R.id.resAudioInclue);
            playAudio = audioView.findViewById(R.id.btnPlay);
            //final SeekBar seekBar = audioView.findViewById(R.id.seekBar);
            seekBar = audioView.findViewById(R.id.seekBar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    seekBar.getProgress();
                    if(b)
                    {
                        player.seekTo(i*1000);
                        seekBar.setProgress(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            if (isPhoto) {
                if (message.getText() == null) {
                    messageTextView.setVisibility(View.GONE);
                } else {
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
            else if(isDoc)
            {
                docView = convertView.findViewById(R.id.resDocLayout);
                docImage = docView.findViewById(R.id.docIcon);
                docText = docView.findViewById(R.id.docText);

                docView.setVisibility(View.VISIBLE);
                docImage.setVisibility(View.VISIBLE);
                docText.setVisibility(View.VISIBLE);
                docImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Message selectedm = getItem(position);
                        //Opening the upload file in browser using the upload url
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(selectedm.getDocUrl()));
                        getContext().startActivity(intent);
                    }
                });
            }
            else if(isAudio)
            {
                playAudio.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handler = new Handler();
                        Message mymessage = getItem(position);
                        //if user clicks same record
                        if(lastPos == position)
                        {
                            if (player==null)
                            {
                                Toast.makeText(getContext(),"same but null",Toast.LENGTH_LONG).show();
                                player = new MediaPlayer();
                                try {
                                    player.setDataSource(mymessage.getAudioUrl());

                                } catch (IOException e) {
                                    Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
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
                            else if(player.isPlaying())
                            {
                                player.pause();
                                playAudio.setImageResource(R.drawable.playaudio);
                            }
                            else {
                                player.start();
                                playAudio.setImageResource(R.drawable.pauseaudio);
                            }
                        }
                        else {
                            if (player!=null)
                            {
                                player.stop();
                                player.release();
                                player =null;
                            }
                            player = new MediaPlayer();
                            Toast.makeText(getContext(),"new",Toast.LENGTH_LONG).show();
                            try {
                                player.setDataSource(mymessage.getAudioUrl());

                            } catch (IOException e) {
                                Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
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
            }
            else {
                photoMessage.setVisibility(View.GONE);
            }

            if(getSelectedIds().get(position))
            {
                convertView.setBackgroundColor(Color.parseColor("#4C525A"));
            }
            else {
                convertView.setBackgroundColor(Color.parseColor("#242424"));
            }
          //  return convertView;

        }
        else {
            //send
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_item_send, parent, false);
             messageTextView = convertView.findViewById(R.id.MessageSend);
             timeTextView = convertView.findViewById(R.id.MessageTimeSend);
            ImageView status = convertView.findViewById(R.id.MessageStatues);
             photoMessage = convertView.findViewById(R.id.image);
            messageTextView.setText(message.getText());
            timeTextView.setText(message.getTime());

             audioView = convertView.findViewById(R.id.audioInclue);
             playAudio = audioView.findViewById(R.id.btnPlay);
           // final SeekBar seekBar = audioView.findViewById(R.id.seekBar);
            seekBar = audioView.findViewById(R.id.seekBar);
            //seekBar.setMax(player.getDuration());
             docView = convertView.findViewById(R.id.docLayout);
             docImage = docView.findViewById(R.id.docIcon);
             docText = docView.findViewById(R.id.docText);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b)
                    {
                        if(player!=null) {
                            player.seekTo(i);
                            seekBar.setProgress(i);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                   // handler.removeCallbacks(runnable);
                    //updateseek();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            if (message.getStatues() == 3) {
                status.setImageResource(R.drawable.read16);
            }

            if (isPhoto) {
                if (message.getText() == null) {
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
            else if(isDoc)
            {
                docView.setVisibility(View.VISIBLE);
                docImage.setVisibility(View.VISIBLE);
                docText.setVisibility(View.VISIBLE);
                docImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Message selectedm = getItem(position);
                        //Opening the upload file in browser using the upload url
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(selectedm.getDocUrl()));
                        getContext().startActivity(intent);
                    }
                });
            }
            else if (isAudio) {
                playAudio.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                playAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Message mymessage = getItem(position);
                        //if user clicks same record
                        if(lastPos == position)
                        {
                            if (player==null)
                            {
                                player = new MediaPlayer();
                                try {
                                    player.setDataSource(mymessage.getAudioUrl());

                                } catch (IOException e) {
                                    Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
                                }
                                player.prepareAsync();
                                playAudio.setImageResource(R.drawable.pauseaudio);
                                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        seekBar.setMax(player.getDuration());
                                        player.start();
                                        updateseek();

                                    }
                                });
                            }
                           else if(player.isPlaying())
                            {
                                player.pause();
                                playAudio.setImageResource(R.drawable.playaudio);
                            }
                            else {
                                player.start();
                                playAudio.setImageResource(R.drawable.pauseaudio);
                            }
                        }
                        else {
                            if (player!=null)
                            {
                                player.stop();
                                player.release();
                                player =null;
                            }
                            player = new MediaPlayer();
                            try {
                                player.setDataSource(mymessage.getAudioUrl());

                            } catch (IOException e) {
                                Toast.makeText(getContext(),"HERE",Toast.LENGTH_LONG).show();
                            }
                            player.prepareAsync();
                            playAudio.setImageResource(R.drawable.pauseaudio);
                            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    seekBar.setMax(player.getDuration());
                                    player.start();
                                    updateseek();

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
            }
            else {
                docView.setVisibility(View.GONE);
                photoMessage.setVisibility(View.GONE);
                playAudio.setVisibility(View.GONE);
                audioView.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                //docImage.setVisibility(View.GONE);
                //docText.setVisibility(View.GONE);
            }
            if(getSelectedIds().get(position))
            {
                convertView.setBackgroundColor(Color.parseColor("#4C525A"));
            }
            else {
                convertView.setBackgroundColor(Color.parseColor("#242424"));
            }
           // return convertView;
        }
        return convertView;
    }

    private void updateseek()
    {
        if(player!=null) {
            Toast.makeText(getContext(),String.valueOf(player.getCurrentPosition()),Toast.LENGTH_LONG).show();
            seekBar.setProgress(player.getCurrentPosition());
            handler.postDelayed(runnable,100);
            if(player.isPlaying())
            {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                     updateseek();
                    }
                };
            }
            else {
                seekBar.setProgress(0);
            }
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

