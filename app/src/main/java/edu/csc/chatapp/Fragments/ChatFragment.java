package edu.csc.chatapp.Fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.csc.chatapp.Adapters.UserAdapter;
import edu.csc.chatapp.Model.Chat;
import edu.csc.chatapp.Model.User;
import edu.csc.chatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ArrayList<String> userArrayList;
    private ArrayList<User> mUser;

    private String userId;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference, collectionReference2;

    private ArrayList<String> arrayList_key, arrayList_key2;

    public ChatFragment() {
        // Required empty public constructor
    }

    public ChatFragment(String user_id) {
        this.userId = user_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Chats");

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userArrayList = new ArrayList<>();
        arrayList_key2 = new ArrayList<>();

        mUser = new ArrayList<>();
        arrayList_key = new ArrayList<>();

        userAdapter = new UserAdapter(getContext(),mUser, true);
        recyclerView.setAdapter(userAdapter);

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();

                    switch (documentChange.getType()){
                        case ADDED:
                            Chat chat = documentSnapshot.toObject(Chat.class);
                            if(chat.getSender().equals(userId)){
                                if(!userArrayList.contains(chat.getReceiver())) {
                                    arrayList_key2.add(documentSnapshot.getId());
                                    userArrayList.add(chat.getReceiver());
                                }
                            }
                            else if(chat.getReceiver().equals(userId)){
                                if(!userArrayList.contains(chat.getSender())) {
                                    arrayList_key2.add(documentSnapshot.getId());
                                    userArrayList.add(chat.getSender());
                                }
                            }

                            break;
                        case REMOVED:
                            userArrayList.clear();
                            break;
                        case MODIFIED:
                            if(arrayList_key2.indexOf(documentSnapshot.getId()) >= 0){
                                Chat chat1 = documentSnapshot.toObject(Chat.class);
                                if(chat1.getSender().equals(userId)){
                                    if(!userArrayList.contains(chat1.getReceiver())) {
                                        userArrayList.set(arrayList_key2.indexOf(documentSnapshot.getId()), chat1.getReceiver());
                                    }
                                }
                                else if(chat1.getReceiver().equals(userId)){
                                    if(!userArrayList.contains(chat1.getSender())) {
                                        userArrayList.set(arrayList_key2.indexOf(documentSnapshot.getId()), chat1.getSender());
                                    }
                                }
                            }
                            break;
                    }
                }
                //Log.w("test", userArrayList.size()+"");
                readChatsUser();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void readChatsUser() {
        collectionReference2 = firebaseFirestore.collection("Users");
        collectionReference2.orderBy("name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    switch (documentChange.getType()){
                        case ADDED:
                            User user = documentSnapshot.toObject(User.class);
                            if(userArrayList.contains(user.getId())){
                                if(!arrayList_key.contains(user.getId())){
                                    arrayList_key.add(user.getId());
                                    mUser.add(user);
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                            break;
                        case REMOVED:
                            mUser.clear();
                            userAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            if(!documentSnapshot.getId().equals(userId)) {
                                User user1 = documentSnapshot.toObject(User.class);
                                if (arrayList_key.indexOf(user1.getId()) >= 0) {
                                    mUser.set(arrayList_key.indexOf(user1.getId()), documentSnapshot.toObject(User.class));
                                }
//                                Log.w("modified", user1.getStatus()+"");
//                                Log.w("modified", arrayList_key.indexOf(documentSnapshot.getId())+"");
                            }
                            userAdapter.notifyDataSetChanged();
                            break;
                    }
                }

            }
        });

    }

}
