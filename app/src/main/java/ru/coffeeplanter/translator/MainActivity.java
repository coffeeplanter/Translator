package ru.coffeeplanter.translator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Класс основной активити — контейнера для фрагментов.
 * ====================================================
 *
 * Дизайн отличается от того, что был на странице с тестовым заданием, т. к. насколько я понял,
 * там был дан рекомендованный вариант, а не обязательный.
 * Мне кажется, мой дизайн удачнее с точки зрения UX.
 * Допускаю, что неправ, но всё-таки сейчас моё мнение таково.
 *
 * Приложение состоит из одной активити-контейнера и двух классов фрагментов, загружаемых в неё по очереди.
 * TranslatorFragment — фрагмент для интерфейса перевода, загружается при запуске приложения.
 * TranslationsListFragment — фрагмент для списков карточек перевода. Используется для вывода
 * истории переводов и избранных переводов.
 *
 * Для обращений к серверу используется библиотека Retrofit2.
 * Она удобна небольшим размером кода для запросов, автоматическим управлением потоками, автообработкой ошибок.
 * Также удобна автоконвертация JSON-файлов в POJO-объекты, но я эту возможность не использовал.
 *
 * История переводов и избранные переводы хранятся в базе данных SQLite,
 * т. к. их может быть много, а доступ к ним должны быстрым.
 * Хранение таких данных в файлах настроек может замедлить работу приложения.
 * Список поддерживаемых языков также хранится в БД.
 *
 * Я постарался использовать максимально современные компоненты приложения, в частности,
 * ConstraintLayout, RecyclerView, анимацию при переключении фрагментов и в списках,
 * для списков также реализовал удаление элементов с помощью свайпов.
 *
 * Не знаю, насколько удачной является идея загрузки списка языков при каждом старте приложения.
 * Из-за этого оно медленновато запускается.
 * Также не уверен, насколько оптимально я реализовал загрузку фонового изображения приложения.
 * И ещё недоволен тем, что в истории переводов сохраняется изменение каждой буквы вводимого текста.
 * Было бы правильнее как-то вычленять слова, но я не знаю, как это сделать. :-)
 */

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // Загрузка фрагмента интерфейса перевода
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
    }

    // Обработка нажатия кнопки назад в тулбаре
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
