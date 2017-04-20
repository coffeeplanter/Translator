package ru.coffeeplanter.translator.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Map;

import ru.coffeeplanter.translator.database.TranslatorDbSchema.LanguagesTable;

/**
 * Обёртка для класса Cursor для чтения списка языков.
 */
// TODO: Класс к удалению

public class LanguagesCursorWrapper extends CursorWrapper {

    public LanguagesCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Map<String, String> getLanguages() {
        String displayLanguage = getString(getColumnIndex(LanguagesTable.Cols.DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE));
        String languageCode = getString(getColumnIndex(LanguagesTable.Cols.LANGUAGE_CODE));
        return null;
    }

}
