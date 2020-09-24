package edu.csc.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
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
import java.util.ArrayList;
import java.util.Date;

import edu.csc.chatapp.MessageActivity;
import edu.csc.chatapp.Model.Chat;
import edu.csc.chatapp.R;
import edu.csc.chatapp.Model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private ArrayList<User> list_user;
    private boolean isChat;
    String theLastMessage, theSender, theChatTime;
    FirebaseUser firebaseUser;

    public UserAdapter(Context context, ArrayList<User> list_user, boolean isChat){
        this.context = context;
        this.list_user = list_user;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View userView = layoutInflater.inflate(R.layout.user_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(userView);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = list_user.get(position);
        holder.username.setText(user.getName());
        if(user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Glide.with(context).load(user.getImageURL()).into(holder.profile_image);
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference("image_user");
//            try {
//                final File file = File.createTempFile("temp_image",user.getImageURL());
//                String []extension = user.getImageURL().split("-");
//                storageReference.child(user.getId()+"."+extension[1]).getFile(file)
//                        .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
//                                if(task.isSuccessful()){
//                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//                                    holder.profile_image.setImageBitmap(bitmap);
//                                }
//                            }
//                        });
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
        }

        if(isChat){
            getLastMessage(user.getId(), holder.last_msg, holder.chat_time);
        }
        else{
            holder.last_msg.setVisibility(View.GONE);
            holder.chat_time.setVisibility(View.GONE);
        }

        if(isChat){
            if(user.getStatus().equals("online")){
                holder.image_on.setVisibility(View.VISIBLE);
                holder.image_off.setVisibility(View.GONE);
            }
            else{
                holder.image_on.setVisibility(View.GONE);
                holder.image_off.setVisibility(View.VISIBLE);
            }
        }
        else{
            holder.image_on.setVisibility(View.GONE);
            holder.image_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("user_id", user.getId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list_user.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        public ImageView image_on;
        public ImageView image_off;
        public TextView last_msg;
        public TextView chat_time;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            image_on = itemView.findViewById(R.id.image_online);
            image_off = itemView.findViewById(R.id.image_offline);
            last_msg = itemView.findViewById(R.id.last_message);
            chat_time = itemView.findViewById(R.id.chat_time);
        }
    }

    private void getLastMessage(final String friend_id, final TextView lastMessage, final TextView chatTime){
        theLastMessage = "default";

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        final String []time = dateFormat.format(date).split(" ");

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Chats");
        collectionReference.orderBy("time").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                    Chat chat = documentSnapshot.toObject(Chat.class);
                    if(chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(friend_id) ||
                            chat.getSender().equals(friend_id) && chat.getReceiver().equals(firebaseUser.getUid())){
                        theLastMessage = chat.getMessage();
                        theSender = chat.getSender();
                        String []timeChat= chat.getTime().split(" ");
                        if(time[0].equals(timeChat[0])){
                            theChatTime = timeChat[1];
                        }
                        else{
                           theChatTime = timeChat[0];
                        }
                    }
                }

                chatTime.setText(theChatTime);

                if (theLastMessage.equals("default")){
                    lastMessage.setText("No message");
                }
                else{
                    if(theSender.equals(firebaseUser.getUid())) {
                        lastMessage.setText("You: "+theLastMessage);
                    }
                    else{
                        lastMessage.setText(theLastMessage);
                    }
                }
            }
        });
    }
}
