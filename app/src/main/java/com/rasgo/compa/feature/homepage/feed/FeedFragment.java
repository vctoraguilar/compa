package com.rasgo.compa.feature.homepage.feed;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.auth.LoginActivity;
import com.rasgo.compa.feature.homepage.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class FeedFragment extends Fragment {

    private FirebaseFirestore db;
    private GridLayout misCompasGrid;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button btnLogout;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }




    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feed, container,false);

        misCompasGrid = root.findViewById(R.id.misCompas);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Obtener datos del usuario
                            String userName = document.getString("displayName");
                            String userPhotoUrl = document.getString("photoUrl");

                            // Crear y configurar CardView
                            CardView cardView = createCardView(userName, userPhotoUrl);
                            misCompasGrid.addView(cardView);
                        }
                    } else {
                        Log.d(TAG, "Error obteniendo documentos: ", task.getException());
                    }
                });

        return root;

    }

    private CardView createCardView(String userName, String userPhotoUrl) {
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(layoutParams);
        cardView.setCardElevation(8); // Elevación de la tarjeta
        cardView.setRadius(8); // Radio de los bordes de la tarjeta

        // Layout interno de la tarjeta (LinearLayout vertical)
        LinearLayout innerLayout = new LinearLayout(requireContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        innerLayout.setPadding(16, 16, 16, 16);

        // ImageView para la foto del usuario
        ImageView imageView = new ImageView(requireContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Cargar imagen usando Glide (asegúrate de agregar la dependencia de Glide en build.gradle)
        Glide.with(this)
                .load(userPhotoUrl)
//                .placeholder(R.drawable.placeholder) // Placeholder si la imagen tarda en cargar
//                .error(R.drawable.error_image) // Imagen de error si no se puede cargar la imagen
                .into(imageView);

        // TextView para el nombre del usuario
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(userName);
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);

        // Agregar imageView y textView al innerLayout
        innerLayout.addView(imageView);
        innerLayout.addView(textView);

        // Agregar innerLayout al cardView
        cardView.addView(innerLayout);

        return cardView;
    }
}