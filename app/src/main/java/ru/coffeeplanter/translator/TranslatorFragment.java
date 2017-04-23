package ru.coffeeplanter.translator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.coffeeplanter.translator.utils.LocaleUtils;

/**
 * Класс фрагмента интерфейса перевода, является основным экраном приложения.
 */

public class TranslatorFragment extends Fragment {

    private final String TAG = "TranslatorFragment";

    // Данные для работы с SharedPreferences.
    private final String APP_PREFERENCES = "translator_settings";
    private final String APP_PREFERENCES_FROM_LANGUAGE_SPINNER = "from_language";
    private final String APP_PREFERENCES_TO_LANGUAGE_SPINNER = "to_language";
    SharedPreferences mSettings;

    Map<String, String> mServerErrorsMap; // Список возможных ошибок перевода сервера.

    Map<String, String> mLanguagesMap; // Список поддерживаемых языков, ключ — название языка для текущей локали, значение — код языка.
    TranslationCard translationCard; // Текущая карточка перевода.

    // Компоненты UI.
    EditText mEditTextTextToTranslate;
    TextView mTextViewTranslatedText;
    Spinner mSpinnerFromLanguage, mSpinnerToLanguage;
    ImageButton mImageButtonClearTextToTranslate, mImageButtonAddToBookmarks;
    ImageButton mImageButtonRevertLanguages, mImageButtonHistory, mImageButtonBookmarks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Инициализация списка поддерживаемых языков.
        mLanguagesMap = new TreeMap<>();
        mLanguagesMap = TranslatorLab.get(getActivity()).getLanguages();

        // Запрос к серверу на получение свежего списка поддерживаемых языков.
        TranslatorApp.getApi().getLanguages(LocaleUtils.getCurrentLanguage(getActivity()),
                getString(R.string.yandex_translator_api)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonPrimitive error = response.body().getAsJsonPrimitive("code");
                if (error != null) {
                    Log.e(TAG, "Error " + error.toString() + ": " + response.body().getAsJsonPrimitive("code").toString());
                    return;
                }
                JsonObject responseJSONObject = response.body().getAsJsonObject("langs");
                mLanguagesMap.clear();
                for (Map.Entry<String, JsonElement> entry : responseJSONObject.entrySet()) {
                    mLanguagesMap.put(entry.getValue().getAsString(), entry.getKey());
                }
                TranslatorLab.get(getActivity()).updateLanguagesList(mLanguagesMap);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error while getting languages list from server", t);
            }
        });

        // Инициализация описания ошибок сервера при запросах на перевод.
        mServerErrorsMap = new HashMap<>();
        mServerErrorsMap.put("401", getString(R.string.server_error_401));
        mServerErrorsMap.put("402", getString(R.string.server_error_402));
        mServerErrorsMap.put("404", getString(R.string.server_error_404));
        mServerErrorsMap.put("413", getString(R.string.server_error_413));
        mServerErrorsMap.put("422", getString(R.string.server_error_422));
        mServerErrorsMap.put("501", getString(R.string.server_error_501));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_translator, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(null);

        mSpinnerFromLanguage = (Spinner) view.findViewById(R.id.from_language_spinner);
        mSpinnerToLanguage = (Spinner) view.findViewById(R.id.to_language_spinner);

        mEditTextTextToTranslate = (EditText) view.findViewById(R.id.text_to_translate_edit_text);

        mTextViewTranslatedText = (TextView) view.findViewById(R.id.translated_text_text_view);

        // Кнопка очистки поля ввода.
        mImageButtonClearTextToTranslate = (ImageButton) view.findViewById(R.id.clear_text_to_translate_image_button);
        mImageButtonClearTextToTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextTextToTranslate.setText("");
            }
        });

        // Кнопка добавления текущего перевода в избранные карточки.
        mImageButtonAddToBookmarks = (ImageButton) view.findViewById(R.id.add_to_bookmarks_image_button);
        mImageButtonAddToBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromBuffer = mEditTextTextToTranslate.getText().toString();
                String toBuffer = mTextViewTranslatedText.getText().toString();
                if ((translationCard != null) && (!fromBuffer.equals(""))) {
                    if (translationCard.isBookmarked()) {
                        translationCard.setBookmarked(false);
                        mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_24dp);
                        TranslatorLab.get(getActivity()).updateTranslationCard(translationCard);
                    } else {
                        translationCard.setBookmarked(true);
                        mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_teal_24dp);
                        TranslatorLab.get(getActivity()).updateTranslationCard(translationCard);
                    }
                } else if ((!fromBuffer.equals("")) && (!toBuffer.isEmpty())) {
                    translationCard = new TranslationCard();
                    translationCard.setTextToTranslate(fromBuffer);
                    translationCard.setTranslatedText(toBuffer);
                    translationCard.setFromLanguage(mLanguagesMap.get(mSpinnerFromLanguage.getSelectedItem().toString()));
                    translationCard.setToLanguage(mLanguagesMap.get(mSpinnerToLanguage.getSelectedItem().toString()));
                    translationCard.setBookmarked(true);
                    TranslatorLab.get(getActivity()).addTranslationCard(translationCard);
                    mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_teal_24dp);
                }
            }
        });

        // Кнопка загрузки фрагмента со списком истории переводов.
        mImageButtonHistory = (ImageButton) view.findViewById(R.id.history_image_button);
        mImageButtonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                ft.replace(R.id.fragment_container, TranslationsListFragment.newInstance(false), "HistoryFragment");
                ft.addToBackStack(null);
                ft.commit();
                switchKeyboard(false);
            }
        });

        // Кнопка загрузки фрагмента со списком избранных карточек перевода.
        mImageButtonBookmarks = (ImageButton) view.findViewById(R.id.bookmarks_image_button);
        mImageButtonBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchKeyboard(false);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                ft.replace(R.id.fragment_container, TranslationsListFragment.newInstance(true), "BookmarksFragment");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        // Кнопка обмена языков ввода и вывода.
        mImageButtonRevertLanguages = (ImageButton) view.findViewById(R.id.revert_languages_image_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Тэг устанавливается для предотвращения загрузки перевода при возврате из списков к фрагменту перевода.
        mEditTextTextToTranslate.setTag(false);

        // Установка обработчика вводу текста.
        mEditTextTextToTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEditTextTextToTranslate.getText().toString().equals("")) {
                    mTextViewTranslatedText.setText("");
                    mImageButtonClearTextToTranslate.setVisibility(View.GONE);
                    mImageButtonAddToBookmarks.setVisibility(View.GONE);
                } else {
                    mImageButtonClearTextToTranslate.setVisibility(View.VISIBLE);
                    mImageButtonAddToBookmarks.setVisibility(View.VISIBLE);
                    makeTranslation(s);
                }
            }
        });

        // Если возвращаемся к фрагменту из списка и у нас нет соединения, то устанавливаем соответствующий цвет текста.
        if (mTextViewTranslatedText.getText().toString().equals(getString(R.string.translation_error_label))) {
            mTextViewTranslatedText.setTextColor(0x4dffffff);
        }

        // Инициализация объекта SharedPreferences.
        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Инициализация спиннеров с языками.
        initLangSpinners();

        // Делаем перевод при возврате из списка.
        makeTranslation(mEditTextTextToTranslate.getText());

        // Установка обработчика нажатий кнопки обмена языков. Делается после инициализации спиннеров.
        mImageButtonRevertLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = mSpinnerFromLanguage.getSelectedItemPosition();
                mSpinnerFromLanguage.setSelection(mSpinnerToLanguage.getSelectedItemPosition());
                mSpinnerToLanguage.setSelection(i);
                if (mEditTextTextToTranslate.getText().toString().equals("")) {
                    mTextViewTranslatedText.setText("");
                } else {
                    makeTranslation(mEditTextTextToTranslate.getText());
                }
            }
        });

        // Настрока видимости кнопок управления переводимым текстом.
        if (!mEditTextTextToTranslate.getText().toString().equals("")) {
            mImageButtonClearTextToTranslate.setVisibility(View.VISIBLE);
            mImageButtonAddToBookmarks.setVisibility(View.VISIBLE);
        }

        // Устанавливаем фокус на поле ввода.
        mEditTextTextToTranslate.requestFocus();
