package ru.coffeeplanter.translator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.coffeeplanter.translator.database.TranslationCardCursorWrapper;
import ru.coffeeplanter.translator.database.TranslatorBaseHelper;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.*;

/**
 * Класс-синглтон для взаимодействия с базой данных.
 */

public class TranslatorLab {

    private final String TAG = "TranslatorLab";

    private final long MAX_HISTORY_LIMIT = 10;
    private final long MAX_BOOKMARKS_LIMIT = 5;

    @SuppressLint("StaticFieldLeak") // Утечки быть не должно, т. к. использую getApplicationContext.
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

    public void addTranslationCard(TranslationCard translationCard) {


        ContentValues values = getContentValues(translationCard);
        long numEntries = DatabaseUtils.queryNumEntries(mDataBase, TranslationCardsTable.NAME);
        Log.d(TAG, "Rows num: " + numEntries);
        if (numEntries < MAX_HISTORY_LIMIT) {
//            mDataBase.insert(TranslationCardsTable.NAME, null, values);
            mDataBase.replace(TranslationCardsTable.NAME, null, values);
            Log.d(TAG, "numEntries < MAX_HISTORY_LIMIT");
        } else {
            Long numEntriesToDelete = numEntries - MAX_HISTORY_LIMIT + 1;
            mDataBase.beginTransaction();
            try {
                for (int i = 0; i < numEntriesToDelete.intValue(); i++) {
//                    int num = mDataBase.delete(TranslationCardsTable.NAME, TranslationCardsTable.Cols.REQUEST_DATE + " = ?",
//                            new String[]{"(SELECT MIN(" + TranslationCardsTable.Cols.REQUEST_DATE + ") FROM " + TranslationCardsTable.NAME + ")"});
//                    Log.d(TAG, "entries deleted: " + num);
                    mDataBase.execSQL("DELETE FROM " + TranslationCardsTable.NAME +
                            " WHERE " + TranslationCardsTable.Cols.REQUEST_DATE +
                            " = (SELECT MIN(" + TranslationCardsTable.Cols.REQUEST_DATE +
                            ") FROM " + TranslationCardsTable.NAME + ");"
                    );
                }
                mDataBase.insert(TranslationCardsTable.NAME, null, values);
                mDataBase.setTransactionSuccessful();
            } finally {
                mDataBase.endTransaction();
            }
            Log.d(TAG, "numEntries > MAX_HISTORY_LIMIT");
        }

    }

    public void updateTranslationCard(TranslationCard translationCard) {
        String uuidString = translationCard.getId().toString();
        ContentValues values = getContentValues(translationCard);
        mDataBase.update(TranslationCardsTable.NAME,
                values,
                TranslationCardsTable.Cols.UUID + " = ?",
                new String[]{uuidString}
        );
    }


    public TranslationCard getTranslationCardByTextAndLangs(String textToTranslate, String fromLang, String toLang) {
        TranslationCardCursorWrapper cursor = queryCrimes(
                TranslationCardsTable.Cols.TEXT_TO_TRANSLATE + " = ? AND " +
                        TranslationCardsTable.Cols.FROM_LANGUAGE + " = ? AND " +
                        TranslationCardsTable.Cols.TO_LANGUAGE + " = ?",
                new String[]{textToTranslate, fromLang, toLang}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getTranslationCard();
        } finally {
            cursor.close();
        }

    }

    public List<TranslationCard> getTranslationCards() {
        List<TranslationCard> cards = new ArrayList<>();
        TranslationCardCursorWrapper cursor = queryCrimes(null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cards.add(0, cursor.getTranslationCard());
            cursor.moveToNext();
        }
        cursor.close();
        return cards;
    }

    private ContentValues getContentValues(TranslationCard translationCard) {
        ContentValues values = new ContentValues();
        values.put(TranslationCardsTable.Cols.UUID, translationCard.getId().toString());
        values.put(TranslationCardsTable.Cols.TEXT_TO_TRANSLATE, translationCard.getTextToTranslate());
        values.put(TranslationCardsTable.Cols.TRANSLATED_TEXT, translationCard.getTranslatedText());
        values.put(TranslationCardsTable.Cols.FROM_LANGUAGE, translationCard.getFromLanguage());
        values.put(TranslationCardsTable.Cols.TO_LANGUAGE, translationCard.getToLanguage());
        values.put(TranslationCardsTable.Cols.BOOKMARKED, translationCard.isBookmarked() ? 1 : 0);
        values.put(TranslationCardsTable.Cols.REQUEST_DATE, translationCard.getRequestDate().getTime());
        return values;
    }

    private TranslationCardCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                TranslationCardsTable.NAME,
                null, // Columns - null выбирает все столбцы
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new TranslationCardCursorWrapper(cursor);
    }

}
