package ru.coffeeplanter.translator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;
import java.util.TreeMap;

import ru.coffeeplanter.translator.database.TranslatorBaseHelper;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.LanguagesTable;

/**
 * Класс-синглтон для взаимодействия с базой данных.
 */

public class TranslatorLab {

    @SuppressLint("StaticFieldLeak")
    private static TranslatorLab sTranslatorLab;

    private Context mContext;
    private SQLiteDatabase mDataBase;

    public static TranslatorLab get(Context context) {
        if (sTranslatorLab == null) {
            sTranslatorLab = new TranslatorLab(context);
        }
        return sTranslatorLab;
    }

    private TranslatorLab(Context context) {
        mContext = context.getApplicationContext();
        mDataBase = new TranslatorBaseHelper(context).getWritableDatabase();
    }

    public void updateLanguagesList(Map<String, String> langs) {
        mDataBase.delete(LanguagesTable.NAME, "1", null);
        for (Map.Entry<String, String> lang : langs.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(LanguagesTable.Cols.LANGUAGE_CODE, lang.getValue());
            values.put(LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE, lang.getKey());
            mDataBase.insert(LanguagesTable.NAME, null, values);
        }
    }

    public Map<String, String> getLanguages() {
        Map<String, String> langs = new TreeMap<>();
        Cursor cursor = mDataBase.query(LanguagesTable.NAME, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            langs.put(cursor.getString(cursor.getColumnIndex(LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE)),
                    cursor.getString(cursor.getColumnIndex(LanguagesTable.Cols.LANGUAGE_CODE))
            );
            cursor.moveToNext();
        }
        cursor.close();
        return langs;
    }

}
