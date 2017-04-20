package ru.coffeeplanter.translator;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.coffeeplanter.translator.utils.LocaleUtils;

/**
 * Класс фрагмента основного экрана переводчика.
 */

public class TranslatorFragment extends Fragment {

    private final String TAG = "TranslatorFragment";

    Map<String, String> mLanguagesMap;

    EditText mEditTextTextToTranslate;
    TextView mTextViewTranslatedText;
    Spinner mSpinnerFromLanguage, mSpinnerToLanguage;
    ImageButton mImageButtonRevertLanguages, mImageButtonHistory, mImageButtonBookmarks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_translator, container, false);

        mLanguagesMap = new TreeMap<>();
        mLanguagesMap = TranslatorLab.get(getActivity()).getLanguages();

        mEditTextTextToTranslate = (EditText) view.findViewById(R.id.text_to_translate_edit_text);
        mEditTextTextToTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                requestForTranslation(s);
                mTextViewTranslatedText.setText(mEditTextTextToTranslate.getText());
                Linkify.addLinks(mTextViewTranslatedText, Linkify.ALL);
                mTextViewTranslatedText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });

        mTextViewTranslatedText = (TextView) view.findViewById(R.id.translated_text_text_view);



        mImageButtonHistory = (ImageButton) view.findViewById(R.id.history_image_button);

        mImageButtonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                ft.replace(R.id.fragment_container, new TranslationsListFragment(), "HistoryFragment");
                ft.addToBackStack(null);
                ft.commit();
                hideKeyboard();
//                getActivity().getSupportFragmentManager().executePendingTransactions();
            }
        });

        mImageButtonBookmarks = (ImageButton) view.findViewById(R.id.bookmarks_image_button);
        mImageButtonBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                ft.replace(R.id.fragment_container, new TranslationsListFragment(), "BookmarksFragment");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        mImageButtonRevertLanguages = (ImageButton) view.findViewById(R.id.revert_languages_image_button);
        mImageButtonRevertLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = mSpinnerFromLanguage.getSelectedItemPosition();
                mSpinnerFromLanguage.setSelection(mSpinnerToLanguage.getSelectedItemPosition());
                mSpinnerToLanguage.setSelection(i);
            }
        });

        mSpinnerFromLanguage = (Spinner) view.findViewById(R.id.from_language_spinner);
        mSpinnerToLanguage = (Spinner) view.findViewById(R.id.to_language_spinner);


        // Делаем пустым заголовок приложения
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("");

        Log.d(TAG, LocaleUtils.getCurrentLanguage(getActivity()));

        TranslatorApp.getApi().getLanguages(LocaleUtils.getCurrentLanguage(getActivity()),
                "trnsl.1.1.20170418T233356Z.12f8610ff8a3faff.3de659c2f61b1b3f82f4a871459f1a2a0d861f8c").enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject responseJSONObject = response.body().getAsJsonObject("langs");
                mLanguagesMap.clear();
                for (Map.Entry<String, JsonElement> entry : responseJSONObject.entrySet()) {
                    mLanguagesMap.put(entry.getValue().getAsString(), entry.getKey());
                }
                TranslatorLab.get(getActivity()).updateLanguagesList(mLanguagesMap);
                Log.d(TAG, mLanguagesMap.toString());
                List<String> langsList = new ArrayList<>(mLanguagesMap.keySet());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, langsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerFromLanguage.setAdapter(adapter);
                mSpinnerFromLanguage.setSelection(langsList.indexOf("Русский"));
                mSpinnerToLanguage.setAdapter(adapter);
                mSpinnerToLanguage.setSelection(langsList.indexOf("Английский"));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error while getting languages list from server", t);
            }
        });


        return view;
    }

    private void requestForTranslation(Editable s) {

        TranslatorApp.getApi().getTranslation(s.toString(), "ru-en", "trnsl.1.1.20170418T233356Z.12f8610ff8a3faff.3de659c2f61b1b3f82f4a871459f1a2a0d861f8c").enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body() != null) {
                    String buffer = response.body().getAsJsonArray("text").get(0).toString();
                    mTextViewTranslatedText.setText(buffer.substring(1, buffer.length() - 1));
                    Log.d(TAG, response.body().getAsJsonArray("text").toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error while getting translation from server", t);
            }
        });

    }



    // Метод изменяет вид ActionBar после окончания анимации входа во фрагмент (кроме старта приложения) и выхода из него
    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (enter) {
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        assert actionBar != null;
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setTitle("История переводов");
                    }
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    assert actionBar != null;
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle("История переводов");
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

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        Log.d("TranslatorFragment", "view: " + view);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
