package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.blockAdapter;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.pass;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class blockedActivity extends AppCompatActivity {

    SwipeMenuListView blockedList;
    ArrayList<User> blockedUsers;
    String myPhone;
    blockAdapter adapter;
    boolean ok ;

    private SwipeMenuCreator creator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);
        blockedList = findViewById(R.id.blockedList);
        blockedList.setEmptyView(findViewById(R.id.layoutEmpty));
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null) {
            myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        }

        blockedUsers = new ArrayList<>();
        adapter = new blockAdapter(this,R.layout.friends_list_item,blockedUsers);
        blockedList.setAdapter(adapter);
        getBlockedContacts();
        SwipeList();
    }

    private void SwipeList(){
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
                openItem.setTitle("Unblock");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(R.color.colorAccent);
                // add to menu
                menu.addMenuItem(openItem);

            }
        };
        blockedList.setMenuCreator(creator);
        blockedList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index)
                {
                    case 0:
                        User selectedUser = (User) blockedList.getItemAtPosition(position);
                        UnBlockContact(selectedUser);
                        break;

                }
                return false;
            }
        });
    }

    private boolean UnBlockContact(final User user) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(blockedActivity.this);
        builder.setMessage("Do you want to unblock this contact ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pass.userRef.child(myPhone).child("blocked by me").child(user.getPhone()).setValue(null);
                        pass.userRef.child(user.getPhone()).child("blocking me").child(myPhone).setValue(null);
                        pass.chatListRef.child(myPhone).child(user.getPhone()).child("block").setValue(null);
                        pass.chatListRef.child(user.getPhone()).child(myPhone).child("block").setValue(null);
                        adapter.remove(user);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               // builder.dismiss();
                ok = false;
            }
        });
        builder.show();
        return ok;
    }


    private void getBlockedContacts()
    {
        pass.userRef.child(myPhone).child("blocked by me").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot bloced :dataSnapshot.getChildren())
                {
                    String userId = bloced.getKey();
                    getBlocedUserInfo(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getBlocedUserInfo(String userPhone) {
        pass.userRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                adapter.add(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
