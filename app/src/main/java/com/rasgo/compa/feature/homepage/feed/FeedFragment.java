package com.rasgo.compa.feature.homepage.feed;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rasgo.compa.R;
import com.rasgo.compa.adapters.UsersAdapter;
import com.rasgo.compa.feature.auth.LoginActivity;
import com.rasgo.compa.feature.homepage.MainActivity;
import com.rasgo.compa.model.user.user;

import java.util.ArrayList;

import androidx.appcompat.widget.SearchView;


public class FeedFragment extends Fragment {

    private FirebaseFirestore db;
    private UsersAdapter mUsersAdapter;
    private RecyclerView recyclerView;
    private ArrayList<user> userList = new ArrayList<>();

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerView = view.findViewById(R.id.recView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Configurar el RecyclerView con 2 columnas
        mUsersAdapter = new UsersAdapter(requireContext(), userList);
        recyclerView.setAdapter(mUsersAdapter);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

                // Cargar productos en productList y notificar al adaptador
        ListUsers();

        SearchView svSearch = view.findViewById(R.id.searchEditText);
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUser(newText);
                return true;
            }
        });
        return view;
    }

    private void ListUsers(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser==null){
            Log.d(TAG, "Usuario no autenticado");
            return;
        }
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            user userAux = document.toObject(user.class);
                            if (!userAux.getUserId().equals(currentUser.getUid())){
                                userList.add(userAux);
                            }
                        }
                        mUsersAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error obteniendo documentos: ", task.getException());
                    }
                });
    }
    private void searchUser(String newText) {
        ArrayList<user> filterUsers = new ArrayList<>();
        for (user obj : userList) {
            if (obj.getDisplayName().toLowerCase().contains(newText.toLowerCase()))
            {
                filterUsers.add(obj);
            }
        }
        UsersAdapter adapterUser = new UsersAdapter(requireContext(), filterUsers);
        recyclerView.setAdapter(adapterUser);
    }
}