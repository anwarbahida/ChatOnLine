package com.projet.chatonline.utils;

import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.projet.chatonline.models.*;
import com.projet.chatonline.utils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class FirebaseUtils {

    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public static String getUid() {
        if (auth.getCurrentUser() != null) return auth.getCurrentUser().getUid();
        return null;
    }

    public static Task<Void> saveUserProfile(User user) {
        if (user.getUid() == null) return null;
        return db.collection(Constants.USERS_COLLECTION)
                .document(user.getUid())
                .set(user, SetOptions.merge());
    }


    public static void uploadImage(Uri uri, String path, OnCompleteListener<Uri> listener) {
        StorageReference ref = storageRef.child(path + "/" + UUID.randomUUID().toString() + ".jpg");
        ref.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        }).addOnCompleteListener(listener);
    }

    public static void updateFcmToken(String uid, String token) {
        if (uid == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.FIELD_FCM_TOKEN, token);
        db.collection(Constants.USERS_COLLECTION).document(uid).update(map);
    }

}