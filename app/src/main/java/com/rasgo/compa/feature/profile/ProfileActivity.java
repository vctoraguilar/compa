package com.rasgo.compa.feature.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private String uid="",profileUrl="", coverUrl="";
    private int current_state=0;

    private Button btnLogout;


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

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        getSharedPreferences("user_preferences", MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        // Desautenticar de Google y revocar acceso
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(this, revokeTask -> {
                // Redirigir a la actividad de inicio de sesión
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

    }


    public void readProfile(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String ToolbarTitle = document.getString("displayName");
                                String ProfileImage = document.getString("photoUrl");
                                if (ToolbarTitle != null && !ToolbarTitle.isEmpty()) {
                                    String firstName = ToolbarTitle.split(" ")[0];
                                    collapsingToolbarLayout.setTitle(firstName);
                                }
                                if (ProfileImage != null) {
                                    Glide.with(this)
                                            .load(ProfileImage)
                                            .into(profileImage);
                                }
                            } else {
                                Log.d(TAG, "No se encontró documento para el usuario actual");
                            }
                        } else {
                            Log.d(TAG, "Error obteniendo documento: ", task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "No hay usuario autenticado");
        }
    }
}