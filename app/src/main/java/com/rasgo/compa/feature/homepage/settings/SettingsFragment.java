package com.rasgo.compa.feature.homepage.settings;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.auth.LoginActivity;
import com.rasgo.compa.feature.profile.ProfileActivity;

public class SettingsFragment extends Fragment {

    private ImageButton btnLogout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the button
        btnLogout = view.findViewById(R.id.btn_logout);

        // Set an OnClickListener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });
    }
    private void showLogoutConfirmationDialog() {
        // Crear el diálogo de confirmación
        new AlertDialog.Builder(getActivity())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de Cerrar Sesión?")
                .setPositiveButton("Sí", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Obtener SharedPreferences desde el contexto de la actividad
        if (getActivity() != null) {
            getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE).edit().clear().apply();
            FirebaseAuth.getInstance().signOut();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

            // Desautenticar de Google y revocar acceso
            googleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
                googleSignInClient.revokeAccess().addOnCompleteListener(getActivity(), revokeTask -> {
                    // Redirigir a la actividad de inicio de sesión
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                });
            });
        }
    }

}