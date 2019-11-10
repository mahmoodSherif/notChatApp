package com.MM.notChatApp.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class setUserNameForFirstTime extends AppCompatActivity {

    // user info
    CircleImageView userPhoto ;
    EditText usernameTx,bioTX;
    Button done;

    StorageReference usersRef;

    final int RC_PHOTO_PICKER = 505;
    final String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    Uri photo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_name_for_first_time);

        usersRef = FirebaseStorage.getInstance().getReference().child("profile Photos");

        photo = Uri.parse("android.resource://com.MM.notChatApp/" + R.drawable.user_empty_photo);
        userPhoto = findViewById(R.id.fUserphoto);
        usernameTx = findViewById(R.id.fusername);
        bioTX = findViewById(R.id.fbio);
        done = findViewById(R.id.fdone);
        Glide.with(setUserNameForFirstTime.this)
                .load(photo)
                .into(userPhoto);

        usernameTx.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    done.setEnabled(true);
                } else {
                    done.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY , true);
                startActivityForResult(Intent.createChooser(intent ,  "Complete action using") , RC_PHOTO_PICKER);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = usernameTx.getText().toString();
                final String bio = bioTX.getText().toString();
                final Uri cphoto = photo;

                // upload photo to storage
                final StorageReference curPhoto = usersRef.child(phone);
                UploadTask uploadTask = curPhoto.putFile(photo);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(setUserNameForFirstTime.this , "upload done",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(setUserNameForFirstTime.this , "upload fail",Toast.LENGTH_SHORT).show();
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
                            new com.MM.notChatApp.classes.User(username,uri.toString(),phone,bio,null).addTODatabae()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(username)
                                                    .build();
                                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileChangeRequest)
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
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            photo = data.getData();
            Glide.with(setUserNameForFirstTime.this)
                    .load(photo)
                    .into(userPhoto);
        }
    }
}
