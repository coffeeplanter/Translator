package ru.coffeeplanter.translator.database;

/**
 * Структура базы данных приложения.
 */

public class TranslatorDbSchema {

    public static final class LanguagesTable {

        public static final String NAME = "languages";

        public static final class Cols {
            public static final String LANGUAGE_CODE = "lang_code";
            public static final String DISPLAY_LANGUAGE_FOR_CURRENT_LOCALE = "display_language";
        }

    }

}
