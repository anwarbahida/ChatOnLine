package com.projet.chatonline.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.projet.chatonline.R;
import com.projet.chatonline.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private Context ctx;
    private List<Message> messages;
    private String currentUid;

    public ChatAdapter(Context ctx, List<Message> messages, String currentUid) {
        this.ctx = ctx;
        this.messages = messages;
        this.currentUid = currentUid;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Message m = messages.get(position);
        boolean isSent = m.getSenderId().equals(currentUid);

        // Formatage de l'heure
        String formattedTime = formatMessageTime(m.getTimestamp());

        // Gestion des conteneurs (gauche/droite)
        holder.leftContainer.setVisibility(isSent ? View.GONE : View.VISIBLE);
        holder.rightContainer.setVisibility(isSent ? View.VISIBLE : View.GONE);

        if (isSent) {
            bindSentMessage(holder, m, formattedTime);
        } else {
            bindReceivedMessage(holder, m, formattedTime);
        }
    }

    private String formatMessageTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        try {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private void bindSentMessage(VH holder, Message message, String formattedTime) {
        holder.txtSent.setVisibility(View.VISIBLE);
        holder.txtSent.setText(message.getText());
        holder.timeSent.setText(formattedTime);

        holder.rightContainer.setOnLongClickListener(v -> {
            showMessageOptions(message);
            return true;
        });
    }

    private void bindReceivedMessage(VH holder, Message message, String formattedTime) {
        holder.txtReceived.setVisibility(View.VISIBLE);
        holder.txtReceived.setText(message.getText());
        holder.timeReceived.setText(formattedTime);
    }

    private void showMessageOptions(Message m) {
        String[] options = {"Modifier", "Supprimer"};

        new AlertDialog.Builder(ctx)
                .setTitle("Action sur le message")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showEditDialog(m);
                    else if (which == 1) deleteMessage(m);
                })
                .show();
    }

    private void showEditDialog(Message m) {
        EditText input = new EditText(ctx);
        input.setText(m.getText());

        new AlertDialog.Builder(ctx)
                .setTitle("Modifier le message")
                .setView(input)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String newText = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newText)) {
                        updateMessageText(m, newText);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void updateMessageText(Message message, String newText) {
        FirebaseFirestore.getInstance()
                .collection("chats")
                .document(message.getChatId())
                .collection("messages")
                .document(message.getId())
                .update("text", newText)
                .addOnSuccessListener(aVoid -> {
                    message.setText(newText);
                    notifyItemChanged(messages.indexOf(message));
                    Toast.makeText(ctx, "Message modifié", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ctx, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMessage(Message m) {
        new AlertDialog.Builder(ctx)
                .setTitle("Supprimer ce message ?")
                .setMessage("Voulez-vous vraiment supprimer ce message ?")
                .setPositiveButton("Oui", (d, w) -> {
                    FirebaseFirestore.getInstance()
                            .collection("chats")
                            .document(m.getChatId())
                            .collection("messages")
                            .document(m.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                messages.remove(m);
                                notifyDataSetChanged();
                                Toast.makeText(ctx, "Message supprimé", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ctx, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // VIEW HOLDER
    static class VH extends RecyclerView.ViewHolder {
        View leftContainer, rightContainer;
        TextView txtReceived, txtSent;
        TextView timeReceived, timeSent;

        VH(View v) {
            super(v);

            leftContainer = v.findViewById(R.id.left_message_container);
            rightContainer = v.findViewById(R.id.right_message_container);

            txtReceived = v.findViewById(R.id.chat_message_text);
            txtSent = v.findViewById(R.id.chat_message_text_sent);

            timeReceived = v.findViewById(R.id.chat_message_time);
            timeSent = v.findViewById(R.id.chat_message_time_sent);
        }
    }

    // Utils
    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
}
