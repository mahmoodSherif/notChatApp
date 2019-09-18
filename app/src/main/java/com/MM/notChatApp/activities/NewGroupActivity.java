package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.Group;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.pass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NewGroupActivity extends AppCompatActivity {

    ListView newGroupList;
    Set<Integer> selectedUsers;
    ArrayList<String> users = new ArrayList<>();
    FloatingActionButton checkFab;
    String groupName;
    Uri groupPhoto;
    String userPhone;

    DatabaseReference chatsRef , chatListRef , usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        initUI();

        chatListRef = FirebaseDatabase.getInstance().getReference().child("chatList");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    }

    private void initUI(){
        newGroupList = findViewById(R.id.newGroupList);
        selectedUsers = new HashSet<>();
        checkFab = findViewById(R.id.checkFab);
        Log.e("list", String.valueOf(pass.list.size()));
        friendsAdapter friendsAdapter = new friendsAdapter(this,R.layout.friends_list_item, pass.list);
        newGroupList.setAdapter(friendsAdapter);

        checkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int pos : selectedUsers){
                    users.add(pass.list.get(pos).getPhone());
                }
                createGroup();
                Intent intent = new Intent(NewGroupActivity.this,ChatActivity.class);
            }
        });

        newGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selectedUsers.contains(i)){
                    selectedUsers.remove(i);
                    view.findViewById(R.id.check).setVisibility(View.GONE);
                }else{
                    selectedUsers.add(i);
                    view.findViewById(R.id.check).setVisibility(View.VISIBLE);
                }

            }
        });
    }
    private void createGroup(){
        users.add(userPhone);
        final Group group = new Group(null,"name test","photo test",users);
        DatabaseReference groupRef = chatsRef.push();
        final String id = groupRef.getKey();
        for(String cur : users){
            chatListRef.child(cur).child(id).setValue(group);
        }
    }
}
