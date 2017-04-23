package ru.coffeeplanter.translator.database;

/**
 * Структура базы данных приложения.
 */

public class TranslatorDbSchema {

    // Таблица поддерживаемых языков.
    public static final class LanguagesTable {

        public static final String NAME = "languages";

        public static final class Cols {
            public static final String LANGUAGE_CODE = "lang_code";
            public static final String DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE = "display_language";
        }

    }

    // Таблица карточек перевода.
    public static final class TranslationCardsTable {

        public static final String NAME = "translation_cards";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TEXT_TO_TRANSLATE = "text_to_translate";
            public static final String TRANSLATED_TEXT = "translated_text";
            public static final String FROM_LANGUAGE = "from_language";
            public static final String TO_LANGUAGE = "to_language";
            public static final String BOOKMARKED = "bookmarked";
            public static final String REQUEST_DATE = "request_date";
        }

    }

}
