package com.rasgo.compa.feature.profile;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rasgo.compa.R;
import com.rasgo.compa.adapters.PhotoAdapter;

import com.rasgo.compa.feature.auth.RegisterActivity;
import com.rasgo.compa.feature.chat.ChatActivity;
import com.rasgo.compa.feature.homepage.MainActivity;
import com.rasgo.compa.model.user.user;
import com.rasgo.compa.utils.GetStream;

import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.errors.cause.StreamException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;

public class ProfileActivity extends AppCompatActivity {

    private static final int STATE_LOADING = 0;
    private static final int STATE_FRIEND = 1;
    private static final int STATE_REQUEST_SENT = 2;
    private static final int STATE_REQUEST_RECEIVED = 3;
    private static final int STATE_NOT_FRIEND = 4;
    private static final int STATE_MY_PROFILE = 5;
    // Imagenes
    private static final int REQUEST_IMAGE_PICKER_PROFILE = 100;
    private static final int REQUEST_IMAGE_PICKER_COVER = 101;
    private static final int SELECT_IMAGE_REQUEST = 105;

    private static final int STORAGE_PERMISSION_CODE=102;

    private String uid="",profileUrl="", coverUrl="";
    private int current_state=0;
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
    private FloatingActionButton addPhotoButton;
    private ImageView profileImage, coverImage;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private List<String> businessInfoList = new ArrayList<>();

    public String idUsuario;
    public String nombreUsuario;
    public String urlImagen;
    public String urlCoverAux;
    public int state;

    public String currentUserId;

    private DocumentReference reference;
    private StorageReference storageReference;
    private FirebaseUser fuser;
    private Uri imageUri;
    private Uri coverImageUri;
    private StorageTask uploadTask;
    private String mUri;
    private boolean profileOptionSelect=false;
    private ImageView editProfile;
    private ImageView editCover;
    public EditText et_businessName;
    public EditText et_businessDescription;
    public EditText et_businessEmail;
    private PhotoAdapter photoAdapter;
    private List<Uri> photoUris;

    public FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        progressBar=findViewById(R.id.progressbar);
        //progressBar.setVisibility(View.VISIBLE);

        idUsuario = getIntent().getStringExtra("idUsuario");
        urlImagen = getIntent().getStringExtra("photoUrl");
        urlCoverAux=getIntent().getStringExtra("coverUrl");
        nombreUsuario = getIntent().getStringExtra("name");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        profileImage = findViewById(R.id.profile_image);
        coverImage = findViewById(R.id.profile_cover);

        addPhotoButton = findViewById(R.id.add_photoButton);
        addPhotoButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        et_businessName=findViewById(R.id.et_business_name);
        et_businessEmail=findViewById(R.id.et_business_email);
        et_businessDescription=findViewById(R.id.et_business_description);

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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        photoUris = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, photoUris);
        recyclerView.setAdapter(photoAdapter);

        //Retroceder
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher());

