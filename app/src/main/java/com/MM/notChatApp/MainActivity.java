package com.MM.notChatApp;

import android.content.Intent;
import android.os.Bundle;

import com.MM.notChatApp.adapters.MessagesListAdapter;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.user.setUserNameForFirstTime;
import com.MM.notChatApp.user.userInfo;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // firebase consts
    private static final int RC_SIGN_IN = 123;
   private ListView MainListView;
   //adapter
    MessagesListAdapter messagesListAdapter;
   // private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainListView = findViewById(R.id.MainListView);
       // mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        // Initialize progress bar
        //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        //setAdapter
        List<User> messages = new ArrayList<>();
        messagesListAdapter = new MessagesListAdapter(this,R.layout.main_listview_item,messages);
        MainListView.setAdapter(messagesListAdapter);
        //initialize
        MainListView = findViewById(R.id.MainListView);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                startActivity(intent);
                //
                // signOut();
            }
        });

        sureSignIn();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.addNewFriend)
        {
            Intent intent = new Intent(MainActivity.this,FriendsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                if(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null){
                    Intent intent = new Intent(MainActivity.this, setUserNameForFirstTime.class);
                    startActivity(intent);
                }
            }else{
                finish();
            }
        }
    }

    //firebase functions
    private void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
                );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    private void signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        signIn();
                    }
                });
    }
    private void sureSignIn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Toast.makeText(MainActivity.this , "HI "+ user.getDisplayName(),Toast.LENGTH_SHORT).show();
        } else {
            signIn();
        }
    }
}
