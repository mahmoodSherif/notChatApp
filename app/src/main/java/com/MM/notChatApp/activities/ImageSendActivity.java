package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ImageSendActivity extends AppCompatActivity {

    private StorageReference photosStorageReference;
    String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    String friendPhone = null;
    private Uri selectedImageUri;
    private Button sendButton;
    private EditText captionEditTxt;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_send);

        sendButton = findViewById(R.id.sendImage);
        captionEditTxt = findViewById(R.id.captionImage);
        imageView = findViewById(R.id.imageMessage);

        photosStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");

        Intent intent = getIntent();
         selectedImageUri = Uri.parse(intent.getStringExtra("uriPhoto"));
        friendPhone = intent.getStringExtra("friendPhone");

        Glide.with(ImageSendActivity.this)
                .load(selectedImageUri)
                .into(imageView);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(selectedImageUri.getLastPathSegment()!=null&&userPhone!=null&&friendPhone!=null)
                    send();
                else {
                    Toast.makeText(getApplicationContext(),userPhone+" "+friendPhone,Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private void send()
    {
        final StorageReference photoRef = photosStorageReference.child(userPhone).child(friendPhone)
                .child(selectedImageUri.getLastPathSegment());
        photosStorageReference.child(friendPhone).child(userPhone)
                .child(selectedImageUri.getLastPathSegment()).putFile(selectedImageUri);
        UploadTask task = photoRef.putFile(selectedImageUri);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageSendActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ImageSendActivity.this,"done",Toast.LENGTH_SHORT).show();
            }
        });
        Task<Uri> uriTask =task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful())
                {
                    throw task.getException();
                }
                return photoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful())
                {
                    Uri downloadedUri = task.getResult();

                    final SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
                    final Message  message = new Message(captionEditTxt.getText().toString().trim()
                            , Time.format(new Date()) , downloadedUri.toString(), 0, userPhone);

                    FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone).child("id")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String CurChatId = null;
                                    if(dataSnapshot.getValue() == null){
                                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").push();
                                        String chatId = chatRef.getKey();
                                        FirebaseDatabase.getInstance().getReference().child("chatList").child(userPhone).child(friendPhone)
                                                .child("id").setValue(chatId);
                                        FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(userPhone)
                                                .child("id").setValue(chatId);
                                        CurChatId = chatId;
                                    }else{
                                        CurChatId = dataSnapshot.getValue().toString();
                                    }
                                    FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                                            .push().setValue(message);
                                    finish();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_send_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.cropImage)
        {
            //go and crop it
            CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //get cropped image
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                selectedImageUri = result.getUri();
                Glide.with(ImageSendActivity.this)
                        .load(selectedImageUri)
                        .into(imageView);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
