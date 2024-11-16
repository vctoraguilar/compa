package com.rasgo.compa.feature.auth;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.homepage.MainActivity;
import com.rasgo.compa.model.auth.AuthResponse;
//import com.rasgo.compa.utils.ViewModelFactory;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import java.util.HashMap;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;

public class LoginActivity extends AppCompatActivity {

    // Variables globales para la autenticación y Google Sign-In
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 50;
    private static final String TAG = "LoginActivity";

    // Elementos de la interfaz de usuario
    private SignInButton signInButton;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUI();
        configureGoogleSignIn();
        checkCurrentUser();
    }

    // Inicializa la interfaz de usuario y configura el progreso
    private void initializeUI() {
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Cargando...");
        progressDialog.setMessage("Iniciando Sesión...");

        signInButton = findViewById(R.id.btn_GoogleInicio);
        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);

        // Listener para el botón de Google Sign-In
        signInButton.setOnClickListener(v -> signIn());

        EditText emailEditText = findViewById(R.id.et_username);
        EditText passwordEditText = findViewById(R.id.et_password);
        Button loginButton = findViewById(R.id.btnLogIn);

        // Listener para el botón de inicio de sesión con correo y contraseña
        loginButton.setOnClickListener(v -> attemptEmailLogin(emailEditText, passwordEditText));
    }

    // Configura las opciones de Google Sign-In
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }

    // Verifica si ya existe un usuario autenticado
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    // Intenta iniciar sesión con correo y contraseña
    private void attemptEmailLogin(EditText emailEditText, EditText passwordEditText) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (isEmptyField(email, password)) {
            showToast("Faltan campos");
            return;
        }

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        showToast("Error al autenticar");
                    }
                });
    }

    // Verifica si los campos de email o contraseña están vacíos
    private boolean isEmptyField(String email, String password) {
        return TextUtils.isEmpty(email) || TextUtils.isEmpty(password);
    }

    // Muestra un mensaje Toast simplificado
    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Inicia el proceso de autenticación de Google Sign-In
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Gestiona el resultado de la actividad para Google Sign-In
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Gestiona el resultado de Google Sign-In
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed: " + e.getStatusCode(), e);
        }
    }

    // Autentica el usuario con Firebase usando Google Sign-In
    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.show();
        signInButton.setVisibility(View.GONE);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createUserChat(user);
                            navigateToMainActivity();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:fa|re", task.getException());
                        signInButton.setVisibility(View.VISIBLE);
                        showToast("Autenticación fallida");
                    }
                });
    }

    // Crea el chat del usuario usando Stream
    private void createUserChat(FirebaseUser userChat) {
        ChatClient client = new ChatClient.Builder("7r7sx9khusmb", getApplicationContext())
                .withPlugins(
                        new StreamOfflinePluginFactory(getApplicationContext()),
                        new StreamStatePluginFactory(new StatePluginConfig(true, true), getApplicationContext())
                )
                .logLevel(ChatLogLevel.ALL) // Establecer en NOTHING en producción
                .build();

        User user = new User.Builder()
                .withId(userChat.getUid())
                .withName(userChat.getDisplayName())
                .withImage(userChat.getPhotoUrl().toString())
                .build();

        client.connectUser(user, client.devToken(user.getId())).enqueue();
    }

    // Navega a la pantalla principal
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Actualiza la interfaz de usuario dependiendo del estado de autenticación
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            navigateToMainActivity();
        } else {
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    // Navega a la pantalla de registro
    public void onLoginClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
    }
}