//        toolbar.setNavigationIcon(R.drawable.back);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                getOnBackPressedDispatcher();
//            }
//        });

       //Botón Editar
        profileOptionButton = findViewById(R.id.profile_action_btn);
        editProfile= findViewById(R.id.profile_imageEdit);
        editCover= findViewById(R.id.profile_coverEdit);

        setEditTextEnabled(et_businessName, false);
        setEditTextEnabled(et_businessEmail, false);
        setEditTextEnabled(et_businessDescription, false);

        setSupportActionBar(toolbar);
        readProfile();
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE_REQUEST);
    }
    private void setEditTextEnabled(EditText editText, boolean enabled) {
        editText.setEnabled(enabled);

        if (enabled) {
            // Restaurar el fondo por defecto cuando está habilitado
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                editText.setBackground(getDrawable(android.R.drawable.edit_text));
            } else {
                editText.setBackground(getResources().getDrawable(android.R.drawable.edit_text));
            }
        } else {
            // Usar un fondo transparente cuando está deshabilitado
            editText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }
    private void addImage(Uri uri) {
        photoUris.add(uri);
        photoAdapter.notifyItemInserted(photoUris.size() - 1);
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
        } else if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadPhoto(selectedImageUri);
            }
        }
    }
    private void uploadPhoto(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("user_photos/" + currentUser.getUid() + "/" + UUID.randomUUID().toString() + ".jpg");
            uploadTask = fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUri = uri.toString();
                                    saveImageUriToFirestore(uri);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Fallo al subir la foto. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageUriToFirestore(Uri uri) {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        userRef.update("imageUris", FieldValue.arrayUnion(uri.toString()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addImage(uri);
                        Toast.makeText(ProfileActivity.this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error subiendo foto" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                                    Toast.makeText(ProfileActivity.this, "Fallo en la carga. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Falló la carga", e);
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
        currentUserId = fuser.getUid();
        db.collection("users").document(currentUserId).update("coverUrl", url)
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
                                Toast.makeText(ProfileActivity.this, "Falló la carga", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Falló la carga " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Falló la carga", e);
                        }
                    });
        } else {
            Toast.makeText(ProfileActivity.this, "Ningún archivo seleccionado", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveBusinessInfo() {
        EditText etBusinessName = findViewById(R.id.et_business_name);
        EditText etBusinessDescription = findViewById(R.id.et_business_description);
        EditText etBusinessEmail = findViewById(R.id.et_business_email);

        String updatedBusinessName = etBusinessName.getText().toString();
        String updatedBusinessDescription = etBusinessDescription.getText().toString();
        String updatedBusinessEmail = etBusinessEmail.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            db.collection("users").document(currentUserId)
                    .update("businessName", updatedBusinessName,
                            "businessDescription", updatedBusinessDescription,
                            "businessEmail", updatedBusinessEmail)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void loadBusinessInfo(user usuario) {

        if (usuario != null) {
            String userId = usuario.getUserId();
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String businessName = document.getString("businessName");
                                String businessDescription = document.getString("businessDescription");
                                String businessEmail = document.getString("businessEmail");

                                if (businessName != null && businessDescription != null && businessEmail != null) {
                                    et_businessName.setText(businessName);
                                    et_businessDescription.setText(businessDescription);
                                    et_businessEmail.setText(businessEmail);
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

    private void updateProfileImageUrl(String url) {
        currentUserId = fuser.getUid();
        db.collection("users").document(currentUserId).update("photoUrl", url)
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
    private void logStateChange(String newState) {
        Log.d(TAG, "Changing state to: " + newState);
    }

    private void logError(String message, Exception exception) {
        Log.e(TAG, message, exception);
    }
    private void switcherState() {
        switch (state) {
            case STATE_LOADING:
                logStateChange("STATE_LOADING");
                break;
            case STATE_FRIEND:
                logStateChange("STATE_FRIEND");
                handleFriendState();
                break;
            case STATE_REQUEST_SENT:
                logStateChange("STATE_REQUEST_SENT");
                handleRequestSentState();
                break;
            case STATE_REQUEST_RECEIVED:
                logStateChange("STATE_REQUEST_RECEIVED");
                handleRequestReceivedState();
                break;
            case STATE_NOT_FRIEND:
                logStateChange("STATE_NOT_FRIEND");
                handleNotFriendState();
                break;
            case STATE_MY_PROFILE:
                logStateChange("STATE_MY_PROFILE");
                handleMyProfileState();
                break;
            default:
                logStateChange("UNKNOWN_STATE");
                break;
        }
    }



    private void startChat() {
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUserId != null) {
                    StreamOfflinePluginFactory streamOfflinePluginFactory = new StreamOfflinePluginFactory(
                            getApplicationContext()
                    );
                    StreamStatePluginFactory streamStatePluginFactory = new StreamStatePluginFactory(
                            new StatePluginConfig(true, true), getApplicationContext()
                    );

                    // Step 2 - Set up the client for API calls with the plugin for offline storage
                    ChatClient client = new ChatClient.Builder("7r7sx9khusmb", getApplicationContext())
                            .withPlugins(streamOfflinePluginFactory, streamStatePluginFactory)
                            .logLevel(ChatLogLevel.ALL) // Set to NOTHING in prod
                            .build();

                    User user = new User.Builder()
                            .withId(currentUser.getUid())
                            .build();

                    client.connectUser(
                            user,
                            client.devToken(user.getId())
                    ).enqueue();

                    ChannelClient channelClient = client.channel("messaging", "");

                    Map<String, Object> extraData = new HashMap<>();
                    List<String> memberIds = new LinkedList<>();
                    memberIds.add(currentUser.getUid());
                    memberIds.add(idUsuario);

                    channelClient.create(memberIds, extraData)
                            .enqueue(result -> {
                                if (result.isSuccess()) {
                                    Channel newChannel = result.getOrNull();
                                    Log.d(TAG, "Channel created" + newChannel.getCid());
                                    startActivity(ChatActivity.newIntent(getApplicationContext(), newChannel));
                                } else {
                                    Log.d(TAG, "Channel created" + result.errorOrNull());
                                }
                            });
                }
            }
//            public void onClick(View view) {
//                if (currentUserId != null) {
//                    ChatClient client = new ChatClient.Builder("7r7sx9khusmb", getApplicationContext()).build();
//                    User user = new User.Builder()
//                            .withId(currentUser.getUid())
//                            .withName(currentUser.getDisplayName().toString())
//                            .withImage(currentUser.getPhotoUrl().toString())
//                            .build();
//
//                    client.connectUser(
//                            user,
//                            client.devToken(user.getId())
//                    ).enqueue(result -> {
//                        if (result.isSuccess()) {
//                            client.createChannel(
//                                    new Channel().withType("messaging")
//                                            .withMembers(Arrays.asList(user.getId(), idUsuario))
//                            ).enqueue(channelResult -> {
//                                if (channelResult.isSuccess()) {
//                                    Channel channel = channelResult.data();
//                                    startActivity(ChatActivity.newIntent(getApplicationContext(), channel.getCid()));
//                                } else {
//                                    // Manejar error en la creación del canal
//                                    Log.e("ChannelCreationError", channelResult.error().getMessage());
//                                }
//                            });
//                        } else {
//                            // Manejar error en la conexión del usuario
//                            Log.e("UserConnectionError", result.error().getMessage());
//                        }
//                    });
//                }
//            }
        });

    }
    private void handleFriendState() {
        profileOptionButton.setText("Compa");
        profileOptionButton.setEnabled(false);
        addPhotoButton.setVisibility(View.VISIBLE);
        addPhotoButton.setImageResource(R.drawable.chat_message_48px);
        startChat();
    }

    private void handleRequestSentState() {
        profileOptionButton.setText("Solicitud Enviada");
        profileOptionButton.setEnabled(false);
        addPhotoButton.setVisibility(View.GONE);

    }

    private void handleRequestReceivedState() {
        profileOptionButton.setText("Aceptar Solicitud");
        profileOptionButton.setOnClickListener(v -> acceptFriendRequest());
        addPhotoButton.setVisibility(View.GONE);
    }

    private void handleNotFriendState() {
        profileOptionButton.setText("Seamos Compas");
        profileOptionButton.setOnClickListener(v -> sendFriendRequest(idUsuario));
        addPhotoButton.setVisibility(View.GONE);
    }

    private void handleMyProfileState() {
        addPhotoButton.setVisibility(View.VISIBLE);
        profileOptionButton.setOnClickListener(v -> {
            if (profileOptionButton.getText().toString().equals("Editar Perfil")) {
                enableProfileEditing();
            } else {
                saveProfileEdits();
            }
        });
    }

    private void enableProfileEditing() {
        profileOptionSelect = true;
        profileOptionButton.setText("Guardar");
        editProfile.setVisibility(View.VISIBLE);
        editCover.setVisibility(View.VISIBLE);
        setEditTextEnabled(et_businessName, true);
        setEditTextEnabled(et_businessEmail, true);
        setEditTextEnabled(et_businessDescription, true);
        addPhotoButton.setVisibility(View.GONE);
    }

    private void saveProfileEdits() {
        profileOptionButton.setText("Editar Perfil");
        saveBusinessInfo();
        editProfile.setVisibility(View.INVISIBLE);
        editCover.setVisibility(View.INVISIBLE);
        setEditTextEnabled(et_businessName, false);
        setEditTextEnabled(et_businessEmail, false);
        setEditTextEnabled(et_businessDescription, false);
    }

    private void readProfile() {
        if (currentUser == null) {
            Toast.makeText(ProfileActivity.this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUserId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(idUsuario != null ? idUsuario : currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (idUsuario == null || currentUserId.equals(idUsuario)) {
                                state = STATE_MY_PROFILE;
                                handleUserProfile(document);
                            } else {
                                handleSelectedUserProfile(document);
                                checkIfFriends(currentUserId, idUsuario);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "No se encontró documento para el usuario actual", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error obteniendo documento", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkIfFriends(String currentUserId, String idUsuario) {
        db.collection("friends").document(currentUserId)
                .collection("myFriends").document(idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        state = STATE_FRIEND;
                        switcherState();
                    } else {
                        checkFriendRequests(currentUserId, idUsuario);
                    }
                });
    }

    private void handleUserProfile(DocumentSnapshot document) {
        user duser = document.toObject(user.class);
        if (duser == null) return;

        updateProfileUI(document);
        updatePhotoUris(document);
        loadBusinessInfo(duser);

    }

    private void handleSelectedUserProfile(DocumentSnapshot document) {
        user duser = document.toObject(user.class);
        if (duser == null) return;

        updateProfileUI(document);
        updatePhotoUris(document);
        loadBusinessInfo(duser);

    }

    private void updateProfileUI(DocumentSnapshot document) {
        String toolbarTitle = document.getString("displayName");
        String ProfileImage = document.getString("photoUrl");
        String CoverImage = document.getString("coverUrl");
        String businessName = document.getString("businessName");
        String businessDescription = document.getString("businessDescription");
        String businessEmail = document.getString("businessEmail");

        if (toolbarTitle != null && !toolbarTitle.isEmpty()) {
            String firstName = toolbarTitle.split(" ")[0];
            collapsingToolbarLayout.setTitle(firstName);
        }
        if (ProfileImage != null) {
            Glide.with(ProfileActivity.this).load(ProfileImage).into(profileImage);
        }
        if (CoverImage != null) {
            Glide.with(ProfileActivity.this).load(CoverImage).into(coverImage);
        }
        et_businessName.setText(businessName);
        et_businessDescription.setText(businessDescription);
        et_businessEmail.setText(businessEmail);
    }

    private void updatePhotoUris(DocumentSnapshot document) {
        List<String> photoUrlsFromDb = (List<String>) document.get("imageUris");
        if (photoUrlsFromDb == null) return;

        photoUris.clear();
        for (String url : photoUrlsFromDb) {
            photoUris.add(Uri.parse(url));
        }
        photoAdapter.notifyDataSetChanged();
    }

    private void checkFriendRequests(String currentUserId, String idUsuario) {
        db.collection("friendRequests").document(currentUserId)
                .collection("sentRequests").document(idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        state = STATE_REQUEST_SENT;
                        switcherState();
                    } else {
                        checkReceivedFriendRequests(currentUserId, idUsuario);
                    }
                });
    }

    private void checkReceivedFriendRequests(String currentUserId, String idUsuario) {
        db.collection("friendRequests").document(currentUserId)
                .collection("receivedRequests").document(idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        state = STATE_REQUEST_RECEIVED;
                    } else {
                        state = STATE_NOT_FRIEND;
                    }
                    switcherState();
                });
    }



    private void sendFriendRequest(String idUsuario) {
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("from", fuser.getUid());
        friendRequest.put("to", idUsuario);
        friendRequest.put("timestamp", FieldValue.serverTimestamp());

        db.collection("friendRequests").document(fuser.getUid())
                .collection("sentRequests").document(idUsuario)
                .set(friendRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("friendRequests").document(idUsuario)
                                .collection("receivedRequests").document(fuser.getUid())
                                .set(friendRequest)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        state = STATE_REQUEST_SENT;
                                        switcherState();
                                        Toast.makeText(this, "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "Error al enviar la solicitud de amistad: ", task2.getException());
                                        Toast.makeText(this, "Error al enviar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d(TAG, "Error al enviar la solicitud de amistad: ", task.getException());
                        Toast.makeText(this, "Error al enviar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptFriendRequest() {
        db.collection("friendRequests").document(fuser.getUid())
                .collection("receivedRequests").document(idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            deleteReceivedRequest();
                        } else {
                            Log.d(TAG, "Documento de solicitud de amistad no encontrado");
                            Toast.makeText(this, "Error al aceptar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Error al acceder al documento de la solicitud recibida: ", task.getException());
                        Toast.makeText(this, "Error al aceptar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteReceivedRequest() {
        db.collection("friendRequests").document(fuser.getUid())
                .collection("receivedRequests").document(idUsuario)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createFriendship();
                    } else {
                        Log.d(TAG, "Error al eliminar la solicitud de la colección de recibidas: ", task.getException());
                        Toast.makeText(this, "Error al aceptar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createFriendship() {
        Map<String, Object> friendship = new HashMap<>();
        friendship.put("timestamp", FieldValue.serverTimestamp());

        db.collection("friends").document(fuser.getUid())
                .collection("myFriends").document(idUsuario)
                .set(friendship)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("friends").document(idUsuario)
                                .collection("myFriends").document(fuser.getUid())
                                .set(friendship)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        state = STATE_FRIEND;
                                        switcherState();
                                        Toast.makeText(this, "Ahora son amigos", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "Error al agregar a fuser como amigo del otro usuario: ", task2.getException());
                                        Toast.makeText(this, "Error al aceptar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d(TAG, "Error al agregar al otro usuario como amigo de fuser: ", task.getException());
                        Toast.makeText(this, "Error al aceptar la solicitud de amistad", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void editarClick(View view) {
        if (profileOptionButton.getText().toString().equals("Editar Perfil")) {
            enableProfileEditing();
        } else {
            saveProfileEdits();
        }
    }
}