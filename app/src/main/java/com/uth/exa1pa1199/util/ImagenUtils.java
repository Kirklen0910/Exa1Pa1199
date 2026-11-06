package com.uth.exa1pa1199.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImagenUtils {

    public static final int MAX_LADO_PX = 1024;
    public static final int CALIDAD_JPEG = 70;
    public static final int MAX_BASE64_BYTES = 2 * 1024 * 1024;

    private ImagenUtils() { }

    public static final class FotoProcesada {
        public final Bitmap bitmap;
        public final String base64;

        FotoProcesada(Bitmap bitmap, String base64) {
            this.bitmap = bitmap;
            this.base64 = base64;
        }
    }

    public static FotoProcesada reducirYCodificar(ContentResolver resolver, Uri uri) throws IOException {
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, limites);
        }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            throw new IOException("No se pudo leer la imagen");
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = calcularInSampleSize(limites.outWidth, limites.outHeight, MAX_LADO_PX);
        Bitmap bitmap;
        try (InputStream in = resolver.openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opciones);
        }
        if (bitmap == null) throw new IOException("No se pudo decodificar la imagen");

        bitmap = escalarAMaximo(bitmap, MAX_LADO_PX);

        int grados = leerRotacion(resolver, uri);
        if (grados != 0) {
            Matrix m = new Matrix();
            m.postRotate(grados);
            Bitmap rotado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (rotado != bitmap) bitmap.recycle();
            bitmap = rotado;
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida);
        String base64 = Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP);
        if (base64.length() > MAX_BASE64_BYTES) {
            throw new IOException("La foto supera el límite de 2 MB de la API");
        }
        return new FotoProcesada(bitmap, base64);
    }

    public static Bitmap decodificarBase64(String base64) {
        if (base64 == null || base64.trim().isEmpty()) return null;
        String limpio = base64;
        int coma = limpio.indexOf(',');
        if (limpio.startsWith("data:") && coma >= 0) limpio = limpio.substring(coma + 1);
        try {
            byte[] bytes = Base64.decode(limpio, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int calcularInSampleSize(int ancho, int alto, int maxLado) {
        int muestra = 1;
        while (Math.max(ancho, alto) / (muestra * 2) >= maxLado) {
            muestra *= 2;
        }
        return muestra;
    }

    private static Bitmap escalarAMaximo(Bitmap original, int maxLado) {
        int ancho = original.getWidth();
        int alto = original.getHeight();
        int mayor = Math.max(ancho, alto);
        if (mayor <= maxLado) return original;
        float factor = (float) maxLado / mayor;
        Bitmap escalado = Bitmap.createScaledBitmap(original,
                Math.round(ancho * factor), Math.round(alto * factor), true);
        if (escalado != original) original.recycle();
        return escalado;
    }

    private static int leerRotacion(ContentResolver resolver, Uri uri) {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) return 0;
            ExifInterface exif = new ExifInterface(in);
            int orientacion = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientacion) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
