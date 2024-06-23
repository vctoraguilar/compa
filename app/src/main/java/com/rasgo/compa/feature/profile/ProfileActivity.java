package com.rasgo.compa.feature.profile;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.google.android.material.internal.ContextUtils.getActivity;
import static java.security.AccessController.getContext;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.auth.LoginActivity;
import com.rasgo.compa.adapters.BusinessInfoAdapter;

import com.rasgo.compa.model.user.user;

import java.text.BreakIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.ContextCompat;

public class ProfileActivity extends AppCompatActivity {

    // Imagenes
    private static final int REQUEST_IMAGE_PICKER_PROFILE = 100;
    private static final int REQUEST_IMAGE_PICKER_COVER = 101;
    private static final int STORAGE_PERMISSION_CODE=102;

    private String uid="",profileUrl="", coverUrl="";
    private int current_state=0;
    private ImageButton btnLogout;
    private static final int IMAGE_REQUEST=1;

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
    private BusinessInfoAdapter adapter;

    private List<String> businessInfoList = new ArrayList<>();

    public String idUsuario;
    public String nombreUsuario;
    public String urlImagen;
    public String urlCoverAux;
    public int state=0;

    private DocumentReference reference;
    private StorageReference storageReference;
    private FirebaseUser fuser;
    private Uri imageUri;
    private Uri coverImageUri;
    private StorageTask uploadTask;
    private String mUri;
    private boolean profileOptionSelect=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        idUsuario = getIntent().getStringExtra("idUsuario");
        urlImagen = getIntent().getStringExtra("photoUrl");
        urlCoverAux=getIntent().getStringExtra("coverUrl");
        nombreUsuario = getIntent().getStringExtra("name");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        profileImage = findViewById(R.id.profile_image);
        coverImage = findViewById(R.id.profile_cover);
        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileOptionSelect==true){
                    openCoverGallery();
                }

            }
        });

        reference = FirebaseFirestore.getInstance().collection("users").document(fuser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileOptionSelect==true) {
                    openGallery();
                }
            }
        });

        toolbar = findViewById(R.id.toolbar_bar);

        profileOptionButton = findViewById(R.id.profile_action_btn);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        recyclerView = findViewById(R.id.recyv_profile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String businessName = "Nombre del Negocio";
        String businessDescription = "Descripción del negocio";

        adapter = new BusinessInfoAdapter(businessName, businessDescription);
        recyclerView.setAdapter(adapter);

        //Retroceder
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher()); // Handle back button click

        Intent intent = getIntent();
        String userId = intent.getStringExtra("user");
        state = intent.getIntExtra("state", -1);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusinessInfoAdapter(this, businessInfoList);
        recyclerView.setAdapter(adapter);

        //loadUserProfile(userId);

        switch (state) {
            case 0:
                // Perfil cargando
                break;
            case 1:
                // Usuario agregado como amigo
                break;
            case 2:
                // Hemos enviado solicitud de amistad
                break;
            case 3:
                // Hemos recibido solicitud de amistad
                break;
            case 4:
                // Usuario que no es amigo
                profileOptionButton.setText("Seamos Compas");
                profileOptionButton.setOnClickListener(v ->{
                    sendFriendRequest(userId);
                });
                break;
            case 5:
                profileOptionButton.setText("Editar Perfil");

                break;
            default:
                // Estado desconocido
                break;
        }


        setSupportActionBar(toolbar);
        readProfile();
        loadBusinessInfo();


        //Botón Editar
        profileOptionButton = findViewById(R.id.profile_action_btn);
        ImageView editProfile= findViewById(R.id.profile_imageEdit);
        ImageView editCover= findViewById(R.id.profile_coverEdit);

        profileOptionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (profileOptionButton.getText().toString().equals("Editar Perfil")){
                    profileOptionSelect=true;
                    adapter.setEditable(true);
                    profileOptionButton.setText("Guardar");
                    editProfile.setVisibility(View.VISIBLE);
                    editCover.setVisibility(View.VISIBLE);

                }else{
                    adapter.setEditable(false);
                    profileOptionButton.setText("Editar Perfil");
                    saveBusinessInfo();
                    editProfile.setVisibility(View.INVISIBLE);
                    editCover.setVisibility(View.INVISIBLE);
                }

            }
        });

        //Botón Cerrar Sesión
        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void openCoverGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_PICKER_COVER);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        user updateUser = new user();

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Subiendo", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        } else if (requestCode == REQUEST_IMAGE_PICKER_COVER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            coverImageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(ProfileActivity.this, "Subiendo", Toast.LENGTH_SHORT).show();
            } else {
                uploadCoverImage();
            }
        }


    }

    private void uploadCoverImage() {
        if (coverImageUri != null) {
            String fileExtension = getFileExtension(coverImageUri);
            if (fileExtension == null) {
                Toast.makeText(ProfileActivity.this, "Error obteniendo extensión de archivo", Toast.LENGTH_SHORT).show();
                return;
            }
            StorageReference fileReference = storageReference.child("cover_" + System.currentTimeMillis() + "." + fileExtension);
            uploadTask = fileReference.putFile(coverImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            if (downloadUri != null) {
                                                String coverUri = downloadUri.toString();
                                                updateCoverImageUrl(coverUri);
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Error obteniendo URL de descarga", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Error obteniendo URL de descarga", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Exception e = task.getException();
                                if (e != null) {
                                    Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Upload failed", e);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Upload failed", e);
                        }
                    });
        } else {
            Toast.makeText(ProfileActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoverImageUrl(String url) {
        String userId = fuser.getUid();
        db.collection("users").document(userId).update("coverUrl", url)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Glide.with(ProfileActivity.this).load(url).into(coverImage);
                            Toast.makeText(ProfileActivity.this, "Cover upload successful", Toast.LENGTH_SHORT).show();
                            readProfile();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error actualizando URL de portada en Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImage() {
        if (imageUri != null) {
            String fileExtension = getFileExtension(imageUri);
            if (fileExtension == null) {
                Toast.makeText(ProfileActivity.this, "Error obteniendo extensión de archivo", Toast.LENGTH_SHORT).show();
                return;
            }
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + fileExtension);
            uploadTask = fileReference.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            if (downloadUri != null) {
                                                mUri = downloadUri.toString();
                                                updateProfileImageUrl(mUri);
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Error obteniendo URL de descarga", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Error obteniendo URL de descarga", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Upload failed", e);
                        }
                    });
        } else {
            Toast.makeText(ProfileActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateProfileImageUrl(String url) {
        String userId = fuser.getUid();
        db.collection("users").document(userId).update("photoUrl", url)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Glide.with(ProfileActivity.this).load(url).into(profileImage);
                            Toast.makeText(ProfileActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                            readProfile();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error actualizando URL de imagen en Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void readProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    user duser = document.toObject(user.class);
                                    if (currentUser != null && idUsuario.equals(currentUser.getUid())) {
                                        if (duser != null) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            String ToolbarTitle = document.getString("displayName");
                                            String ProfileImage = document.getString("photoUrl");
                                            String CoverImage = document.getString("coverUrl");
                                            if (ToolbarTitle != null && !ToolbarTitle.isEmpty()) {
                                                String firstName = ToolbarTitle.split(" ")[0];
                                                collapsingToolbarLayout.setTitle(firstName);
                                            }
                                            if (ProfileImage != null) {
                                                Glide.with(ProfileActivity.this)
                                                        .load(ProfileImage)
                                                        .into(profileImage);
                                            }
                                            if (CoverImage != null) {
                                                Glide.with(ProfileActivity.this)
                                                        .load(CoverImage)
                                                        .into(coverImage);
                                            }
                                        }
                                    }
                                    else {
                                        String ToolbarTitle2=nombreUsuario;
                                        String CoverImage = coverUrl;
                                        if (ToolbarTitle2 != null && !ToolbarTitle2.isEmpty()) {
                                            String firstName = ToolbarTitle2.split(" ")[0];
                                            collapsingToolbarLayout.setTitle(firstName);
                                        }
                                        Glide.with(ProfileActivity.this)
                                                .load(urlImagen)
                                                .into(profileImage);
                                        Glide.with(ProfileActivity.this)
                                                    .load(urlCoverAux)
                                                    .into(coverImage);
                                    }
                                } else {
                                    Toast.makeText(ProfileActivity.this, "No se encontró documento para el usuario actual", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ProfileActivity.this, "Error obteniendo documento", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(ProfileActivity.this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendFriendRequest(String userId) {
        Toast.makeText(this, "Solicitud de Compa enviada a " + userId, Toast.LENGTH_SHORT).show();

    }

    private void saveBusinessInfo() {
        EditText etBusinessName = recyclerView.findViewById(R.id.et_business_name);
        EditText etBusinessDescription = recyclerView.findViewById(R.id.et_business_description);

        String updatedBusinessName = etBusinessName.getText().toString();
        String updatedBusinessDescription = etBusinessDescription.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db.collection("users").document(currentUserId)
                    .update("businessName", updatedBusinessName,
                            "businessDescription", updatedBusinessDescription)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadBusinessInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db.collection("users").document(currentUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String businessName = document.getString("businessName");
                                String businessDescription = document.getString("businessDescription");

                                if (businessName != null && businessDescription != null) {
                                    adapter.setBusinessInfo(businessName, businessDescription);
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


//    private void checkFriendRequests() {
//        db.collection("friendRequests").document(currentUser.getUid())
//                .collection("sentRequests").document(userId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            // Hemos enviado una solicitud de amistad
//                            setState(2);
//                        } else {
//                            // Verificar si hemos recibido una solicitud de amistad
//                            db.collection("friendRequests").document(currentUser.getUid())
//                                    .collection("receivedRequests").document(userId)
//                                    .get()
//                                    .addOnCompleteListener(task2 -> {
//                                        if (task2.isSuccessful()) {
//                                            DocumentSnapshot document2 = task2.getResult();
//                                            if (document2.exists()) {
//                                                // Hemos recibido una solicitud de amistad
//                                                setState(3);
//                                            } else {
//                                                // Usuario que no es amigo
//                                                setState(4);
//                                            }
//                                        } else {
//                                            Log.d(TAG, "Error al verificar las solicitudes recibidas: ", task2.getException());
//                                        }
//                                    });
//                        }
//                    } else {
//                        Log.d(TAG, "Error al verificar las solicitudes enviadas: ", task.getException());
//                    }
//                });
//    }

//    private void setState(int state) {
//        // Configurar el estado del perfil basado en el valor de state
//        switch (state) {
//            case 0:
//                // Perfil cargando
//                break;
//            case 1:
//                // Usuario agregado como amigo
//                break;
//            case 2:
//                // Hemos enviado solicitud de amistad
//                break;
//            case 3:
//                // Hemos recibido solicitud de amistad
//                break;
//            case 4:
//                // Usuario que no es amigo
//                break;
//            case 5:
//                // Nuestro perfil
//                break;
//        }
//    }

//    private void loadUserProfile(String userId) {
//        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists()) {
//                    user user = document.toObject(user.class);
//                    if (user != null) {
//
////                       displayName.setText(user.getDisplayName());
////                       businessName.setText(user.getBusinessName());
////                        businessInfoList.clear();
////                        businessInfoList.addAll(user.getBusinessInfo()); // Suponiendo que user tiene un método getBusinessInfo()
////                        adapter.notifyDataSetChanged();
//                        Glide.with(this).load(user.getPhotoUrl()).into(profileImage);
//                    }
//                } else {
//                    Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}