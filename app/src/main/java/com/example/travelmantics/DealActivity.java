package com.example.travelmantics;

//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class DealActivity extends AppCompatActivity {
    //fields declaration
    private FirebaseDatabase _firebaseDatabase;
    private DatabaseReference _databaseReference;
    private ListActivity listActivity;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    ImageView imageView;
    TravelDeal deal;
    private static final int PICTURE_RESULT = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        FirebaseUtil.openFbReference("traveldeals", listActivity);
        _firebaseDatabase = FirebaseUtil._firebaseDatabase;
        _databaseReference = FirebaseUtil._databaseReference;
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        imageView = findViewById(R.id.image);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
            intent1.setType("image/jpeg");
            intent1.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(intent1.createChooser(intent1, "Insert Picture"), PICTURE_RESULT);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this,"Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin == true){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil._storageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, taskSnapshot -> {
                String imageUrl = taskSnapshot.getMetadata().getReference().getPath();
                String imageName = taskSnapshot.getStorage().getPath();
                deal.setImageName(imageName);
                deal.setImageUrl(imageUrl);
                Log.d("Url: ", imageUrl);
                Log.d("Image Name: ", imageName);
                ref.getPath();
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    deal.setImageUrl(uri.toString());
                    showImage(uri.toString());
                });
            });
        }
    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());

        //use database reference to push saved items to database
        if (deal.getId() == null){
            _databaseReference.push().setValue(deal);
        }else {
            _databaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if(deal == null){
            Toast.makeText(this, "Please create deal before attempting to Delete", Toast.LENGTH_SHORT).show();
        }
        _databaseReference.child(deal.getId()).removeValue();

        if (deal.getImageName() != null && deal.getImageName().isEmpty()==false){
            StorageReference picRef = FirebaseUtil._storage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(aVoid -> {
                Log.d("Delete Image: ", "Image successfully deleted");
            }).addOnFailureListener(e -> {
                Log.d("Delete Image: ", e.getMessage());
            });
        }
    }

    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");

        txtTitle.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if(url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get().load(url).resize(width, width*2/3).centerCrop().into(imageView);
        }
    }
}
