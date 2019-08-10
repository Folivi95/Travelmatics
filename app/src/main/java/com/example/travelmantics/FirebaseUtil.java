package com.example.travelmantics;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtil {
    public static FirebaseDatabase _firebaseDatabase;
    public static DatabaseReference _databaseReference;
    private static FirebaseUtil _firebaseUtil;
    public static ArrayList<TravelDeal> _deals;

    private FirebaseUtil() {}

    public static void openFbReference(String ref){
        if (_firebaseUtil == null){
            _firebaseUtil = new FirebaseUtil();
            _firebaseDatabase = FirebaseDatabase.getInstance();
        }
        _deals = new ArrayList<>();
        _databaseReference = _firebaseDatabase.getReference().child(ref);
    }
}
