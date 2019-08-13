package com.example.travelmantics;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class FirebaseUtil {
    public static FirebaseDatabase _firebaseDatabase;
    public static DatabaseReference _databaseReference;
    private static FirebaseUtil _firebaseUtil;
    public static FirebaseAuth _firebaseAuth;
    public static FirebaseAuth.AuthStateListener _authListener;
    public static ArrayList<TravelDeal> _deals;
    private static final int RC_SIGN_IN = 123;
    private static ListActivity caller;
    public static boolean isAdmin;

    private FirebaseUtil() {}

    public static void openFbReference(String ref, ListActivity callerActivity){
        if (_firebaseUtil == null){
            _firebaseUtil = new FirebaseUtil();
            _firebaseDatabase = FirebaseDatabase.getInstance();
            _firebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            _authListener = firebaseAuth -> {
                if(_firebaseAuth.getCurrentUser() == null){
                    FirebaseUtil.signIn();
                }else {
                    String userId = _firebaseAuth.getUid();
                    checkAdmin(userId);
                }

                Toast.makeText(callerActivity.getBaseContext(), "Welcome Back!", Toast.LENGTH_LONG).show();
            };
        }
        _deals = new ArrayList<>();
        _databaseReference = _firebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = _firebaseDatabase.getReference().child("administrators").child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    private static void signIn(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachListener(){
        _firebaseAuth.addAuthStateListener(_authListener);
    }

    public static void detachListener(){
        _firebaseAuth.removeAuthStateListener(_authListener);
    }
}
