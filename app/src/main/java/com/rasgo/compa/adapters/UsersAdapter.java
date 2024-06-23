package com.rasgo.compa.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.profile.ProfileActivity;
import com.rasgo.compa.model.user.user;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private ArrayList<user> userList;
    private Context context;
    private View.OnClickListener onClickListener;

    public UsersAdapter(Context context, ArrayList<user> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user user = userList.get(position);
        holder.bind(user);

        holder.userName.setText(user.getDisplayName());
        holder.businessName.setText(user.getBusinessName());
        Glide.with(context).load(user.getPhotoUrl()).into(holder.userImage);
        Glide.with(context).load(user.getCoverUrl()).into(holder.coverImage);


        holder.itemView.setOnClickListener(View -> {
           if (firebaseUser != null) {
               Intent intent = new Intent(context,ProfileActivity.class);
               intent.putExtra("idUsuario", user.getUserId());
               intent.putExtra("name", user.getDisplayName());
               intent.putExtra("businessName", user.getBusinessName());
               intent.putExtra("descripcion", user.getBusinessDescription());
               intent.putExtra("photoUrl", user.getPhotoUrl());
               intent.putExtra("coverUrl",user.getCoverUrl());
               intent.putExtra("businessEmail", user.getBusinessEmail());
               Log.d(TAG, "Cover URL: " + user.getCoverUrl());

               context.startActivity(intent);
           } else {
               Toast.makeText(context, "Error al mostrar usuarios", Toast.LENGTH_SHORT).show();
           }
        });
    }

    private int getState(user user, FirebaseUser firebaseUser) {
        int state;
        if (user.getUserId().equals(firebaseUser.getUid())){
            state=5;//nuestro perfil
        }else{
            if (isFriend(user.getUserId())) {
                state = 1; // Usuario agregado como amigo
            } else if (hasSentFriendRequest(user.getUserId())) {
                state = 2; // Hemos enviado solicitud de amistad
            } else if (hasReceivedFriendRequest(user.getUserId())) {
                state = 3; // Hemos recibido solicitud de amistad
            } else {
                state = 4; // Usuario que no es amigo
            }
        }
        return state;
    }

    private boolean hasReceivedFriendRequest(String userId) {
        return false;
    }

    private boolean hasSentFriendRequest(String userId) {
        return false;
    }

    private boolean isFriend(String userId) {
        return false;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView businessName;
        public ImageView userImage;
        public ImageView coverImage;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            businessName=itemView.findViewById(R.id.business_name);
            userImage = itemView.findViewById(R.id.photo);
            coverImage = itemView.findViewById(R.id.imageCover);
        }

        public void bind(user user) {
            userName.setText(user.getDisplayName());
            Glide.with(context).load(user.getPhotoUrl()).into(userImage);
            Glide.with(context).load(user.getCoverUrl()).into(coverImage);

        }
    }
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.onClick(v);
        }
    }
}

