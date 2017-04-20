package ru.coffeeplanter.translator.utils;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Класс для определения текущей локали.
 */

public class LocaleUtils {

    /**
     * Определяет код языка системы, исполняющей приложение.
     * @param context контекст, для которого определяется текущий язык.
     */
    @SuppressWarnings("deprecation")
    public static String getCurrentLanguage(Context context) {

        Locale currentLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale =  context.getResources().getConfiguration().getLocales().get(0);
        } else {
            currentLocale = context.getResources().getConfiguration().locale;
        }
        return currentLocale.getLanguage();
    }

}
