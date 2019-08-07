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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendsActivity extends AppCompatActivity {

    //counter for process bar
    int counter = 0;
    ListView FriendsList;
    friendsAdapter adapter;
    ArrayList<String>numbers ;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    // Request code for READ_CONTACTS
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        FriendsList = findViewById(R.id.FriendsList);
        FriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               User selectedUser = (User) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(FriendsActivity.this,ChatActivity.class);
              //  intent.putExtra("userFromIntent", (Parcelable) selectedUser);
                intent.putExtra("username",selectedUser.getUserName());
                intent.putExtra("phone",selectedUser.getPhone());
                intent.putExtra("userPhoto",selectedUser.getUserPhotoUrl());
                startActivity(intent);
            }
        });
        ArrayList<User> users = new ArrayList<>();
        numbers = new ArrayList<>();
        adapter = new friendsAdapter(this,R.layout.friends_list_item,users);
        FriendsList.setAdapter(adapter);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
         //   requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            getFromContacts();
       // }
        //Toast.makeText(getApplicationContext(),String.valueOf(numbers.size()),Toast.LENGTH_SHORT).show();
        for(int i=0;i<numbers.size();i++) {
            read(numbers.get(i));
            counter++;
        }

    }
    private void read(String number)
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

                        //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();
                    if(counter == numbers.size()) {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                    else
                        progressBar.setVisibility(ProgressBar.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        if(counter==numbers.size())
        {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }

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
    private void status(String status)
    {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("UserStatues",status);
        FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
        ).updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.clear();
        status("offline");
    }
}
