package com.MM.notChatApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.MM.notChatApp.adapters.MessagesListAdapter;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.dialogs.searchForNewFriend;
import com.MM.notChatApp.user.setUserNameForFirstTime;
import com.MM.notChatApp.user.userInfo;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // firebase consts
    private static final int RC_SIGN_IN = 123;
    private SwipeMenuListView MainListView;
    //adapter
    MessagesListAdapter messagesListAdapter;
    FirebaseAuth mfirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatListRef;
    private ChildEventListener childEventListener;
    private ChildEventListener MessagegsEventListener;
    private SwipeMenuCreator creator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDatabase = FirebaseDatabase.getInstance();

        MainListView = findViewById(R.id.listView);
        FloatingActionButton fab = findViewById(R.id.fab);

        List<User> usersList = new ArrayList<>();
        messagesListAdapter = new MessagesListAdapter(this, R.layout.main_listview_item, usersList);
        MainListView.setAdapter(messagesListAdapter);
        MainListView.setEmptyView(findViewById(R.id.layoutEmpty));

        setUpSwipeMenuCreator();

        MainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("username", user.getUserName());
                intent.putExtra("phone", user.getPhone());
                intent.putExtra("userPhoto", user.getUserPhotoUrl());
                startActivity(intent);
            }
        });

        mfirebaseAuth = FirebaseAuth.getInstance();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    getChatList();
                } else {
                    signIn();

                }
            }
        };
    }


    private void setUpSwipeMenuCreator(){
        creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(170);
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.delete_icon);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        MainListView.setMenuCreator(creator);
        MainListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        Intent intent = new Intent(MainActivity.this,userInfo.class);
                        User user = messagesListAdapter.getItem(position);
                        intent.putExtra("photo",user.getUserPhotoUrl());
                        intent.putExtra("name",user.getUserName());
                        intent.putExtra("bio",user.getUserBio());
                        intent.putExtra("phone",user.getPhone());
                        startActivity(intent);
                        break;
                    case 1:
                        // delete
                        Toast.makeText(getApplicationContext(),"one",Toast.LENGTH_SHORT).show();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void getChatList() {
        //Listener to get users friends that he talked to
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String friendPhone = dataSnapshot.getKey();
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
                                        friendUser.setLastMessage(LastMessage);
                                        int check = 0;
                                        for (int i = 0; i < messagesListAdapter.getCount(); i++) {
                                            User myuser = messagesListAdapter.getItem(i);
                                            if (myuser.getPhone().equals(friendPhone)) {
                                                check = -1;
                                                break;
                                            }
                                        }
                                        if (check != -1) {
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

    private void status(String status) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("UserStatues", status);
        FirebaseDatabase.getInstance().getReference().child("users").child(
                mfirebaseAuth.getCurrentUser().getPhoneNumber()
        ).updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mfirebaseAuth.addAuthStateListener(mAuthStateListener);
//        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mfirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        messagesListAdapter.clear();
  //      status("offline");
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
        if (id == R.id.addNewFriend) {
            Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.signOut) {
            signOut();
            return true;
        }
        if (id == R.id.info) {
            Intent intent = new Intent(MainActivity.this, userInfo.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // The user is new, show them a fancy intro screen!
                    Toast.makeText(MainActivity.this, "first Time", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, setUserNameForFirstTime.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "NOT first Time", Toast.LENGTH_LONG).show();
                }

            } else {
                finish();
            }
        }
    }

    //auth functions
    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "signed Out", Toast.LENGTH_LONG).show();
                    }
                });
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void CheckConnection(String phone)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("users/"+phone+"/connections");

// Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/users/"+phone+"/lastOnline");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    DatabaseReference con = myConnectionsRef.push();

                    // When this device disconnects, remove it
                    con.onDisconnect().removeValue();

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(Boolean.TRUE);
                    Toast.makeText(getApplicationContext(),"connected",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