//        switchKeyboard(true); // Автопоказ клавиатуры убрал, т. к. это создавало мельтешение на экране.

    }

    @Override
    public void onPause() {
        super.onPause();
        // Сохраняем в SharedPreferences текущие языки перевода.
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_FROM_LANGUAGE_SPINNER, mLanguagesMap.get(mSpinnerFromLanguage.getSelectedItem().toString()));
        editor.putString(APP_PREFERENCES_TO_LANGUAGE_SPINNER, mLanguagesMap.get(mSpinnerToLanguage.getSelectedItem().toString()));
        editor.apply();
    }

    // Инициализация спиннеров.
    private void initLangSpinners() {
        if (mLanguagesMap != null) {
            List<String> langsList = new ArrayList<>(mLanguagesMap.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, langsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            String lang = mSettings.getString(APP_PREFERENCES_FROM_LANGUAGE_SPINNER, "ru");
            mSpinnerFromLanguage.setAdapter(adapter);
            mSpinnerFromLanguage.setSelection(langsList.indexOf(getCurrentLangCodeByDisplayName(mLanguagesMap, lang)));
            lang = mSettings.getString(APP_PREFERENCES_TO_LANGUAGE_SPINNER, "en");
            mSpinnerToLanguage.setAdapter(adapter);
            mSpinnerToLanguage.setSelection(langsList.indexOf(getCurrentLangCodeByDisplayName(mLanguagesMap, lang)));
        }
        // Установка обработчика нажатия для спиннеров, предотвращаюшего выбор значения спиннера системой.
        // Обработчик выполняет заданный код, только если сам пользователь кликает по спиннеру.
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener();
        mSpinnerFromLanguage.setOnItemSelectedListener(spinnerInteractionListener);
        mSpinnerFromLanguage.setOnTouchListener(spinnerInteractionListener);
        mSpinnerToLanguage.setOnItemSelectedListener(spinnerInteractionListener);
        mSpinnerToLanguage.setOnTouchListener(spinnerInteractionListener);
    }

    // Получение кода языка по его наименованию в текущей локали.
    private <T, E> T getCurrentLangCodeByDisplayName(Map<T, E> langMap, E value) {
        for (Map.Entry<T, E> lang : langMap.entrySet()) {
            if (value.equals(lang.getValue())) {
                return lang.getKey();
            }
        }
        return null;
    }

    // Запрос на перевод.
    private void makeTranslation(final Editable s) {

        String fromLang = mLanguagesMap.get(mSpinnerFromLanguage.getSelectedItem().toString());
        String toLang = mLanguagesMap.get(mSpinnerToLanguage.getSelectedItem().toString());
        if (fromLang == null) {
            fromLang = "ru";
        }
        if (toLang == null) {
            toLang = "en";
        }

        // Сначала пытаемся прочитать из базы данных существующую карточку перевода.
        translationCard = TranslatorLab.get(getActivity()).getTranslationCardByTextAndLangs(s.toString(), fromLang, toLang);
        if (translationCard != null) { // Если карточка существует, загружаем её в UI.
            translationCard.setRequestDate(new Date());
            mTextViewTranslatedText.setTextColor(0xffffffff);
            mTextViewTranslatedText.setText(translationCard.getTranslatedText());
            if (translationCard.isBookmarked()) {
                mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_teal_24dp);
            } else {
                mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_24dp);
            }
            TranslatorLab.get(getActivity()).updateTranslationCard(translationCard);
        } else if ((!(Boolean) mEditTextTextToTranslate.getTag()) &&
                (TranslatorLab.get(getActivity()).getAllTranslationCards().isEmpty())) {
            // Для предотвращения обращения к серверу при возврате из очищенного списка к фрагменту перевода.
            // Карточки в БД не существует, но перевод, находящийся в UI, мы только что стёрли из БД,
            // поэтому не нужно его снова записывать туда.
            mEditTextTextToTranslate.setTag(true);
        } else {

            // В остальных случаях посылаем запрос к серверу.
            TranslatorApp.getApi().getTranslation(
                    s.toString(),
                    fromLang + "-" + toLang,
                    getString(R.string.yandex_translator_api)
            ).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.body() != null) {

                        // Ловим коды ошибок от сервера.
                        JsonPrimitive code = response.body().getAsJsonPrimitive("code");
                        try {
                            if ((code != null) && (!code.getAsString().equals("200"))) {
                                Log.e(TAG, "Server error " + code.getAsString() + ": " + mServerErrorsMap.get(code.getAsString()));
                                return;
                            }
                        } catch (NullPointerException npe) {
                            Log.e(TAG, "Error while parsing JSON with translated text", npe);
                        }

                        // Парсим ответ с переведённым текстом и вносим изменнеия в UI и БД.
                        String buffer = response.body().getAsJsonArray("text").get(0).toString();
                        buffer = buffer.substring(1, buffer.length() - 1);
                        mTextViewTranslatedText.setTextColor(0xffffffff);
                        mTextViewTranslatedText.setText("");
                        if (!buffer.equals("")) {
                            buffer = buffer.replace("\\n", System.getProperty("line.separator"));
                            mTextViewTranslatedText.setText(buffer);
                        }
                        mImageButtonAddToBookmarks.setImageResource(R.drawable.ic_bookmark_24dp);
                        Log.d(TAG, response.body().getAsJsonArray("text").toString());
                        Linkify.addLinks(mTextViewTranslatedText, Linkify.ALL);
                        mTextViewTranslatedText.setMovementMethod(LinkMovementMethod.getInstance());
                        translationCard = new TranslationCard();
                        translationCard.setTextToTranslate(s.toString());
                        translationCard.setTranslatedText(mTextViewTranslatedText.getText().toString());
                        translationCard.setFromLanguage(mLanguagesMap.get(mSpinnerFromLanguage.getSelectedItem().toString()));
                        translationCard.setToLanguage(mLanguagesMap.get(mSpinnerToLanguage.getSelectedItem().toString()));
                        translationCard.setBookmarked(false);
                        translationCard.setRequestDate(new Date());
                        TranslatorLab.get(getActivity()).addTranslationCard(translationCard);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    mTextViewTranslatedText.setText(R.string.translation_error_label);
                    mTextViewTranslatedText.setTextColor(0x4dffffff);
                    Log.e(TAG, "Error while getting translation from server", t);
                }
            });

        }

    }

    // Анимация при смене фрагментов.
    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            AnimationSet animSet = new AnimationSet(true);
            animSet.addAnimation(anim);
            return animSet;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    // Показ / сокрытие клавиатуры.
    private void switchKeyboard(boolean show) {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (show) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // Класс обработчика нажатий спиннеров, предотвращающий автовыбор значения спиннера системой.
    // Выбор происходит, только если пользователь коснулся экрана.
    private class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        private boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userSelect) {
                if (mEditTextTextToTranslate.getText().toString().equals("")) {
                    mTextViewTranslatedText.setText("");
                } else {
                    makeTranslation(mEditTextTextToTranslate.getText());
                }
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

}
