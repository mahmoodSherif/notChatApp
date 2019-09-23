package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.MediaAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MediaActivity extends AppCompatActivity {

    MediaAdapter mediaAdapter;
    GridView ImagesGrid;
    String friendName;
    String userName;
    ArrayList<String> photosList;
    //firebase
    DatabaseReference photosRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ImagesGrid = findViewById(R.id.imagesGridView);
        photosList = new ArrayList<>();

        //get intent from chat activity
         Intent intent = getIntent();
        friendName = intent.getStringExtra("chatId");
        userName = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        //get Ref
        photosRef = FirebaseDatabase.getInstance().getReference().child("chatPhotos")
                .child(userName).child(friendName);
        getImagesFromFB();
        ImagesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent viewIntent = new Intent(MediaActivity.this,ImageViewer.class);
                viewIntent.putStringArrayListExtra("list",photosList);
                startActivity(viewIntent);
            }
        });
    }

    private void getImagesFromFB() {
        photosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren())
                {
                    photosList.add(data.getValue(String.class));
                }
                mediaAdapter = new MediaAdapter(getBaseContext(),photosList,ImagesGrid);
                ImagesGrid.setAdapter(mediaAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
