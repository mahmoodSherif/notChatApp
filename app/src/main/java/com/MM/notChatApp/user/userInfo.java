package com.MM.notChatApp.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.bumptech.glide.Glide;
import com.MM.notChatApp.classes.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class userInfo extends AppCompatActivity {

    final int RC_PHOTO_PICKER = 505;
    // user info views
    CircleImageView userImage;
    EditText usernameTX, userBioTX, userPhoneTX;
    Button saveBtn;
    ProgressBar progressBar;

    //firebase
    FirebaseUser curFirebaseUser;
    final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    //user info
    Uri photo = null;
    String phone = null;
    boolean isCh= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        curFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        phone = curFirebaseUser.getPhoneNumber();

        userImage = findViewById(R.id.userPhoto);
        usernameTX = findViewById(R.id.username);
        userBioTX = findViewById(R.id.userBio);
        userPhoneTX = findViewById(R.id.userPhone);
        saveBtn = findViewById(R.id.saveBtn);
        progressBar = findViewById(R.id.probar);

        FirebaseDatabase.getInstance().getReference().child("users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User curUser = dataSnapshot.getValue(User.class);
                usernameTX.setText(curUser.getUserName());
                userPhoneTX.setText(curUser.getPhone());
                userBioTX.setText(curUser.getUserBio());
                firebaseStorage.getReference().child("profile Photos").child(phone).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        photo = uri;
                        Glide.with(userImage.getContext())
                                .load(photo)
                                .into(userImage);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // set info
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY , true);
                startActivityForResult(Intent.createChooser(intent ,  "Complete action using") , RC_PHOTO_PICKER);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(isCh){
                    final StorageReference curPhoto = FirebaseStorage.getInstance().getReference().child("profile Photos").child(phone);
                    UploadTask uploadTask = curPhoto.putFile(photo);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(userInfo.this , "upload done",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(userInfo.this , "upload fail",Toast.LENGTH_SHORT).show();
                        }
                    }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return curPhoto.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Uri uri = task.getResult();
                                new com.MM.notChatApp.classes.User(usernameTX.getText().toString(),uri.toString(),phone,userBioTX.getText().toString()).addTODatabae()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                finish();
                                            }
                                        });
                            }
                        }
                    });
                }else{
                    firebaseStorage.getReference().child("profile Photos").child(phone).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            new com.MM.notChatApp.classes.User(usernameTX.getText().toString(),uri.toString(),phone,userBioTX.getText().toString()).addTODatabae()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            finish();
                                        }
                                    });
                        }
                    });
                }
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            photo = data.getData();
            Glide.with(userImage.getContext())
                    .load(photo)
                    .into(userImage);
            isCh = true;
        }
    }
}
