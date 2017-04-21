package ru.coffeeplanter.translator.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import ru.coffeeplanter.translator.TranslationCard;
import ru.coffeeplanter.translator.database.TranslatorDbSchema.TranslationCardsTable;

/**
 * Обёртка для чтения данных из БД в объект TranslationCard.
 */

public class TranslationCardCursorWrapper extends CursorWrapper {

    public TranslationCardCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public TranslationCard getTranslationCard() {

        String uuidString = getString(getColumnIndex(TranslationCardsTable.Cols.UUID));
        String textToTranslate = getString(getColumnIndex(TranslationCardsTable.Cols.TEXT_TO_TRANSLATE));
        String translatedText = getString(getColumnIndex(TranslationCardsTable.Cols.TRANSLATED_TEXT));
        String fromLanguage = getString(getColumnIndex(TranslationCardsTable.Cols.FROM_LANGUAGE));
        String toLanguage = getString(getColumnIndex(TranslationCardsTable.Cols.TO_LANGUAGE));
        int isBookmarked = getInt(getColumnIndex(TranslationCardsTable.Cols.BOOKMARKED));
        long requestDate = getLong(getColumnIndex(TranslationCardsTable.Cols.REQUEST_DATE));

        TranslationCard translationCard = new TranslationCard(UUID.fromString(uuidString));
        translationCard.setTextToTranslate(textToTranslate);
        translationCard.setTranslatedText(translatedText);
        translationCard.setFromLanguage(fromLanguage);
        translationCard.setToLanguage(toLanguage);
        translationCard.setBookmarked(isBookmarked != 0);
        translationCard.setRequestDate(new Date(requestDate));

        return translationCard;

    }

}
