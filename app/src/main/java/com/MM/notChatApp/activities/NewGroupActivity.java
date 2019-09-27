package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.MM.notChatApp.DBvars;
import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.friendsAdapter;
import com.MM.notChatApp.classes.Group;
import com.MM.notChatApp.pass;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_GALLERY = 999;

    ListView newGroupList;
    Set<Integer> selectedUsers;
    ArrayList<String> users = new ArrayList<>();
    FloatingActionButton checkFab;
    String groupName;
    Uri groupPhoto;
    String userPhone;

    DatabaseReference chatsRef , chatListRef , usersRef;
    StorageReference  photosStorageReference;

    //dialog
     EditText groupNameET;
    CircleImageView groupImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        initUI();

        chatListRef = FirebaseDatabase.getInstance().getReference().child("chatList");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        photosStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");
        userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

    }

    private void initUI(){
        newGroupList = findViewById(R.id.newGroupList);
        selectedUsers = new HashSet<>();
        checkFab = findViewById(R.id.checkFab);
        Log.e("list", String.valueOf(pass.list.size()));
        friendsAdapter friendsAdapter = new friendsAdapter(this,R.layout.friends_list_item, pass.list);
        newGroupList.setAdapter(friendsAdapter);

        checkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedUsers.size()>0) {
                    createDialog();
                }
                else {
                    Toast.makeText(getApplicationContext(),"select members",Toast.LENGTH_LONG).show();
                }

             /*   /// start test
                for(int pos : selectedUsers){
                    users.add(pass.list.get(pos).getPhone());
                }
                if(!users.contains(userPhone))
                    users.add(userPhone);

                addNewGroup("new group baby", "uri" , users);
                /// end test
*/
                Log.v("fab pressed" , "true");
            }
        });

        newGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selectedUsers.contains(i)){
                    selectedUsers.remove(i);
                    view.findViewById(R.id.check).setVisibility(View.GONE);
                }else{
                    selectedUsers.add(i);
                    view.findViewById(R.id.check).setVisibility(View.VISIBLE);
                }

            }
        });
    }/*
    private void createGroup(){
        users.add(userPhone);
        final Group group = new Group(null,"name test","photo test",users);
        DatabaseReference groupRef = chatsRef.push();
        final String id = groupRef.getKey();
        for(String cur : users){
            chatListRef.child(cur).child(id).setValue(group);
        }
    }*/
    private void createDialog()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_group_layout, null);
        dialogBuilder.setView(dialogView);
         groupNameET =  dialogView.findViewById(R.id.groupNameET);
         groupImage = dialogView.findViewById(R.id.addGroupPhoto);
        dialogBuilder.setMessage("Enter group info");
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(NewGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_GALLERY);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_GALLERY);
                }
            }
        });
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
               if(groupNameET.getText().toString().length()==0)
               {
                   Toast.makeText(getApplicationContext(),"set group name",Toast.LENGTH_LONG).show();
               }
               else {
                   groupName = groupNameET.getText().toString().trim();
                   for(int pos : selectedUsers){
                       users.add(pass.list.get(pos).getPhone());
                   }
                   saveImageToFb();
                   Intent intent = new Intent(NewGroupActivity.this,ChatActivity.class);
                   startActivity(intent);
               }
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void saveImageToFb() {
        final StorageReference photoRef = photosStorageReference
                .child(groupPhoto.getLastPathSegment());
        UploadTask task = photoRef.putFile(groupPhoto);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewGroupActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(NewGroupActivity.this,"done",Toast.LENGTH_SHORT).show();
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
                    addNewGroup(groupName,downloadedUri.toString(),users);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            groupPhoto = selectedImageUri;
            Glide.with(groupImage.getContext())
                    .load(selectedImageUri)
                    .into(groupImage);
        }
    }
    private void addNewGroup(String groupName, String uri, ArrayList<String> members){
        String DBGroupid = pass.groupRef.push().getKey();
        String DBGroupChatid = pass.chatRef.push().getKey();

        Group newGroup = new Group(DBGroupid , groupName , uri , members);
        pass.groupRef.child(DBGroupid).setValue(newGroup);

        members.add(userPhone);
        for(String cur : members){
            pass.chatListRef.child(cur).child(DBGroupid).child(DBvars.GROUP.isGroup).setValue(true);
            pass.chatListRef.child(cur).child(DBGroupid).child(DBvars.GROUP.id).setValue(DBGroupChatid);
        }
        Log.v("Group" , "new group has been added");
    }
}
