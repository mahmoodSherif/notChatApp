package com.MM.notChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.MM.notChatApp.adapters.MessageAdapter;
import com.MM.notChatApp.classes.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    // activity views
    private MessageAdapter messageAdapter;
    private ListView messagesListView;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    // bar views
    private TextView BarfriendName;
    private CircleImageView BarFriendImage;

    // firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    // cur chat info
    private String CurChatId = null;
    String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    String friendPhone = null;
    Uri photo = null;
    String friendname = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // intent
        friendname = getIntent().getExtras().getString("username");
        photo = Uri.parse(getIntent().getExtras().getString("userPhoto"));
        friendPhone =getIntent().getExtras().getString("phone");

        //custom Bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_bar);
        View view = getSupportActionBar().getCustomView();

        // bar views
        BarfriendName = view.findViewById(R.id.BarfriendName);
        BarFriendImage = view.findViewById(R.id.BarFriendImage);


        messagesListView = findViewById(R.id.messagesList);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // set friend info
        BarfriendName.setText(friendname);
        Glide.with(BarFriendImage.getContext())
                .load(photo)
                .into(BarFriendImage);

        List<Message> messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,R.layout.message_item,messages);
        messagesListView.setAdapter(messageAdapter);

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
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO write to the database
                Message message = new Message(mMessageEditText.getText().toString(),
                        Calendar.getInstance().getTime().toString(),null , 0 ,userPhone);

                FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                        .push().setValue(message);
                mMessageEditText.setText("");
            }
        });


        FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null){
                            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").push();
                            String chatId = chatRef.getKey();
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone)
                                    .setValue(chatId);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(userPhone)
                                    .setValue(chatId);
                            CurChatId = chatId;
                        }else{
                            CurChatId = dataSnapshot.getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});


    }
}
