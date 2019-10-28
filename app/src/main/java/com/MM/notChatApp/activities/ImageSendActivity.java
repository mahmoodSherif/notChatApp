package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.classes.Message;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ImageSendActivity extends AppCompatActivity {

    private StorageReference photosStorageReference;
    String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    String friendPhone = null;
    private Uri selectedImageUri;
    private FloatingActionButton sendButton;
    private EditText captionEditTxt;
    private ImageView imageView;
    private DatabaseReference chatRef;
    private DatabaseReference curChatRef;
    private ProgressBar progressBar;
    private String curChatId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_send);

        sendButton = findViewById(R.id.sendImage);
        captionEditTxt = findViewById(R.id.captionImage);
        imageView = findViewById(R.id.imageMessage);
        progressBar = findViewById(R.id.ProBar);
        progressBar.setVisibility(View.GONE);
        photosStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");
        chatRef = FirebaseDatabase.getInstance().getReference().child("chatPhotos");

        Intent intent = getIntent();
        selectedImageUri = Uri.parse(intent.getStringExtra("uriPhoto"));
        friendPhone = intent.getStringExtra("chatId");
        curChatId = intent.getStringExtra("curChatId");

        curChatRef = FirebaseDatabase.getInstance().getReference().child("chats").child(curChatId);

        Glide.with(ImageSendActivity.this)
                .load(selectedImageUri)
                .into(imageView);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(selectedImageUri.getLastPathSegment()!=null&&userPhone!=null&&friendPhone!=null) {
                   try {
                       send();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
                else {
                    Toast.makeText(getApplicationContext(),userPhone+" "+friendPhone,Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private void send() throws IOException {

        final StorageReference photoRef = photosStorageReference
                .child(selectedImageUri.getLastPathSegment());

        chatRef.child(userPhone).child(friendPhone).push().setValue(selectedImageUri.toString());
        chatRef.child(friendPhone).child(userPhone).push().setValue(selectedImageUri.toString());

        Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();
        //uploading the image
        UploadTask task = photoRef.putBytes(data);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageSendActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ImageSendActivity.this,"done",Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int currentProgress = (int) progress;
                progressBar.setProgress(currentProgress);

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
           progressBar.setVisibility(View.GONE);
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
                    message.setText("📷 Photo");
                    ChatActivity.notify(message, friendPhone);
                    message.setText("");
                    Map<String,Object> rr = message.toMap(new ArrayList<String>(Arrays.asList(userPhone , friendPhone)));
                    curChatRef.push().updateChildren(rr);
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
