package com.MM.notChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    ListView FriendsList;
    friendsAdapter adapter;
    ArrayList<String>numbers ;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    // Request code for READ_CONTACTS
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        firebaseDatabase = FirebaseDatabase.getInstance();
        FriendsList = findViewById(R.id.FriendsList);
        FriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               User selectedUser = (User) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(FriendsActivity.this,ChatActivity.class);
              //  intent.putExtra("userFromIntent", (Parcelable) selectedUser);
                intent.putExtra("userName",selectedUser.getUserName());
                intent.putExtra("userPhone",selectedUser.getPhone());
                intent.putExtra("userphotoUrl",selectedUser.getUserPhotoUrl());
            }
        });
        ArrayList<User> users = new ArrayList<>();
        numbers = new ArrayList<>();
        adapter = new friendsAdapter(this,R.layout.friends_list_item,users);
        FriendsList.setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            getFromContacts();
        }
        //Toast.makeText(getApplicationContext(),String.valueOf(numbers.size()),Toast.LENGTH_SHORT).show();
        for(int i=0;i<numbers.size();i++) {
            read(numbers.get(i));
        }

    }
    private void read(final String number)
    {
        databaseReference = firebaseDatabase.getReference().child("users").child(number);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if(user!=null)
                    {
                        adapter.add(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    public void getFromContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor names = getContentResolver().query(uri, projection, null, null, null);

        int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        names.moveToFirst();
        do {
            String name = names.getString(indexName);
            String number = names.getString(indexNumber);
            numbers.add(number);
        } while (names.moveToNext());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Toast.makeText(this, "great", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
