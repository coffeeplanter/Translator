package ru.coffeeplanter.translator;

import java.util.Date;
import java.util.UUID;

/**
 * Модель карточки перевода.
 */

public class TranslationCard {

    private final String TAG = "TranslationsCard";

    private final String LANGUAGES_DIVIDER = "→";

    private UUID mId;
    private String mTextToTranslate;
    private String mTranslatedText;
    private String mFromLanguage;
    private String mToLanguage;
    private boolean mBookmarked;
    private Date mRequestDate;

    public TranslationCard() {
        this(UUID.randomUUID());
    }

    public TranslationCard(UUID id) {
        mId = id;
        mBookmarked = false;
        mRequestDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
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

    public String getFromLanguage() {
        return mFromLanguage;
    }

    public void setFromLanguage(String fromLanguage) {
        mFromLanguage = fromLanguage;
    }

    public String getToLanguage() {
        return mToLanguage;
    }

    public void setToLanguage(String toLanguage) {
        mToLanguage = toLanguage;
    }

    public String getTranslationDirection() {
        String fromBufffer = getFromLanguage();
        if (fromBufffer != null) {
            fromBufffer = getFromLanguage().substring(0, 1).toUpperCase() + getFromLanguage().substring(1);
        }
        String toBufffer = getToLanguage();
        if (toBufffer != null) {
            toBufffer = getToLanguage().substring(0, 1).toUpperCase() + getToLanguage().substring(1);
        }
        return fromBufffer + LANGUAGES_DIVIDER + toBufffer;
    }

    public boolean isBookmarked() {
        return mBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        mBookmarked = bookmarked;
    }

    public Date getRequestDate() {
        return mRequestDate;
    }

    public void setRequestDate(Date requestDate) {
        mRequestDate = requestDate;
    }

}
