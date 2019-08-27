package com.MM.notChatApp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.MessagesListAdapter;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.user.setUserNameForFirstTime;
import com.MM.notChatApp.user.userInfo;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    // database References
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatListRef , usersRef ;

    // auth
    private FirebaseAuth mfirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // firebase consts
    private static final int RC_SIGN_IN = 123;

    //adapter
    private MessagesListAdapter messagesListAdapter;
    private List<User> usersList = new ArrayList<>();

    private SwipeMenuCreator creator;
    private SwipeMenuListView MainListView;

    // maps
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerHashMap = new HashMap<>();
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerHashMap = new HashMap<>();
    private HashMap<String ,String > chatIdMap = new HashMap<>();
    String userPhone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // database ref set
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatListRef = firebaseDatabase.getReference().child("chatList");
        usersRef = firebaseDatabase.getReference().child("users");

        MainListView = findViewById(R.id.listView);
        FloatingActionButton fab = findViewById(R.id.fab);

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
                String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                FirebaseMessaging.getInstance().subscribeToTopic("user_"+phone.substring(1));
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
                        Toast.makeText(getApplicationContext(),"zero",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // delete
                       deleteChat(position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }


    private void getChatList(){
        ChildEventListener chatsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String friendPhone = dataSnapshot.getKey();
                if(dataSnapshot.child("have messages").getValue(boolean.class)) {
                    final String chatId = dataSnapshot.child("id").getValue(String.class);
                    messagesListAdapter.add(new User(friendPhone));
                    chatIdMap.put(friendPhone, chatId);
                    makeFriendListeners(usersList.size() - 1, friendPhone);
                }
            }
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        DatabaseReference ref = chatListRef.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        ref.addChildEventListener(chatsListener);
        childEventListenerHashMap.put(ref, chatsListener);
    }

    private void makeFriendListeners(final int postion , final String friendPhone){
        makeFriendNameListener(postion, friendPhone);
        makeLastMessageListener(postion, friendPhone);
        makeFriendPhotoListener(postion, friendPhone);
    }

    private void makeLastMessageListener(final int postion , final String friendPhone){
        ValueEventListener lastMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String lastMessage = dataSnapshot.child("text").getValue(String.class);
                User user = messagesListAdapter.getItem(postion);
                user.setUserBio(lastMessage);
                usersList.set(postion, user);
                messagesListAdapter.notifyDataSetChanged();
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        DatabaseReference ref = chatListRef.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(friendPhone).child("lastMessage");
        ref.addValueEventListener(lastMessageListener);
        valueEventListenerHashMap.put(ref , lastMessageListener);
    }
    private void makeFriendNameListener(final int postion , final String friendPhone){
        ValueEventListener FriendNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.getValue(String.class);
                User user = messagesListAdapter.getItem(postion);
                user.setUserName(name);
                usersList.set(postion, user);
                messagesListAdapter.notifyDataSetChanged();
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        DatabaseReference ref = usersRef.child(friendPhone).child("userName");
        ref.addValueEventListener(FriendNameListener);
        valueEventListenerHashMap.put(ref , FriendNameListener);
    }
    private void makeFriendPhotoListener(final int postion , final String friendPhone){
        ValueEventListener FriendPhotoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String photoUrl = dataSnapshot.getValue(String.class);
                User user = messagesListAdapter.getItem(postion);
                user.setUserPhotoUrl(photoUrl);
                usersList.set(postion, user);
                messagesListAdapter.notifyDataSetChanged();
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        DatabaseReference ref = usersRef.child(friendPhone).child("userPhotoUrl");
        ref.addValueEventListener(FriendPhotoListener);
        valueEventListenerHashMap.put(ref , FriendPhotoListener);
    }





    @Override
    protected void onResume() {
        super.onResume();
        mfirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messagesListAdapter.clear();
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerHashMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerHashMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
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

    private void signOut()  {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "signed Out", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void CheckConnection(String phone) {
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

    private void deleteChat(final int postion){
        User curChatFriend = messagesListAdapter.getItem(postion);
        final String curPhone = curChatFriend.getPhone();
        final DatabaseReference ref = chatListRef.child(userPhone).child(curPhone);
        ref.child("have messages").setValue(false);

        final DatabaseReference refForChat = firebaseDatabase.getReference().child("chats").child(chatIdMap.get(curPhone));

        firebaseDatabase.getReference().child("chats").child(chatIdMap.get(curPhone)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot x : children){
                    if(x.child("have").getValue(String.class).equals("both")){
                        x.getRef().child("have").setValue(curPhone);
                    }else{
                        x.getRef().removeValue();
                    }
                }
                usersList.remove(postion);
                messagesListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
