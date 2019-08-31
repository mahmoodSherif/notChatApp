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
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 55;
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
    Map<String, Boolean>map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        map = new HashMap<String, Boolean>();
        // ask for
        if (ContextCompat.checkSelfPermission(FriendsActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(FriendsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(FriendsActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }


        progressBar = findViewById(R.id.FriendsListProgressBar);
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
        for(String number : map.keySet()) {
            if(checkIfNumVal(number)){
                Log.v("NEWEE" , "new one ");
             //   String num = numbers.get(i).replaceAll(" ","");
               // read(num);
            }
            else {
                map.put(number,false);
            }
            counter++;
        }
        read();

    }

    private boolean checkIfNumVal(String s) {
        for(int i = 0;i<s.length();i++){
            if ((s.charAt(i) != '+' && ('0' > s.charAt(i) || s.charAt(i) > '9') && s.charAt(i) != ' ')) {
                return false;
            }
        }
        return true;
    }

    private void read()
    {
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
        /*databaseReference = firebaseDatabase.getReference().child("users").child(number);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    boolean ok = false;
                    if(user!=null)
                    {
                        for(int i = 0 ;i<adapter.getCount();i++)
                        {
                            if(adapter.getItem(i).getPhone().equals(number))
                            {
                                ok = true;
                            }
                        }
                        if(!ok) {
                            adapter.add(user);
                        }
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

*/
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
            //numbers.add(number);
            String num = number.replaceAll(" ","");
            StringBuilder newString = new StringBuilder();
            if(num.charAt(0)!='+'&&num.charAt(1)!='2') {
              /* for (int i = 0; i < num.length(); i++) {
                    newString.append(num.charAt(i));
                    if (i == 0) {
                        newString.append("+2");
                    }
                }*/
              newString.append("+2");
              newString.append(num);
               // Toast.makeText(getApplicationContext(),newString,Toast.LENGTH_LONG).show();
              map.put(newString.toString(),true);
            }
            else {
                map.put(num, true);
            }
        } while (names.moveToNext());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
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

}
