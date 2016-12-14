package com.csce.tutorapp;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by tylerroper on 10/29/16.
 */

public class SearchResultsActivity extends AppCompatActivity{

    private Button homeBtn, newSearchBtn;
    private ListView tutorList;
    private ArrayList<String> foundTutorKeys;
    private ArrayList<User> foundUsers;
    private ArrayAdapter<User> listAdapter;
    private int foundKeysIndex;
    private StringBuilder stringHolder;

    ArrayList<String> tutorKeys;
    private DatabaseReference fireBaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load the view for the page
        setContentView(R.layout.search_results_activity);
        homeBtn = (Button) findViewById(R.id.home_button);
        newSearchBtn = (Button) findViewById(R.id.new_search_button);
        tutorList = (ListView) findViewById(R.id.tutor_list);

        //go through each of the keys and grab profile information about them all
        foundUsers = new ArrayList<>();
        foundKeysIndex = 0;
        foundTutorKeys = getIntent().getStringArrayListExtra("foundkeys");

        if (foundKeysIndex < foundTutorKeys.size()) {
            ConsumeNextTutorKey(foundTutorKeys.get(foundKeysIndex));
        }

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foundUsers);
        tutorList.setAdapter(listAdapter);

        createButtonListeners();
        fillList();

    }

    private void createButtonListeners ()
    {
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //TODO: Switch to home activity
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
            }
        });

        newSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //TODO: Switch to find tutor activity
                Intent i = new Intent(getApplicationContext(), FindTutorActivity.class);
                startActivity(i);
            }
        });

        tutorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < foundUsers.size() && foundUsers.get(position) != null){
                    Intent convoIntent = new Intent(SearchResultsActivity.this, ProfileActivity.class);
                    convoIntent.putExtra("userid", foundUsers.get(position).getID());
                    startActivity(convoIntent);
                }
            }
        });
    }

    private void fillList() {
        tutorKeys = new ArrayList<String>();
        stringHolder = new StringBuilder("");
        fireBaseRef = FirebaseDatabase.getInstance().getReference();
        fireBaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tutors : dataSnapshot.getChildren()) {
                    if(foundTutorKeys.contains(tutors.getKey().toString())) {
                        stringHolder.append(tutors.child("firstName").getValue().toString()).append(" ").append(tutors.child("lastName").getValue().toString());
                        tutorKeys.add(stringHolder.toString());
                        stringHolder.delete(0,stringHolder.length());
                    }
                }
                ArrayAdapter adapter = new ArrayAdapter(SearchResultsActivity.this, android.R.layout.simple_list_item_1, tutorKeys);
                tutorList.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ConsumeNextTutorKey(String key){
        DatabaseReference userProfileDb = FirebaseDatabase.getInstance().getReference("users").child(key);
        userProfileDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User foundUser = dataSnapshot.getValue(User.class); //get the user info from Firebase
                if (foundUser != null){
                    foundUsers.add(foundUser);
                }

                foundKeysIndex++;
                if (foundKeysIndex < foundTutorKeys.size()) {
                    ConsumeNextTutorKey(foundTutorKeys.get(foundKeysIndex));
                }
                else{
                    FillListResults();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FillListResults(){
        listAdapter.notifyDataSetChanged();
    }
}
