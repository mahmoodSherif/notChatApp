package com.MM.notChatApp.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.MM.notChatApp.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class userInfo extends AppCompatActivity {

    // user info
    ImageView userImage;
    EditText username , userBio , userPhone;
    Button saveBtn;

    FirebaseUser curUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userImage = (ImageView)findViewById(R.id.userPhoto);
        username = (EditText)findViewById(R.id.username);
        userBio = (EditText)findViewById(R.id.userBio);
        userPhone = (EditText)findViewById(R.id.userPhone);
        saveBtn = (Button)findViewById(R.id.saveBtn);

        curUser = FirebaseAuth.getInstance().getCurrentUser();
        if(curUser != null){
            Glide.with(userImage.getContext())
                    .load(curUser.getPhotoUrl())
                    .into(userImage);
            username.setText(curUser.getDisplayName());
            userPhone.setText(curUser.getPhoneNumber());
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "https://www.gstatic.com/images/branding/googlelogo/2x/googlelogo_color_284x96dp.png";
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username.getText().toString())
                        .setPhotoUri(Uri.parse(s))
                        .build();
                curUser.updateProfile(profileChangeRequest);
            }
        });

    }
}
