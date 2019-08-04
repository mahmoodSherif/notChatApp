package com.MM.notChatApp.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import de.hdodenhof.circleimageview.CircleImageView;

public class userInfo extends AppCompatActivity {

    // user info
    CircleImageView userImage;
    EditText username , userBio , userPhone;
    Button saveBtn;

    FirebaseUser curUser;
    final int RC_PHOTO_PICKER = 505;
    Uri photo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userImage = findViewById(R.id.userPhoto);
        username = findViewById(R.id.username);
        userBio = findViewById(R.id.userBio);
        userPhone = findViewById(R.id.userPhone);
        saveBtn = findViewById(R.id.saveBtn);

        curUser = FirebaseAuth.getInstance().getCurrentUser();
        photo = Uri.parse(curUser.getPhotoUrl().toString());

        Glide.with(userImage.getContext())
                .load(photo)
                .into(userImage);
        username.setText(curUser.getDisplayName());
        userPhone.setText(curUser.getPhoneNumber());

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
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username.getText().toString())
                        .setPhotoUri(photo)
                        .build();
                curUser.updateProfile(profileChangeRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(userInfo.this , "Fail" ,Toast.LENGTH_LONG).show();
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
            Glide.with(userImage.getContext())
                    .load(photo)
                    .into(userImage);
        }
    }
}
