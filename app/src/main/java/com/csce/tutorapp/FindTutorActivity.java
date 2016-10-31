package com.csce.tutorapp;

// Author: Joel Ashman
// Date Created: 10/22/2016
// Last Update: 10/23/2016
// Description: Gets information from the user to do a search of users that meet the criteria.

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindTutorActivity extends AppCompatActivity {
    MultiAutoCompleteTextView mactvInstitutions;
    FirebaseAuth authTempUser;
    FirebaseAuth.AuthStateListener authListener;
    private static final String TAG = "AnonymousAuth";

    private DatabaseReference refDatabase;
    private DatabaseReference refInstitution;


    // Create adapters for the autocomplete text fields.
    ArrayAdapter<String> adapterInstitution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore last saved instance.
        super.onCreate(savedInstanceState);

        // Load the resources for the layout.
        setContentView(R.layout.activity_find_tutor);

        refDatabase = FirebaseDatabase.getInstance().getReference("https://tutoring-app-e2bdd/");
        refInstitution = refDatabase.child("institution");

        //// The following section is to create temporary authentication for testing purposes. Should be removed on deploy.
        // Create temporary authentication instance.
        authTempUser = FirebaseAuth.getInstance();

        // Create a listener to respond to changes in the signin state.
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in.
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else {
                    // User is signed out.
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        adapterInstitution = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
    }

    @Override
    public void onStart(){
        super.onStart();

        // Add authentication state listener.
        authTempUser.addAuthStateListener(authListener);
        signInAnonymously();

        // Listens for user to attempt to put information in the institution field.
        // Grab data from the database to make suggestions as the user types their institution.
        ValueEventListener mactvInstitutionsFieldListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot institutions: dataSnapshot.getChildren()) {
                    String institution = institutions.getValue(String.class);
                    adapterInstitution.add(institution);
                    Log.d(TAG, "institution:" + institution);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        refInstitution.addValueEventListener(mactvInstitutionsFieldListener);
        /*refDatabase.child("institution").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot institutions: dataSnapshot.getChildren()){
                    String institution = institutions.getValue(String.class);
                    adapterInstitution.add(institution);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        // Assign the institutions field, finding it by field.
        mactvInstitutions = (MultiAutoCompleteTextView) findViewById(R.id.mactv_institutions);
        // Set the adapter for the institutions field.
        mactvInstitutions.setAdapter(adapterInstitution);

    }

    @Override
    public void onStop(){
        super.onStop();

        // If the authentication listener is active, kill it.
        if(authListener != null){
            authTempUser.removeAuthStateListener(authListener);
        }

    }

    private void signInAnonymously() {
        authTempUser.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // Notify user authentication failed.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(FindTutorActivity.this, "Failed to authenticate.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}