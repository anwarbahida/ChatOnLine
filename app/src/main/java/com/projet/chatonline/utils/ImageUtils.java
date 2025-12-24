package com.projet.chatonline.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static byte[] compressImage(Uri imageUri, Context context) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(imageUri);
        if (is == null) return null;

        // Étape 1: Obtenir les dimensions sans charger en mémoire
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();

        // Étape 2: Calculer le ratio de redimensionnement
        options.inSampleSize = calculateInSampleSize(options, 800, 800);

        // Étape 3: Charger l'image avec le bon ratio
        is = context.getContentResolver().openInputStream(imageUri);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

        if (bitmap == null) {
            is.close();
            return null;
        }

        // Étape 4: Compresser en JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] compressedData = baos.toByteArray();

        is.close();
        bitmap.recycle();

        return compressedData;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}