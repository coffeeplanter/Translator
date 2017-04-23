package ru.coffeeplanter.translator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import ru.coffeeplanter.translator.database.TranslationCardCursorWrapper;
import ru.coffeeplanter.translator.database.TranslatorBaseHelper;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.LanguagesTable;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.TranslationCardsTable;

/**
 * Класс-синглтон для взаимодействия с базой данных.
 */

public class TranslatorLab {

    private final String TAG = "TranslatorLab";

    private final long MAX_HISTORY_LIMIT = 100; // Максимальное количество хранимых карточек в истории переводов
    private final long MAX_BOOKMARKS_LIMIT = 50; // Максимальное количество избранных карточек

    @SuppressLint("StaticFieldLeak")
    // Утечки быть не должно, т. к. использую getApplicationContext.
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

    // Сохранение нового списка языков в БД
    public void updateLanguagesList(Map<String, String> langs) {
        mDataBase.delete(LanguagesTable.NAME, "1", null);
        for (Map.Entry<String, String> lang : langs.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(LanguagesTable.Cols.LANGUAGE_CODE, lang.getValue());
            values.put(LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE, lang.getKey());
            mDataBase.insert(LanguagesTable.NAME, null, values);
        }
    }

    // Чтение списка языков из БД
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

    // Добавление новой карточки перевода в БД.
    public void addTranslationCard(TranslationCard translationCard) {
        ContentValues values = getContentValues(translationCard);
        long numEntries = DatabaseUtils.queryNumEntries(mDataBase, TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"0"});
        if (numEntries < MAX_HISTORY_LIMIT) {
            mDataBase.insert(TranslationCardsTable.NAME, null, values);
        } else {
            Long numEntriesToDelete = numEntries - MAX_HISTORY_LIMIT + 1;
            mDataBase.beginTransaction();
            try {
                for (int i = 0; i < numEntriesToDelete.intValue(); i++) {
                    mDataBase.execSQL("DELETE FROM " + TranslationCardsTable.NAME +
                            " WHERE " + TranslationCardsTable.Cols.REQUEST_DATE +
                            " = (SELECT MIN(" + TranslationCardsTable.Cols.REQUEST_DATE +
                            ") FROM " + TranslationCardsTable.NAME +
                            " WHERE " + TranslationCardsTable.Cols.BOOKMARKED +
                            " = 0" + ");"
                    );
                }
                mDataBase.insert(TranslationCardsTable.NAME, null, values);
                mDataBase.setTransactionSuccessful();
            } finally {
                mDataBase.endTransaction();
            }
        }
    }

    // Обновление существующей карточки перевода в БД.
    public boolean updateTranslationCard(TranslationCard translationCard) {
        String uuidString = translationCard.getId().toString();
        ContentValues values = getContentValues(translationCard);
        long numEntries = DatabaseUtils.queryNumEntries(mDataBase, TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"1"});
        if (((numEntries < MAX_BOOKMARKS_LIMIT) && (translationCard.isBookmarked())) || (!translationCard.isBookmarked())) {
            mDataBase.update(TranslationCardsTable.NAME,
                    values,
                    TranslationCardsTable.Cols.UUID + " = ?",
                    new String[]{uuidString}
            );
            return true; // В случае успешного обновления
        } else {
            return false; // В случае достижения лимита на количество строк в БД
        }
    }

    // Удаление карточки перевода из БД.
    public void deleteTranslationCard(TranslationCard translationCard) {
        String uuidString = translationCard.getId().toString();
        mDataBase.delete(TranslationCardsTable.NAME,
                TranslationCardsTable.Cols.UUID + " = ?",
                new String[]{uuidString}
        );

    }

    // Удаление всех карточек перевода из БД, в зависимости от того, избранные они или нет.
    public void deleteTranslationCards(boolean bookmarks) {
        if (bookmarks) { // Удаляем все избранные карточки.
            Long numEntries = DatabaseUtils.queryNumEntries(mDataBase, TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"1"});
            mDataBase.delete(TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"1"});
        } else { // Удаляем все карточки без отметки "Избранная".
            Long numEntries = DatabaseUtils.queryNumEntries(mDataBase, TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"0"});
            mDataBase.delete(TranslationCardsTable.NAME, TranslationCardsTable.Cols.BOOKMARKED + " = ?", new String[]{"0"});
        }
    }

    // Чтение карточки перевода из БД по её id.
    public TranslationCard getTranslationCardById(UUID id) {
        TranslationCardCursorWrapper cursor = queryTranslationCards(
                TranslationCardsTable.Cols.UUID + " = ?",
                new String[]{id.toString()},
                null
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

    // Поиск и чтение карточки перевода по переводимому тексту и направлению перевода.
    public TranslationCard getTranslationCardByTextAndLangs(String textToTranslate, String fromLang, String toLang) {
        TranslationCardCursorWrapper cursor = queryTranslationCards(
                TranslationCardsTable.Cols.TEXT_TO_TRANSLATE + " = ? AND " +
                        TranslationCardsTable.Cols.FROM_LANGUAGE + " = ? AND " +
                        TranslationCardsTable.Cols.TO_LANGUAGE + " = ?",
                new String[]{textToTranslate, fromLang, toLang},
                null
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

    // Чтение всего списка карточек перевода из БД.
    public List<TranslationCard> getAllTranslationCards() {
        List<TranslationCard> cards = new ArrayList<>();
        TranslationCardCursorWrapper cursor = queryTranslationCards(null, null, TranslationCardsTable.Cols.REQUEST_DATE + " ASC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cards.add(0, cursor.getTranslationCard());
            cursor.moveToNext();
        }
        cursor.close();
        return cards;
    }

    // Чтение списка избранных карточек из БД.
    public List<TranslationCard> getBookmarkedTranslationCards() {
        List<TranslationCard> cards = new ArrayList<>();
        TranslationCardCursorWrapper cursor = queryTranslationCards(
                TranslationCardsTable.Cols.BOOKMARKED + " = ?",
                new String[]{"1"},
                TranslationCardsTable.Cols.REQUEST_DATE + " ASC"
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cards.add(0, cursor.getTranslationCard());
            cursor.moveToNext();
        }
        cursor.close();
        return cards;
    }

    // Подготовка объекта TranslationCard для записи в БД.
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

    // Запрос на чтение из БД с обёрткой дял удобного преобразования в объект TranslationCard.
    private TranslationCardCursorWrapper queryTranslationCards(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDataBase.query(
                TranslationCardsTable.NAME,
                null, // Columns - null выбирает все столбцы
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                orderBy // orderBy
        );
        return new TranslationCardCursorWrapper(cursor);
    }

}
