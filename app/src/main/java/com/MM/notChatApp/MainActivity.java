package com.MM.notChatApp;

import android.content.Intent;
import android.os.Bundle;

import com.MM.notChatApp.adapters.MessagesListAdapter;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.dialogs.searchForNewFriend;
import com.MM.notChatApp.user.setUserNameForFirstTime;
import com.MM.notChatApp.user.userInfo;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // firebase consts
    private static final int RC_SIGN_IN = 123;
    private ListView MainListView;
    //adapter
    MessagesListAdapter messagesListAdapter;
    FirebaseAuth mfirebaseAuth;
    // private ProgressBar mProgressBar;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatListRef;
    private ChildEventListener childEventListener;
    private ChildEventListener MessagegsEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDatabase = FirebaseDatabase.getInstance();
        MainListView = findViewById(R.id.MainListView);
        List<User> usersList= new ArrayList<>();
        messagesListAdapter = new MessagesListAdapter(this,R.layout.main_listview_item,usersList);
        MainListView.setAdapter(messagesListAdapter);
        MainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("username",user.getUserName());
                intent.putExtra("phone",user.getPhone());
                intent.putExtra("userPhoto",user.getUserPhotoUrl());
                startActivity(intent);
            }
        });

        mfirebaseAuth = FirebaseAuth.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,FriendsActivity.class);
                startActivity(intent);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Toast.makeText(MainActivity.this , "HI " + mfirebaseAuth.getCurrentUser().getDisplayName(),Toast.LENGTH_SHORT).show();
                    getChatList();
                }else{
                    signIn();

                }
            }
        };

    }
    private void getChatList()
    {
        //Listener to get users friends that he talked to
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    final String friendPhone = dataSnapshot.getKey();
                   Toast.makeText(getApplicationContext(),"fuck",Toast.LENGTH_SHORT).show();
                    final String chatId = dataSnapshot.getValue(String.class);
                    //get last message sent
                        MessagegsEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                Message message = dataSnapshot.getValue(Message.class);
                                final String LastMessage = message.getText();
                                //get user data
                                FirebaseDatabase.getInstance().getReference().child("users").child(friendPhone).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User friendUser = dataSnapshot.getValue(User.class);
                                                friendUser.setUserBio(LastMessage);
                                                int check = 0;
                                                for(int i=0;i<messagesListAdapter.getCount();i++)
                                                {
                                                    User myuser = messagesListAdapter.getItem(i);
                                                    if(myuser.getPhone().equals(friendPhone))
                                                    {
                                                        check = -1;
                                                        break;
                                                    }
                                                }
                                                if(check!=-1) {
                                                    messagesListAdapter.add(friendUser);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        FirebaseDatabase.getInstance().getReference().child("chats").
                                child(chatId).orderByKey().limitToLast(1).addChildEventListener(MessagegsEventListener);
                    }


                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("chatList").child(FirebaseAuth
            .getInstance().getCurrentUser().getPhoneNumber()).addChildEventListener(childEventListener);
        }


    @Override
    protected void onResume() {
        super.onResume();
        mfirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null) {
            mfirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        messagesListAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.addNewFriend)
        {
            Intent intent = new Intent(MainActivity.this,FriendsActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.signOut) {
            signOut();
            return true;
        }
        if(id == R.id.info){
            Intent intent = new Intent(MainActivity.this,userInfo.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.test){
            Intent intent = new Intent(MainActivity.this,setUserNameForFirstTime.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                if(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null){
                    Intent intent = new Intent(MainActivity.this, setUserNameForFirstTime.class);
                    startActivity(intent);
                }
            }else{
                finish();
            }
        }
    }

    //auth functions
    private void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
                );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }
    private void signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this , "signed Out" ,Toast.LENGTH_LONG).show();
                    }
                });
    }
}
