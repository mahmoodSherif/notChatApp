package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    // keys
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
    final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    //counter for process bar
    int counter = 0;

    //GUI elements
    ListView FriendsList;
    ProgressBar progressBar;

    //listView
    friendsAdapter adapter;
    ArrayList<String> numbers = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();


    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // ask for user permission
        if (ContextCompat.checkSelfPermission(FriendsActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(FriendsActivity.this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(FriendsActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        }

        // UI
        progressBar = findViewById(R.id.FriendsListProgressBar);
        FriendsList = findViewById(R.id.FriendsList);

        // Database setUp
        firebaseDatabase = FirebaseDatabase.getInstance();

        FriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User selectedUser = (User) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(FriendsActivity.this, ChatActivity.class);
                //  intent.putExtra("userFromIntent", (Parcelable) selectedUser);
                intent.putExtra("username", selectedUser.getUserName());
                intent.putExtra("phone", selectedUser.getPhone());
                intent.putExtra("userPhoto", selectedUser.getUserPhotoUrl());
                startActivity(intent);
            }
        });

        adapter = new friendsAdapter(this, R.layout.friends_list_item, users);
        FriendsList.setAdapter(adapter);

        // get friends
        getFromContacts();
        for (int i = 0; i < numbers.size(); i++) {
            if (checkIfNumVal(numbers.get(i))) {
                Log.v("NEWEE", "new one ");
                String num = numbers.get(i).replaceAll(" ", "");
                read(num);
            }
            counter++;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.clear();
    }

    private boolean checkIfNumVal(String s) {
        for (int i = 0; i < s.length(); i++) {
            if ((s.charAt(i) != '+' && ('0' > s.charAt(i) || s.charAt(i) > '9') && s.charAt(i) != ' ')) {
                return false;
            }
        }
        return true;
    }

    private void read(final String number) {

        DatabaseReference curUserRef = firebaseDatabase.getReference().child("users").child(number);

        curUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                Boolean ok = false;
                if (user != null) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).getPhone().equals(number)) {
                            ok = true;
                        }
                    }
                    if (!ok) {
                        adapter.add(user);
                    }
                }

                //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();
                if (counter == numbers.size()) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                } else
                    progressBar.setVisibility(ProgressBar.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (counter == numbers.size()) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }


    }

    public void getFromContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ContentResolver cr = getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
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


}
