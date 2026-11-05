package com.uth.exa1pa1199.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class FechaUtils {

    private FechaUtils() { }

    public static String formatear(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        Date fecha = parsear(iso);
        if (fecha == null) return iso;
        SimpleDateFormat salida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return salida.format(fecha);
    }

    private static Date parsear(String iso) {
        String s = iso.replaceFirst("\\.\\d+", "");
        boolean tieneZona = s.endsWith("Z") || s.matches(".*[+-]\\d{2}:\\d{2}$");
        try {
            if (tieneZona) {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(s);
            }
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
}
