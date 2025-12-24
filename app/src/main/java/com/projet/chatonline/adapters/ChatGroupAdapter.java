package com.projet.chatonline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.chatonline.R;
import com.projet.chatonline.models.Message;

import java.util.List;

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.ViewHolder> {

    private final Context context;
    private final List<Message> messages;
    private final String currentUid;

    public ChatGroupAdapter(Context context, List<Message> messages, String currentUid) {
        this.context = context;
        this.messages = messages;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);

        // Texte et expéditeur sécurisés
        holder.tvMessage.setText(message.getText() != null ? message.getText() : "");
        holder.tvSender.setText(message.getSenderId() != null ? message.getSenderId() : "Utilisateur inconnu");

        // Alignement et style selon expéditeur
        if (message.getSenderId() != null && message.getSenderId().equals(currentUid)) {
            holder.container.setGravity(android.view.Gravity.END);
            holder.tvMessage.setBackgroundResource(R.drawable.bg_right_message);
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.container.setGravity(android.view.Gravity.START);
            holder.tvMessage.setBackgroundResource(R.drawable.bg_left_message);
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView tvMessage, tvSender;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.message_container);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
            tvSender = itemView.findViewById(R.id.tv_message_sender);
        }
    }
}
