package com.MM.notChatApp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.MessageAdapter;
import com.MM.notChatApp.classes.Message;
import com.MM.notChatApp.classes.User;
import com.MM.notChatApp.dialogs.FloatingView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER =  2;
    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_CODE_GALLERY =999;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    //camera permission
    String CameraPermission[];
    // activity views
    private MessageAdapter messageAdapter;
    private ListView messagesListView;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private ImageButton mSendButton;
    private ChildEventListener messagesListener;
    boolean typing = false;
    private String text = "" ;
    // bar views
    private TextView BarfriendName;
    private CircleImageView BarFriendImage;
    private TextView friendStatus;
    private ImageButton backButton;

    //popup menu views
    private ImageView galaryImage;
    private ImageView cameraImage;
    private ImageView docImage;

    // firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference chatRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference docRef;
    // cur chat info
    String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    String friendPhone = null;
    Uri photo = null;
    String friendname = null;
    boolean readMessage = false;
    boolean Online = false;
    private Uri uri_image;
    private ProgressDialog progressDialog;

    // maps
    private HashMap<DatabaseReference, ValueEventListener> valueEventListenerHashMap = new HashMap<>();
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerHashMap = new HashMap<>();

    //action mode Represents a contextual mode of the user interface
    private ActionMode mActionMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        readMessage = true;
        // intent
        if(getIntent().getExtras()!=null) {
            friendname = getIntent().getExtras().getString("username");
            photo = Uri.parse(getIntent().getExtras().getString("userPhoto"));
            friendPhone = getIntent().getExtras().getString("phone");
        }

        chatRef = FirebaseDatabase.getInstance().getReference().child("chats");
        firebaseStorage = FirebaseStorage.getInstance();
        docRef = firebaseStorage.getReference().child("chat_docs");
        //camera permission
        CameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //custom Bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_bar);
        final View view = getSupportActionBar().getCustomView();

        // bar views
        BarfriendName = view.findViewById(R.id.BarfriendName);
        BarFriendImage = view.findViewById(R.id.BarFriendImage);
        friendStatus = view.findViewById(R.id.BarFriendLastSeen);
        backButton = view.findViewById(R.id.backButton);

        //popup menu views
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View menuView = getLayoutInflater().inflate(R.layout.attachments_view, null, false);
        galaryImage = menuView.findViewById(R.id.galaryImage);
        cameraImage = menuView.findViewById(R.id.cameraImage);
        docImage = menuView.findViewById(R.id.docImage);
        TextView textView = menuView.findViewById(R.id.tvGallery);





        messagesListView = findViewById(R.id.messagesList);
        mPhotoPickerButton =  findViewById(R.id.photoPickerButton);
        mMessageEditText =  findViewById(R.id.messageEditText);
        mSendButton =  findViewById(R.id.sendButton);

        final List<Message> messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this,R.layout.message_item_res,messages);
        messagesListView.setAdapter(messageAdapter);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mActionMode != null)
                {
                    // // add or remove selection for current list item
                    onListItemSelect(i);
                }
            }
        });

        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setActivated(true);
               messagesListView.setItemChecked(i,true);
                onListItemSelect(i);
                return true;
            }
        });

        // set friend info
        BarfriendName.setText(friendname);
        Glide.with(ChatActivity.this)
                .load(photo)
                .into(BarFriendImage);
        checkStatus();
        showTyping();
        setSeenIndecator(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });


        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                    mSendButton.setClickable(true);
                } else {
                    mSendButton.setEnabled(false);
                    mSendButton.setClickable(false);
                }*/

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable.toString())&&editable.toString().trim().length()==1) {
                    typing = true;
                    setTypingIndecator(true);
                } else if(editable.toString().trim().length()==0 && typing){
                    typing = false;
                    setTypingIndecator(false);
                }
                mSendButton.setClickable(true);
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
    }

    private void openDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the custom popup layout
        final View inflatedView;
        inflatedView = layoutInflater.inflate(R.layout.attachments_view, null, false);
        LinearLayout layoutGallery;
        FloatingView.onShowPopup(this, inflatedView);
    }


    private void onListItemSelect(int position)
    {
        messageAdapter.toggleSelection(position);
        boolean hasCheckedItems = messageAdapter.getSelectedCount()>0;
        if(hasCheckedItems && mActionMode == null)
        {
            final Message message = messageAdapter.getItem(position);
            // there are some selected items, start the actionMode
            mActionMode = startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    // inflate contextual menu
                    actionMode.getMenuInflater().inflate(R.menu.chat_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    switch (menuItem.getItemId())
                    {
                        case R.id.copyMenu:
                            for (int i = 0; i<messageAdapter.getSelectedIds().size();i++)
                            {
                             Message message1 = messageAdapter.getItem(messageAdapter.getSelectedIds()
                             .keyAt(i));
                             text+=message1.getText()+" ";
                            }
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("message text", text);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getApplicationContext(),"Copied",Toast.LENGTH_SHORT).show();
                            actionMode.finish();
                            text = "";
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    // remove selection
                    messageAdapter.removeSelection();
                    mActionMode = null;
                }
            });
        }
        else if(!hasCheckedItems&&mActionMode!=null){
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }
        if (mActionMode!=null)
        {
            mActionMode.setTitle(String.valueOf(messageAdapter.getSelectedCount()));
        }
    }


    private void setTypingIndecator(final Boolean typing) {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone)
                .child(FirebaseAuth.getInstance()
                                .getCurrentUser().getPhoneNumber())
                .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                        {
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).child("typing").setValue(typing);
                        }
                        else
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("typing",typing);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).updateChildren(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }
    private void setSeenIndecator(final Boolean seen)
    {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone)
                .child(FirebaseAuth.getInstance()
                        .getCurrentUser().getPhoneNumber()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null)
                        {
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).child("seen").setValue(seen);
                        }
                        else
                        {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("seen",seen);
                            FirebaseDatabase.getInstance().getReference().child("chatList").child(friendPhone).child(
                                    FirebaseAuth.getInstance()
                                            .getCurrentUser().getPhoneNumber()).updateChildren(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    private void ensureChatId(){
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
                        getChatMessages(CurChatId);
                        setOnClickListenerForSendButton(CurChatId);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private Task<HttpsCallableResult> notify(Message message , String to) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("from", message.getSentby());
        data.put("text",message.getText());
        data.put("to", to);

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("sendNotification")
                .call(data);
    }


    private void setOnClickListenerForSendButton(final String CurChatId){
        if (mMessageEditText.getText().toString().trim().equals(""))
            return;
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String text = mMessageEditText.getText().toString();
                final SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
                FirebaseDatabase.getInstance().getReference().child("chatList")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                        .child(friendPhone).child("seen").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null)
                                {
                                    if(dataSnapshot.getValue().equals(true))
                                    {

                                        Message message = new Message(text,
                                                Time.format(new Date())  ,null , 3,userPhone);

                                        ChatActivity.this.notify(message ,friendPhone );
                                        FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                                                .push().setValue(message);
                                    }
                                    else
                                    {
                                        Message message = new Message(text,
                                                Time.format(new Date())  ,null , 2,userPhone);

                                        ChatActivity.this.notify(message ,friendPhone );
                                        FirebaseDatabase.getInstance().getReference().child("chats").child(CurChatId)
                                                .push().setValue(message);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        }
                );
                mMessageEditText.setText("");
            }
        });
    }
    private void getChatMessages(String id) {
        ChildEventListener chatListener = new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageAdapter.add(message);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        DatabaseReference ref = chatRef.child(id);
        ref.addChildEventListener(chatListener);
        childEventListenerHashMap.put(ref, chatListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setSeenIndecator(true);
        readMessage = true;
        ensureChatId();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageAdapter.clear();
        setSeenIndecator(false);
        readMessage = false;
        for (Map.Entry<DatabaseReference, ChildEventListener> entry : childEventListenerHashMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : valueEventListenerHashMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
    private void checkStatus() {
        FirebaseDatabase.getInstance().getReference().child("users").child(friendPhone).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user.getUserStatues().equals("online"))
                        {
                            friendStatus.setVisibility(View.VISIBLE);
                            Online = true;
                        }
                        else
                        {
                            friendStatus.setVisibility(View.GONE);
                            Online = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }
    private void showTyping()
    {
        FirebaseDatabase.getInstance().getReference().child("chatList").child(
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
        ).child(friendPhone).child("typing").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            if (dataSnapshot.getValue().equals(true)) {
                                Toast.makeText(getApplicationContext(),"hola",Toast.LENGTH_SHORT).show();
                                friendStatus.setVisibility(View.VISIBLE);
                                friendStatus.setText(R.string.typing);
                            }
                            else
                            {
                                if(Online) {
                                    friendStatus.setText("Online");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void imageclick(View view) {
        if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_GALLERY);
        }
        else
        {
            Intent intent=new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,REQUEST_CODE_GALLERY);
        }
        /*ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY);
       /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==READ_REQUEST_CODE)
        {
            if(grantResults.length>0 &&grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE_GALLERY);
                Toast.makeText(getApplicationContext(),"You  have permission to acscess file location!",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"You don't have permission to acscess file location!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_REQUEST_CODE)
        {
            if(grantResults.length > 0)
            {
                boolean cameraAccepted = grantResults[0]==
                        PackageManager.PERMISSION_GRANTED;
                boolean writeStorageAccepted = grantResults[0]==
                        PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted&&writeStorageAccepted)
                {
                    pickCamera();
                }
                else
                {
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK)
        {
            Uri selectedImageUri = data.getData();
            Intent intent = new Intent(ChatActivity.this,ImageSendActivity.class);
            intent.putExtra("uriPhoto",selectedImageUri.toString());
            intent.putExtra("friendPhone",friendPhone);
            startActivity(intent);
        }
        if(requestCode == READ_REQUEST_CODE&&resultCode==RESULT_OK)
        {
            Uri uri = null;
            if(data!=null)
            {
                uri = data.getData();
                if(uri!=null) {
                    send(uri);
                }
            }
            else {
                Toast.makeText(getApplicationContext(),"Please , select a file ",Toast.LENGTH_LONG).show();
            }
        }
        if(resultCode==RESULT_OK && requestCode==IMAGE_PICK_CAMERA_CODE)
        {
            Toast.makeText(getApplicationContext(),"hi",Toast.LENGTH_LONG).show();
            Uri image = uri_image;
            Intent intent = new Intent(ChatActivity.this,ImageSendActivity.class);
            intent.putExtra("uriPhoto",image.toString());
            intent.putExtra("friendPhone",friendPhone);
            startActivity(intent);

        }
    }
    private void send(Uri selectedDocUri)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("loading...");
        progressDialog.setProgress(0);
        final ProgressBar progressBar = new ProgressBar(ChatActivity.this,
                null, android.R.attr.progressBarStyleSmall);
        progressBar.setVisibility(View.VISIBLE);

        final StorageReference photoRef = docRef.child(userPhone).child(friendPhone)
                .child(selectedDocUri.getLastPathSegment());
        docRef.child(friendPhone).child(userPhone)
                .child(selectedDocUri.getLastPathSegment()).putFile(selectedDocUri);
        UploadTask task = photoRef.putFile(selectedDocUri);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ChatActivity.this,"done",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentp = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentp);
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
                    final Uri downloadedUri = task.getResult();

                    final SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
                    final Message  message = new Message(downloadedUri.toString()
                            , Time.format(new Date()) , "IsDOC", 0, userPhone);
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
                                    //Opening the upload file in browser using the upload url
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(downloadedUri);
                                    startActivity(intent);
                                    finish();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void documentClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/pdf");
        //To fetch files
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, READ_REQUEST_CODE);

    }

    public void CameraClick(View view) {
       /* Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }*/
        //intent to take camera , save to storage to get high quality
        pickCamera();
    }
    public void pickCamera()
    {
        if(checkCameraPermission()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Pic"); // title of pic
            values.put(MediaStore.Images.Media.DESCRIPTION, "image to text"); //description
            uri_image = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }
        else {
            ActivityCompat.requestPermissions(this, CameraPermission, CAMERA_REQUEST_CODE);
        }
    }
    private boolean checkCameraPermission() {
        //storage permission to get high quality image we have to save image to ex storage first
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
}
