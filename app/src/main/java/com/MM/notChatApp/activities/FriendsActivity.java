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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.pass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    // keys
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
    //counter for process bar
    int counter = 0;

    //GUI elements
    ListView FriendsList;
    ImageView checkImage;

    //listView
    friendsAdapter adapter;
    ArrayList<String> numbers = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();


    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference curUserRef;
    // Request code for READ_CONTACTS

    ProgressBar progressBar;
    Map<String, Boolean>map  = new HashMap<String, Boolean>();

    String userPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        adapter = new friendsAdapter(this, R.layout.friends_list_item, users);
        // UI
        progressBar = findViewById(R.id.FriendsListProgressBar);
        FriendsList = findViewById(R.id.FriendsList);
        checkImage = findViewById(R.id.check);
        userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        // Database setUp
        firebaseDatabase = FirebaseDatabase.getInstance();
        curUserRef = firebaseDatabase.getReference().child("users").child(userPhone);
        // ask for
        if(ActivityCompat.checkSelfPermission(FriendsActivity.this,Manifest.permission.READ_CONTACTS)!=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(FriendsActivity.this, new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else {
            setFriendsList();
        }
    }
    public FriendsActivity()
    {

    }

    public void setFriendsList()
    {
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

           // adapter = new friendsAdapter(this, R.layout.friends_list_item, users);
            FriendsList.setAdapter(adapter);

            getFromContacts();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FriendsActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                    setFriendsList();
                } else {
                    Toast.makeText(getApplicationContext(), "You don't have permission to acscess file location!",
                            Toast.LENGTH_SHORT).show();
                }
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

    public void read() {
        firebaseDatabase.getReference().child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();
                        int counter = 0;
                       for(DataSnapshot snapshot : dataSnapshot.getChildren())
                       {
                           User user = snapshot.getValue(User.class);
                           if(map.get(user.getPhone())!=null) {
                               if (map.get(user.getPhone())) {
                                       adapter.add(user);
                                   progressBar.setVisibility(View.VISIBLE);
                               }
                           }
                           else {
                               Log.v("see",user.getPhone()+" "+String.valueOf(map.get(user.getPhone())));
                            //   Toast.makeText(getApplicationContext(),user.getPhone()+" "+String.valueOf(map.get(user.getPhone())),Toast.LENGTH_LONG).show();
                           }
                           counter++;
                           if(counter == count)
                           {
                               progressBar.setVisibility(View.GONE);
                           }
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void getFromContacts() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor names = getContentResolver().query(uri, projection, null, null, null);

        int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        names.moveToFirst();
        do {
            String name = names.getString(indexName);
            String number = names.getString(indexNumber);
            //numbers.add(number);
            String num = number.replaceAll(" ","");
            StringBuilder newString = new StringBuilder();
            if(num.charAt(0)!='+'&&num.charAt(1)!='2') {
              newString.append("+2");
              newString.append(num);
              map.put(newString.toString(),true);
            }
            else {
                map.put(num, true);
            }
        } while (names.moveToNext());
        for (String number : map.keySet()) {
            if (checkIfNumVal(number)) {
                Log.v("NEWEE", "new one ");
                //   String num = numbers.get(i).replaceAll(" ","");
                // read(num);
            } else {
                map.put(number, false);
            }
            counter++;
        }
        getBlocked();
        read();

    }
    private void getBlocked(){
        curUserRef.child("blocked").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user : dataSnapshot.getChildren()){
                    map.put(user.getKey() , false);
                   // read();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.newGroup) {
            pass.list.clear();
            pass.list.addAll(users);
            Intent intent = new Intent(FriendsActivity.this,NewGroupActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
