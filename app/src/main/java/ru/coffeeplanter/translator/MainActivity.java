package ru.coffeeplanter.translator;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Класс основной активити — контейнера для фрагментов
 */

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private EditText mEditTextToTranslate;
    private TextView mTextViewTranslatedText;
    private Spinner mSpinnerLanguageFrom, mSpinnerLanguageTo;
    private ImageButton mImageButtonRevert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new TranslatorFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }





//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mEditTextToTranslate = (EditText) findViewById(R.id.text_to_translate_edit_text);
//
//        mEditTextToTranslate.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//            @Override
//            public void afterTextChanged(Editable s) {
//                mTextViewTranslatedText.setText(mEditTextToTranslate.getText());
//            }
//        });


//        mEditTextToTranslate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    makeKeyboardVisible(false);
//                }
//            }
//        });

//        mTextViewTranslatedText = (TextView) findViewById(R.id.translated_text_text_view);
////        mTextViewTranslatedText.setMovementMethod(new ScrollingMovementMethod());
//
//
//        mSpinnerLanguageFrom = (Spinner) findViewById(R.id.from_language_spinner);
//        mSpinnerLanguageFrom.setSelection(1);
//
//        mSpinnerLanguageTo = (Spinner) findViewById(R.id.to_language_spinner);
//
//
//

//
//        mImageButtonRevert = (ImageButton) findViewById(R.id.revert_languages_image_button);
//        mImageButtonRevert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int i = mSpinnerLanguageTo.getSelectedItemPosition();
//                mSpinnerLanguageTo.setSelection(mSpinnerLanguageFrom.getSelectedItemPosition());
//                mSpinnerLanguageFrom.setSelection(i);
//            }
//        });


    }




    // Метод для управления видимостью клавиатуры
//    private void makeKeyboardVisible(boolean visibility) {
//        View view = MainActivity.this.getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            if (visibility) {
//                imm.showSoftInputFromInputMethod(view.getWindowToken(), 0);
//            } else {
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
//        }
//    }

}
