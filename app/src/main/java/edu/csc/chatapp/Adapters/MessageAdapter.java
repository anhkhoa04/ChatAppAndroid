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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.csc.chatapp.MessageActivity;
import edu.csc.chatapp.Model.Chat;
import edu.csc.chatapp.R;
import edu.csc.chatapp.Model.User;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<Chat> mChat;
    private String imageURL;
    FirebaseUser firebaseUser;


    public MessageAdapter(Context context, ArrayList<Chat> list_chat, String imageURL){
        this.context = context;
        this.mChat = list_chat;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View userView = layoutInflater.inflate(R.layout.chat_item_right, parent, false);
            ViewHolder viewHolder = new ViewHolder(userView);
            return viewHolder;
        }
        else{
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View userView = layoutInflater.inflate(R.layout.chat_item_left, parent, false);
            ViewHolder viewHolder = new ViewHolder(userView);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.showMessage.setText(chat.getMessage());
        if(imageURL.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Glide.with(context).load(imageURL).into(holder.profile_image);
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference("image_user");
//            try {
//                final File file = File.createTempFile("temp_image",imageURL);
//                String []extension = imageURL.split("-");
//                storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"."+extension[1]).getFile(file)
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

        //Log.w("pos", holder.getLayoutPosition()+"-"+mChat.get(holder.getLayoutPosition()).getMessage());
        if(mChat.size()-1 == position ){
            if(chat.getSeen().equals("true")){
                //Log.w("pos size", mChat.size()+"-seen");
                holder.txt_seen.setText("Seen");
            }
            else{
                //Log.w("pos size", mChat.size()+"-delivered");
                holder.txt_seen.setText("Delivered");
            }
        }
        else{
            holder.txt_seen.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView showMessage;
        public ImageView profile_image;
        public TextView txt_seen;


        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_msg);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return  MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }
}
