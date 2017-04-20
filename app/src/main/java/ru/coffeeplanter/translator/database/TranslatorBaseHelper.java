package ru.coffeeplanter.translator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.coffeeplanter.translator.database.TranslatorDbSchema.LanguagesTable;

/**
 * Класс для инициализации базы данных.
 */

public class TranslatorBaseHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String DATABASE_NAME = "translatorBase.db";

    private String[][] initial_languages;

    public TranslatorBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        initial_languages = new String[][]{
                new String[]{"'ar'", "'Arabic'"},
                new String[]{"'zh'", "'Chinese'"},
                new String[]{"'en'", "'English'"},
                new String[]{"'fr'", "'French'"},
                new String[]{"'de'", "'German'"},
                new String[]{"'ru'", "'Russian'"},
                new String[]{"'es'", "'Spanish'"},
                new String[]{"'tr'", "'Turkish'"}
        };
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Создание базы данных поддерживаемых языков
        db.execSQL("CREATE TABLE IF NOT EXIST " + LanguagesTable.NAME + "(" +
                "_id integer PRIMARY KEY AUTOINCREMENT, " +
                LanguagesTable.Cols.LANGUAGE_CODE + " text NOT NULL UNIQUE, " +
                LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE + " text NOT NULL UNIQUE" +
                ")"
        );

        // Её наполнение начальными данными (которые потом будут перезаписаны данными с сервера)
        for (String[] langs : initial_languages) {
            db.execSQL("INSERT INTO " + LanguagesTable.NAME + " VALUES (" +
                    "null, " + langs[0] + ", " + langs[1] + ")"
            );
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
