package com.projet.chatonline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projet.chatonline.R;
import com.projet.chatonline.models.*;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnUserClick { void onUserClick(User user); }

    private Context ctx;
    private List<User> list;
    private OnUserClick listener;

    public UserAdapter(Context ctx, List<User> list, OnUserClick listener) {
        this.ctx = ctx; this.list = list; this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        User u = list.get(position);
        holder.name.setText(u.getDisplayName());
        if (u.getAvatarUrl()!=null && !u.getAvatarUrl().isEmpty()) {
            Glide.with(ctx).load(u.getAvatarUrl()).into(holder.avatar);
        } else holder.avatar.setImageResource(R.drawable.ic_user_placeholder);
        holder.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CircleImageView avatar; TextView name;
        VH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.item_user_avatar);
            name = itemView.findViewById(R.id.item_user_name);
        }
    }
}
