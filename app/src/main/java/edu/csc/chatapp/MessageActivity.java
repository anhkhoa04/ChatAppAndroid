package edu.csc.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.csc.chatapp.Adapters.MessageAdapter;
import edu.csc.chatapp.Model.Chat;
import edu.csc.chatapp.Model.User;

public class MessageActivity extends AppCompatActivity {
    CircleImageView profile_image;
    TextView txt_username;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;

    FirebaseUser firebaseUser;
    ImageButton btn_send;
    EditText edt_send;

    MessageAdapter messageAdapter;
    ArrayList<Chat> list_chat;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    ArrayList<String> arrayList_key;

    ImageView image_on, image_off;
    String user_id;

    static boolean active = false;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        active = true;

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //finish();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("image_user");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        txt_username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        edt_send = findViewById(R.id.text_send);
        image_on = findViewById(R.id.image_online);
        image_off = findViewById(R.id.image_offline);

        list_chat = new ArrayList<>();
        arrayList_key = new ArrayList<>();

        user_id = getIntent().getStringExtra("user_id");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                String msg = edt_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(dateFormat.format(date),firebaseUser.getUid(), user_id, msg);

                }
                else{
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                edt_send.setText("");
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(user_id);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                User user = documentSnapshot.toObject(User.class);
                txt_username.setText(user.getName());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                   // getImage(user);
                }
                if(user.getStatus().equals("online")){
                    image_on.setVisibility(View.VISIBLE);
                    image_off.setVisibility(View.GONE);
                }
                else{
                    image_on.setVisibility(View.GONE);
                    image_off.setVisibility(View.VISIBLE);
                }

                messageAdapter = new MessageAdapter(getApplicationContext(), list_chat, user.getImageURL());
                recyclerView.setAdapter(messageAdapter);
                readMessage(firebaseUser.getUid(), user_id);
                recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
            }
        });

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ( bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(messageAdapter.getItemCount()-1); // can use smoothScrollToPosition
                            //recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });

        seenMessage(user_id);
    }

//    public void getImage(User user){
//        try {
//            final File file = File.createTempFile("temp_image",user.getImageURL());
//            String []extension = user.getImageURL().split("-");
//            storageReference.child(user.getId()+"."+extension[1]).getFile(file)
//                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
//                            if(task.isSuccessful()){
//                                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//                                profile_image.setImageBitmap(bitmap);
//                            }
//                        }
//                    });
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    public void seenMessage(final String friend_id){
        collectionReference = firebaseFirestore.collection("Chats");
        collectionReference.orderBy("time").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    Chat chat = documentSnapshot.toObject(Chat.class);
                    if(chat.getSender().equals(friend_id) && chat.getReceiver().equals(firebaseUser.getUid())){
                        Map<String, Object> map = new HashMap<>();
                        map.put("seen", "true");
                        collectionReference.document(documentSnapshot.getId()).update(map);
                    }
                }
                recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
            }
        });

    }

    private void sendMessage(String time,String sender, String receiver, String message){
        Map<String, Object> msg = new HashMap<>();
        msg.put("time", time);
        msg.put("sender",sender);
        msg.put("receiver",receiver);
        msg.put("message",message);
        msg.put("seen","false");

        firebaseFirestore.collection("Chats").document().set(msg);
    }

    private void readMessage(final String my_id, final String friend_id){
        collectionReference = firebaseFirestore.collection("Chats");
        collectionReference.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();

                    switch (documentChange.getType()){
                        case ADDED:
                            Chat chat = documentSnapshot.toObject(Chat.class);
                            if(chat.getSender().equals(my_id) && chat.getReceiver().equals(friend_id) ||
                                    chat.getSender().equals(friend_id) && chat.getReceiver().equals(my_id)){
                                if(!arrayList_key.contains(documentSnapshot.getId())){
                                    arrayList_key.add(documentSnapshot.getId());
                                    list_chat.add(chat);
                                }
                            }
                            if(active){
                                seenMessage(friend_id);
                            }
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messageAdapter.getItemCount()-1); // can use smoothScrollToPosition
                            //recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                            break;
                        case REMOVED:
                            list_chat.clear();
                            messageAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            if(arrayList_key.indexOf(documentSnapshot.getId()) >= 0){
                                list_chat.set(arrayList_key.indexOf(documentSnapshot.getId()), documentSnapshot.toObject(Chat.class));
                            }
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
                            break;
                    }
                }
            }
        });

    }

    private void status(String status){
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseUser.getUid());
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        documentReference.update(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        seenMessage(user_id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        active = false;
    }
}
