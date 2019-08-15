package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.MessageAdapter;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    // activity views
    private MessageAdapter messageAdapter;
    private ListView messagesListView;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private ChildEventListener messagesListener;
    boolean typing = false;
    // bar views
    private TextView BarfriendName;
    private CircleImageView BarFriendImage;
    private TextView friendStatus;

    // firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    // cur chat info
    String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    String friendPhone = null;
    Uri photo = null;
    String friendname = null;
    boolean readMessage = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        readMessage = true;
        // intent
        if(getIntent().getExtras()!=null) {
            friendname = getIntent().getExtras().getString("username");
            photo = Uri.parse(getIntent().getExtras().getString("userPhoto"));
            friendPhone = getIntent().getExtras().getString("phone");
        }


        //custom Bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_bar);
        final View view = getSupportActionBar().getCustomView();

        // bar views
        BarfriendName = view.findViewById(R.id.BarfriendName);
        BarFriendImage = view.findViewById(R.id.BarFriendImage);
        friendStatus = view.findViewById(R.id.BarFriendLastSeen);


        messagesListView = findViewById(R.id.messagesList);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        List<Message> messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this,R.layout.message_item_res,messages);
        messagesListView.setAdapter(messageAdapter);

        // set friend info
        BarfriendName.setText(friendname);
        Glide.with(ChatActivity.this)
                .load(photo)
                .into(BarFriendImage);
        checkStatus();
        showTyping();
        setSeenIndecator(true);


        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable.toString())&&editable.toString().trim().length()==1) {
                    typing = true;
                    setTypingIndecator(true);
                } else if(editable.toString().trim().length()==0 && typing){
                    typing = false;
                    setTypingIndecator(false);
                }
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        ensureChatId();
    }

    private void setSeenIndecator(final Boolean seen)
    {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone)
                .child(FirebaseAuth.getInstance()
                        .getCurrentUser().getPhoneNumber()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                        {
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).child("seen").setValue(seen);
                        }
                        else
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("seen",seen);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).updateChildren(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }
    private void setTypingIndecator(final Boolean typing) {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone)
                .child(FirebaseAuth.getInstance()
                                .getCurrentUser().getPhoneNumber())
                .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                        {
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).child("typing").setValue(typing);
                        }
                        else
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("typing",typing);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).updateChildren(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void ensureChatId(){
        FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone).child("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String CurChatId = null;
                        if(dataSnapshot.getValue() == null){
                            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").push();
                            String chatId = chatRef.getKey();
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone)
                                    .child("id").setValue(chatId);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(userPhone)
                                    .child("id").setValue(chatId);
                            CurChatId = chatId;
                        }else{
                            CurChatId = dataSnapshot.getValue().toString();
                        }
                        getChatMessages(CurChatId);
                        setOnClickListenerForSendButton(CurChatId);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setOnClickListenerForSendButton(final String CurChatId){
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String text = mMessageEditText.getText().toString();
                final SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
                FirebaseDatabase.getInstance().getReference().child("chatList")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                        .child(friendPhone).child("seen").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null)
                                {
                                    if(dataSnapshot.getValue().equals(true))
                                    {

                                        Message message = new Message(text,
                                                Time.format(new Date())  ,null , 3,userPhone);

                                        FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                                                .push().setValue(message);
                                    }
                                    else
                                    {
                                        Message message = new Message(text,
                                                Time.format(new Date())  ,null , 2,userPhone);

                                        FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                                                .push().setValue(message);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }
                );
                mMessageEditText.setText("");
            }
        });
    }
    private void getChatMessages(String id) {
        FirebaseDatabase.getInstance().getReference().child("chats").child(id)
                .addChildEventListener( new ChildEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        LayoutInflater inflater = getLayoutInflater();
                        //View view = inflater.inflate(R.layout.message_item_res,null);
                        Message message = dataSnapshot.getValue(Message.class);
                        messageAdapter.add(message);
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setSeenIndecator(true);
        readMessage = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageAdapter.clear();
        setSeenIndecator(false);
        readMessage = false;
    }
    private void checkStatus()
    {
        FirebaseDatabase.getInstance().getReference().child("users").child(friendPhone).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user.getUserStatues().equals("online"))
                        {
                            friendStatus.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            //friendStatus.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }
    private void showTyping()
    {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
        ).child(friendPhone).child("typing").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            if (dataSnapshot.getValue().equals(true)) {
                                friendStatus.setVisibility(View.VISIBLE);
                                friendStatus.setText(R.string.typing);
                            }
                            else
                            {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

}
