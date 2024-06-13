package com.rasgo.compa.feature.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rasgo.compa.R;

public class ProfileActivity extends AppCompatActivity {

    private String uid="",profileUrl="", coverUrl="";
    private int current_state=0;

    /*
        0=perfil cargando
        1=compas
        2=hemos enviado solicitud de compa
        3=hemos recibido solicitud de compa
        4=no somos compas
        5=nuestro perfil
     */
    private Button profileOptionButton;
    private ImageView profileImage, coverImage;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        toolbar = findViewById(R.id.toolbar_bar);
        profileOptionButton = findViewById(R.id.profile_action_btn);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);


        setSupportActionBar(toolbar);
        readProfile();


    }

    public void readProfile(){
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (DocumentSnapshot document: task.getResult()){
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            if (document.exists()){
                                String ToolbarTitle =document.getString("displayName");
                                String ProfileImage =document.getString("photoUrl");
                                if (ToolbarTitle != null && !ToolbarTitle.isEmpty()) {
                                    String firstName = ToolbarTitle.split(" ")[0];
                                    collapsingToolbarLayout.setTitle(firstName);
                                }
                                if (ProfileImage != null) {
                                    Glide.with(this)
                                            .load(ProfileImage)
                                            .into(profileImage);
                                }
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "No se encontró documento");
                    }

                });


    }
}