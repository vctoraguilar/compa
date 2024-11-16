package com.rasgo.compa.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.profile.ProfileActivity;

import java.util.List;

public class BusinessInfoAdapter extends RecyclerView.Adapter<BusinessInfoAdapter.BusinessInfoViewHolder>{
    private String businessName;
    private String businessDescription;
    private boolean isEditable = false;

    public BusinessInfoAdapter() {
        // Constructor vacío
    }

    public BusinessInfoAdapter(String businessName, String businessDescription) {
        this.businessName = businessName;
        this.businessDescription = businessDescription;
    }

//    public BusinessInfoAdapter(ProfileActivity profileActivity, List<String> businessInfoList) {
//    }


    public void setBusinessInfo(String businessName, String businessDescription) {
        this.businessName = businessName;
        this.businessDescription = businessDescription;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BusinessInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_business_info, parent, false);
        return new BusinessInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessInfoViewHolder holder, int position) {
        // Bind the data to the ViewHolder
        holder.etBusinessName.setText(businessName);
        holder.etBusinessDescription.setText(businessDescription);
        holder.etBusinessName.setEnabled(isEditable);
        holder.etBusinessDescription.setEnabled(isEditable);
    }

    @Override
    public int getItemCount() {
        return 1; }

    public void setEditable(boolean editable) {
        isEditable = editable;
        notifyItemChanged(0);
    }

    static class BusinessInfoViewHolder extends RecyclerView.ViewHolder {
        EditText etBusinessName;
        EditText etBusinessDescription;

        public BusinessInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            etBusinessName = itemView.findViewById(R.id.et_business_name);
            etBusinessDescription = itemView.findViewById(R.id.et_business_description);
        }
    }
}
