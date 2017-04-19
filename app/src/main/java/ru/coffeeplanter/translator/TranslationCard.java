package ru.coffeeplanter.translator;

/**
 * Модель карточки перевода для списков истории и закладок
 */

public class TranslationCard {

    private String mTextToTranslate;
    private String mTranslatedText;
    private String mTranslationDirection;
    private boolean mBookmarked;

    public TranslationCard(String textToTranslate, String translatedText, boolean bookmarked) {
        mTextToTranslate = textToTranslate;
        mTranslatedText = translatedText;
        mTranslationDirection = "En→Ru";
        mBookmarked = bookmarked;
    }

    public String getTextToTranslate() {
        return mTextToTranslate;
    }

    public void setTextToTranslate(String textToTranslate) {
        mTextToTranslate = textToTranslate;
    }

    public String getTranslatedText() {
        return mTranslatedText;
    }

    public void setTranslatedText(String translatedText) {
        mTranslatedText = translatedText;
    }

    public String getTranslationDirection() {
        return mTranslationDirection;
    }

    public void setTranslationDirection(String translationDirection) {
        mTranslationDirection = translationDirection;
    }

    public boolean isBookmarked() {
        return mBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        mBookmarked = bookmarked;
    }

}
