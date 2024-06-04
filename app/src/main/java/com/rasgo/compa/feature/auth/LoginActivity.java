package com.rasgo.compa.feature.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.homepage.MainActivity;
import com.rasgo.compa.model.auth.AuthResponse;
import com.rasgo.compa.utils.ViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN=50;
    private static final String TAG="LoginActivity";
    private SignInButton signInButton;
    public LoginViewModel viewModel;
    private ProgressDialog progressDialog;


    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Cargando ...");
        progressDialog.setMessage("Iniciando Sesíón ...  ");
        viewModel= new ViewModelProvider(this, new ViewModelFactory()).get(LoginViewModel.class);

        //viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        //viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(LoginViewModel.class);

        signInButton=findViewById(R.id.btn_GoogleInicio);

        //Configurando Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient= GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                signIn();
            }
        });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Log.w(TAG, "Google sign in failed: " + e.getStatusCode(), e);
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        progressDialog.show();
        signInButton.setVisibility(View.GONE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    viewModel.login(new UserInfo(
                                            user.getUid(),
                                            user.getDisplayName(),
                                            user.getEmail(),
                                            user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null,
                                            null, // Cover URL can be set to null or provide the correct value
                                            token
                                    )).observe(LoginActivity.this, new Observer<AuthResponse>() {
                                        @Override
                                        public void onChanged(AuthResponse authResponse) {
                                            progressDialog.hide();
                                            Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                            if (authResponse.getAuth() != null) {
                                                Toast.makeText(LoginActivity.this, "Bienvenido, " + user, Toast.LENGTH_SHORT).show();
                                                updateUI(user);
                                            } else {
                                                FirebaseAuth.getInstance().signOut();
                                                updateUI(null);
                                            }
                                        }
                                    });

                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get FCM token", e);
                                progressDialog.hide();
                                signInButton.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                            });

                        }else {
                            Log.w(TAG,"signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    public static class UserInfo {
        String uid, name, email, profileUrl, coverUrl, userToken;

        public UserInfo(String uid, String name, String email, String profileUrl, String coverUrl, String userToken) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.profileUrl = profileUrl;
            this.coverUrl = coverUrl;
            this.userToken = userToken;
        }
    }
}