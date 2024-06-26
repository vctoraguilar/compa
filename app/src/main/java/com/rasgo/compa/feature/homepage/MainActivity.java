package com.rasgo.compa.feature.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.homepage.chat.ChatFragment;
import com.rasgo.compa.feature.homepage.feed.FeedFragment;
import com.rasgo.compa.feature.homepage.notification.NotificationFragment;
import com.rasgo.compa.feature.homepage.settings.SettingsFragment;
import com.rasgo.compa.feature.profile.ProfileActivity;
import com.rasgo.compa.model.user.user;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public user usuario;
    private static final String TAG="MainActivity";
    ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profileButton = findViewById(R.id.profile_button);

        Intent intent = getIntent();
        String userPhotoUrl = intent.getStringExtra("USER_PHOTO_URL");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

       obtenerUsuario();

        profileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class).putExtra("idUsuario", FirebaseAuth.getInstance().getUid());
                startActivity(intent);
            }
        });

        bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView = findViewById(R.id.navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
        
        setBottomNavigationView(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.homeFragment) {
                    setFragment(new FeedFragment());
                    return true;
                } else if (id == R.id.chatFragment) {
                    setFragment(new ChatFragment());
                    return true;
                }
                else if (id == R.id.notificationFragment) {
                    setFragment(new NotificationFragment());
                    return true;
                }
                else if (id == R.id.settingsFragment) {
                    setFragment(new SettingsFragment());
                    return true;
                }
                return false;
            }
        });

        // Load the default fragment
        setFragment(new FeedFragment());

    }

    private void obtenerUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Recuperar los datos del usuario desde Firestore
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String photoUrl = document.getString("photoUrl");
                                // Cargar la foto de perfil en el ImageButton usando Glide
                                if (photoUrl != null) {
                                    Glide.with(MainActivity.this)
                                            .load(photoUrl)
                                            .placeholder(R.drawable.user) // imagen predeterminada mientras carga
                                            .error(R.drawable.user)       // imagen en caso de error
                                            .into(profileButton);
                                } else {
                                    // Manejar caso cuando photoUrl es null
                                    Glide.with(MainActivity.this)
                                            .load(R.drawable.user) // Cargar imagen predeterminada
                                            .into(profileButton);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    });
        }
    }

    private void setBottomNavigationView(NavigationBarView.OnItemSelectedListener listener) {
        bottomNavigationView.setOnItemSelectedListener(listener);
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment).commit();
    }
}