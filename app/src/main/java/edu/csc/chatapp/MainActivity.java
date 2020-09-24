package edu.csc.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.csc.chatapp.Fragments.ChatFragment;
import edu.csc.chatapp.Fragments.ProfileFragment;
import edu.csc.chatapp.Fragments.UserFragment;
import edu.csc.chatapp.Model.User;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    Toolbar toolbar;
    FirebaseFirestore firestore;
    TabLayout tabLayout;
    ViewPager viewPager;
    String user_id;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        toolbar = findViewById(R.id.toolbar_main);
        //must write this to show menu
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // fire base
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("image_user");

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = firestore.collection("Users").document(user_id);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                User user = documentSnapshot.toObject(User.class);

                username.setText(user.getName());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);

                   // getImage(user);
                }
            }
        });

        // tab and view pager
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatFragment(user_id), "Chat");
        viewPagerAdapter.addFragment(new UserFragment(user_id), "User");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_with);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_group_user);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_profile);
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
//
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
        DocumentReference documentReference = firestore.collection("Users").document(user_id);
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        documentReference.update(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

}
