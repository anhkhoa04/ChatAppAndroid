package edu.csc.chatapp.Fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.csc.chatapp.MainActivity;
import edu.csc.chatapp.Model.User;
import edu.csc.chatapp.R;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    CircleImageView profile_image;
    TextView txt_username, user_email;

    FirebaseUser firebaseUser;
    DocumentReference documentReference;

    StorageReference storageReference;
    private  static  final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask storageTask;
    private int temp = 0;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        txt_username = view.findViewById(R.id.username);
        user_email = view.findViewById(R.id.user_email);

        storageReference = FirebaseStorage.getInstance().getReference("image_user");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        documentReference = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                User user = documentSnapshot.toObject(User.class);

                txt_username.setText(user.getName());
                if(isAdded()){
                    if(user.getImageURL().equals("default")){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                        user_email.setText(firebaseUser.getEmail());
                    }
                    else{
                        Glide.with(getContext()).load(user.getImageURL()).into(profile_image);
                        user_email.setText(firebaseUser.getEmail());
//                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("image_user");
//                        try {
//                            final File file = File.createTempFile("temp_image",user.getImageURL());
//                            String []extension = user.getImageURL().split("-");
//                            storageReference.child(user.getId()+"."+extension[1]).getFile(file)
//                                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
//                                            if(task.isSuccessful()){
//                                                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//                                                profile_image.setImageBitmap(bitmap);
//                                            }
//                                        }
//                                    });
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }

                    }
                }
            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp += 1;
                openImage();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference fileReference = storageReference.child(firebaseUser.getUid()+"."+getFileExtension(imageUri));

            storageTask = fileReference.putFile(imageUri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // upload image uri
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            String mUri = downloadUrl.toString();
                            documentReference = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid());
                            Map<String, Object> map = new HashMap<>();
                            map.put("imageURL", mUri);
                            documentReference.update(map);

                            progressDialog.dismiss();
                        }
                    });

                    // upload extension of image
//                    documentReference = FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid());
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("imageURL",  temp+"-"+getFileExtension(imageUri));
//                    documentReference.update(map);
//
//                    progressDialog.dismiss();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            if(storageTask != null && storageTask.isInProgress()){
                Toast.makeText(getContext(), "Uploading in progress", Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }

        }
    }
}
