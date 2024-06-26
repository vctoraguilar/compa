package com.rasgo.compa.feature.auth;

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

public class RegisterActivity extends AppCompatActivity {

    public EditText Name;
    public EditText Email;
    public EditText Password;
    public FirebaseAuth mAuth;
    private static final String TAG = "RegisterActivity";

    private Button btnRegister;
    private CheckBox checkTerms;

    private TextView terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRegister = findViewById(R.id.btnRegister);
        checkTerms = findViewById(R.id.checkTerms);
        btnRegister.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        checkTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRegister.setEnabled(isChecked);
        });

        Name = findViewById(R.id.et_name);
        Email = findViewById(R.id.et_email);
        Password = findViewById(R.id.et_password);

        terms=findViewById(R.id.termsText);
        terms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String url = "https://www.compa.pe/terminos";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = Email.getText().toString().trim();
                String passwordUser = Password.getText().toString().trim();
                String nameUser = Name.getText().toString().trim();

                if (emailUser.isEmpty() || passwordUser.isEmpty() || nameUser.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                register(emailUser, passwordUser, nameUser);
            }
        });
        setupPasswordVisibilityToggle();
    }

    private void setupPasswordVisibilityToggle() {
        Password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int drawableEnd = 2; // Index of the drawableEnd in the compound drawables array
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (Password.getRight() - Password.getCompoundDrawables()[drawableEnd].getBounds().width())) {
                        if (Password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
                            Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            Password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_24px, 0, R.drawable.hide_24px, 0);
                        } else {
                            Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            Password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock_24px, 0, R.drawable.eye_24px, 0);
                        }
                        Password.setSelection(Password.getText().length()); // Place cursor at the end of text
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void register(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userid = task.getResult().getUser().getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            user usuario = new user();
                            usuario.setUserId(userid);
                            usuario.setDisplayName(name);
                            usuario.setEmail(email);
                            String defaultPhotoUrl = getUriFromDrawable(R.drawable.male_user_96px);
                            usuario.setPhotoUrl(defaultPhotoUrl);

                            db.collection("users").document(userid).set(usuario)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(RegisterActivity.this, W1Activity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Error al registrar en Firestore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error al registrar: ", task.getException());
                            Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private String getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + drawableId).toString();
    }
    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
    }
}
