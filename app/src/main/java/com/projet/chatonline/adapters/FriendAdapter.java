package com.projet.chatonline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projet.chatonline.R;
import com.projet.chatonline.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private Context context;
    private List<User> friends;
    private List<String> selectedUids = new ArrayList<>();

    public FriendAdapter(Context context, List<User> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = friends.get(position);
        holder.tvName.setText(user.getDisplayName());

        Glide.with(context)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(holder.ivAvatar);

        // ⚡ Déconnecter temporairement le listener avant de changer l’état
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUids.contains(user.getUid()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUids.contains(user.getUid()))
                    selectedUids.add(user.getUid());
            } else {
                selectedUids.remove(user.getUid());
            }
        });


        holder.itemView.setOnClickListener(v -> {
            boolean newState = !holder.checkBox.isChecked();
            holder.checkBox.setChecked(newState);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public List<String> getSelectedUids() {
        return selectedUids;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_friend_avatar);
            tvName = itemView.findViewById(R.id.tv_friend_name);
            checkBox = itemView.findViewById(R.id.cb_select_friend);
        }
    }
}
