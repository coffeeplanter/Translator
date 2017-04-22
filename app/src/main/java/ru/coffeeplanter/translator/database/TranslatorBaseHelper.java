package ru.coffeeplanter.translator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.coffeeplanter.translator.database.TranslatorDbSchema.LanguagesTable;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.TranslationCardsTable;

/**
 * Класс для инициализации базы данных.
 */

public class TranslatorBaseHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String DATABASE_NAME = "translatorBase.db";

    private final String TAG = "TranslatorBaseHelper";

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

        // Создание базы данных поддерживаемых языков.
        db.execSQL("CREATE TABLE " + LanguagesTable.NAME + "(" +
                "_id integer UNIQUE, " +
                LanguagesTable.Cols.LANGUAGE_CODE + " text PRIMARY KEY, " +
                LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE + " text" +
                ");"
        );

        // Её наполнение начальными данными (которые потом будут перезаписаны данными с сервера).
        int i = 0;
        for (String[] langs : initial_languages) {
            db.execSQL("INSERT INTO " + LanguagesTable.NAME + " VALUES (" +
                    + ++i + ", " + langs[0] + ", " + langs[1] + ")"
            );
        }

        // Создаём базу данных карточек перевода.
        db.execSQL("CREATE TABLE " + TranslationCardsTable.NAME + "(" +
                "_id integer PRIMARY KEY AUTOINCREMENT, " +
                TranslationCardsTable.Cols.UUID + " text, " +
                TranslationCardsTable.Cols.TEXT_TO_TRANSLATE + " text, " +
                TranslationCardsTable.Cols.TRANSLATED_TEXT + " text, " +
                TranslationCardsTable.Cols.FROM_LANGUAGE + " text, " +
                TranslationCardsTable.Cols.TO_LANGUAGE + " text, " +
                TranslationCardsTable.Cols.BOOKMARKED + " integer, " +
                TranslationCardsTable.Cols.REQUEST_DATE + " integer, " +
                "FOREIGN KEY (" + TranslationCardsTable.Cols.FROM_LANGUAGE + ") " +
                "REFERENCES " + LanguagesTable.NAME + " (" + LanguagesTable.Cols.LANGUAGE_CODE +"), " +
                "FOREIGN KEY (" + TranslationCardsTable.Cols.TO_LANGUAGE + ") " +
                "REFERENCES " + LanguagesTable.NAME + " (" + LanguagesTable.Cols.LANGUAGE_CODE +")" +
                ");" +
                "CREATE UNIQUE INDEX idx_translation_cards_text_to_translate ON " +
                TranslationCardsTable.NAME + " (" + TranslationCardsTable.Cols.TEXT_TO_TRANSLATE + ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
