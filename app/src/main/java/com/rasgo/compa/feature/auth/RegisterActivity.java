package com.rasgo.compa.feature.auth;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.homepage.MainActivity;
import com.rasgo.compa.feature.welcome.W1Activity;
import com.rasgo.compa.model.user.user;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;

public class RegisterActivity extends AppCompatActivity {

    // Variables globales para la interfaz de usuario
    private EditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton;
    private CheckBox termsCheckBox;
    private TextView termsTextView;
    private FirebaseAuth mAuth;

    private static final String TAG = "RegisterActivity";
    private static final String TERMS_URL = "https://www.compa.pe/terminos";
    private static final int DRAWABLE_END = 2; // Índice del drawableEnd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        setupAuth();
        setupListeners();
    }

    // Método para inicializar la interfaz de usuario
    private void setupUI() {
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de vistas
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        registerButton = findViewById(R.id.btnRegister);
        termsCheckBox = findViewById(R.id.checkTerms);
        termsTextView = findViewById(R.id.termsText);

        // Deshabilitar botón de registro inicialmente
        registerButton.setEnabled(false);
    }

    // Método para inicializar FirebaseAuth
    private void setupAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Método para configurar los listeners
    private void setupListeners() {
        // Listener para habilitar el botón de registro cuando los términos son aceptados
        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                registerButton.setEnabled(isChecked)
        );

        // Listener para mostrar los términos de servicio
        termsTextView.setOnClickListener(v -> openTermsPage());

        // Listener para el botón de registro
        registerButton.setOnClickListener(v -> attemptRegister());

        // Listener para cambiar la visibilidad de la contraseña
        setupPasswordVisibilityToggle();
    }

    // Abre la página de términos de servicio en el navegador
    private void openTermsPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        startActivity(intent);
    }

    // Intenta registrar al usuario
    private void attemptRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        // Validación de campos
        if (isEmptyField(email, password, name)) {
            showToast("Todos los campos son obligatorios");
            return;
        }

        // Registro de usuario
        register(email, password, name);
    }

    // Valida si alguno de los campos está vacío
    private boolean isEmptyField(String email, String password, String name) {
        return email.isEmpty() || password.isEmpty() || name.isEmpty();
    }

    // Método para mostrar un Toast de manera simplificada
    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Registra al usuario en FirebaseAuth y FirebaseFirestore
    private void register(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        createUserInFirestore(userId, email, name);
                    } else {
                        handleRegisterError(task.getException());
                    }
                });
    }

    // Crea un nuevo usuario en Firestore
    private void createUserInFirestore(String userId, String email, String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User usuario = new User();

        db.collection("users").document(userId).set(usuario)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToNextActivity(usuario);
                    } else {
                        showToast("Error al registrar en Firestore: " + task.getException().getMessage());
                    }
                });
    }

    // Maneja los errores de registro
    private void handleRegisterError(Exception exception) {
        Log.e(TAG, "Error al registrar: ", exception);
        showToast("Error al registrar: " + exception.getMessage());
    }

    // Navega a la siguiente actividad después del registro exitoso
    private void navigateToNextActivity(User usuario) {
        createUserChat(usuario);
        Intent intent = new Intent(RegisterActivity.this, W1Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Crea un chat para el usuario utilizando Stream
    private void createUserChat(User usuario) {
        ChatClient client = new ChatClient.Builder("7r7sx9khusmb", getApplicationContext())
                .withPlugins(new StreamOfflinePluginFactory(getApplicationContext()),
                        new StreamStatePluginFactory(new StatePluginConfig(true, true), getApplicationContext()))
                .logLevel(ChatLogLevel.ALL) // Establecer en NOTHING en producción
                .build();

        User chatUser = new User.Builder()
                .withId(usuario.getId())
                .withName(usuario.getName())
                .withImage(usuario.getImage())
                .build();

        client.connectUser(chatUser, client.devToken(chatUser.getId())).enqueue();
    }

    // Método para obtener la URL del drawable
    private String getDefaultPhotoUrl() {
        return Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.male_user_96px).toString();
    }

    // Método para configurar el toggle de visibilidad de la contraseña
    private void setupPasswordVisibilityToggle() {
        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP &&
                    event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                togglePasswordVisibility();
                return true;
            }
            return false;
        });
    }

    // Alterna la visibilidad de la contraseña
    private void togglePasswordVisibility() {
        if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_24px, 0, R.drawable.hide_24px, 0);
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_24px, 0, R.drawable.eye_24px, 0);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    // Navega a la pantalla de inicio de sesión
    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
    }
}
