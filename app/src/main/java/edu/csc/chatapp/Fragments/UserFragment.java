package edu.csc.chatapp.Fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.csc.chatapp.Adapters.UserAdapter;
import edu.csc.chatapp.R;
import edu.csc.chatapp.Model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter, userAdapter2;
    private ArrayList<User> userArrayList, userArrayList2;
    private String userId;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private ArrayList<String> arrayList_key;

    private EditText search_user;
    public UserFragment() {
        // Required empty public constructor
    }

    public UserFragment(String user_id) {
        this.userId = user_id;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Users");

        View view = inflater.inflate(R.layout.fragment_user, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        search_user = view.findViewById(R.id.search_user);

        userArrayList = new ArrayList<>();
        arrayList_key = new ArrayList<>();
        userArrayList2 = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userArrayList, false);
        userAdapter2 = new UserAdapter(getContext(), userArrayList2, false);

        readUsers();

        search_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void searchUsers(final String toString) {
        collectionReference.orderBy("name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                userArrayList.clear();
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();

                    switch (documentChange.getType()) {
                        case ADDED:
                            if (!documentSnapshot.getId().equals(userId)) {
                                User user = documentSnapshot.toObject(User.class);
                                if(!toString.equals("")) {
                                    Log.w("search", user.getName().toLowerCase().startsWith(toString)+"");
                                    if (user.getName().toLowerCase().startsWith(toString)) {
                                        //arrayList_key.add(documentSnapshot.getId());
                                        userArrayList.add(documentSnapshot.toObject(User.class));
                                        userAdapter.notifyDataSetChanged();
                                    }
                                }
                                else{
                                    //arrayList_key.add(documentSnapshot.getId());
                                    userArrayList.add(documentSnapshot.toObject(User.class));
                                    userAdapter.notifyDataSetChanged();
                                }
                                recyclerView.setAdapter(userAdapter);
                                userAdapter.notifyDataSetChanged();
                            }
                            break;
                        case REMOVED:
                            userArrayList.clear();
                            userAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            if (!documentSnapshot.getId().equals(userId)) {
                                if (arrayList_key.indexOf(documentSnapshot.getId()) >=0) {
                                    userArrayList.set(arrayList_key.indexOf(documentSnapshot.getId()), documentSnapshot.toObject(User.class));
                                }
                                recyclerView.setAdapter(userAdapter);
                                userAdapter.notifyDataSetChanged();
                            }
                            break;
                    }
                }
            }
        });
    }

    private void readUsers(){
        collectionReference.orderBy("name").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //userArrayList.clear();
                if(search_user.getText().toString().equals("")) {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        switch (documentChange.getType()) {
                            case ADDED:
                                if (!documentSnapshot.getId().equals(userId)) {
                                    arrayList_key.add(documentSnapshot.getId());
                                    userArrayList2.add(documentSnapshot.toObject(User.class));

                                    recyclerView.setAdapter(userAdapter2);
                                    userAdapter2.notifyDataSetChanged();
                                }
                                break;
                            case REMOVED:
                                userArrayList2.clear();
                                userAdapter2.notifyDataSetChanged();
                                break;
                            case MODIFIED:
                                if (!documentSnapshot.getId().equals(userId)) {
                                    if (arrayList_key.indexOf(documentSnapshot.getId()) >= 0) {
                                        userArrayList2.set(arrayList_key.indexOf(documentSnapshot.getId()), documentSnapshot.toObject(User.class));
                                    }
                                    recyclerView.setAdapter(userAdapter2);
                                    userAdapter2.notifyDataSetChanged();
                                }
                                break;
                        }
                    }
                }
            }
        });
    }

}
