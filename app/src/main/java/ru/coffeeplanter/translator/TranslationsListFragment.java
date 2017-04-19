package ru.coffeeplanter.translator;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс фрагмента для списков истории и закладок.
 */

public class TranslationsListFragment extends Fragment {

    private List<TranslationCard> mTranslationCards;
    private TranslationCardAdapter mTranslationCardAdapter;
    private RecyclerView mTranslationCardsRecyclerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_translations_list, container, false);

        mTranslationCardsRecyclerView = (RecyclerView) view.findViewById(R.id.translation_cards_recycler_view);
        mTranslationCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mTranslationCards == null) {
            mTranslationCards = new ArrayList<>();
            mTranslationCards.add(new TranslationCard("1 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("2 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("3 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("4 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("5 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("6 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("7 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("8 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("9 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("10 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("11 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("12 House fvfdhjbsdjv df dsfv dsfds d seg se ser  er esg  ers erg ", "Дом  dsfsdg e er esrg seg sdg se df st hstg sdstr", true));
            mTranslationCards.add(new TranslationCard("13 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("14 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("15 Table", "Стол", false));
            mTranslationCards.add(new TranslationCard("16 House", "Дом", true));
            mTranslationCards.add(new TranslationCard("17 Flat", "Квартира", false));
//            mTranslationCards.add(new TranslationCard("Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem.", "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga.", false));

        }

        if (mTranslationCardAdapter == null) {
            mTranslationCardAdapter = new TranslationCardAdapter(mTranslationCards);
            mTranslationCardsRecyclerView.setAdapter(mTranslationCardAdapter);
        }


        // Заголовок приложения
//        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        assert actionBar != null;
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle("История переводов");


        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("TransListFragment", "Some button pressed");
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("TransListFragment", "Up button pressed");
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("История переводов");
    }

    // Метод изменяет вид ActionBar после окончания анимации выхода из фрагмента
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (nextAnim != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    assert actionBar != null;
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setTitle("");
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

    // Класс ViewHolder для RecyclerView
    private class TranslationCardHolder extends RecyclerView.ViewHolder {

        private TextView mTextToTranslateTextView, mTranslatedTextTextView, mTranslationDirectionTextView;
        private ImageButton mBookmarkedImageButton;
        private TranslationCard mTranslationCard;

        public TranslationCardHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            mTextToTranslateTextView = (TextView) itemView.findViewById(R.id.text_to_translate_list_item_text_view);
            mTranslatedTextTextView = (TextView) itemView.findViewById(R.id.translated_text_list_item_text_view);
            mTranslationDirectionTextView = (TextView) itemView.findViewById(R.id.translate_direction_list_item_text_view);
            mBookmarkedImageButton = (ImageButton) itemView.findViewById(R.id.add_to_bookmarks_list_item_image_button);
        }

        public void bindTranslationCard(TranslationCard translationCard) {
            mTranslationCard = translationCard;
            mTextToTranslateTextView.setText(mTranslationCard.getTextToTranslate());
            mTranslatedTextTextView.setText(mTranslationCard.getTranslatedText());
            mTranslationDirectionTextView.setText(mTranslationCard.getTranslationDirection());
            if (mTranslationCard.isBookmarked()) {
                mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_teal_24dp);
            } else {
                mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_white_24dp);
            }
        }

    }

    // Класс адаптера для RecyclerView
    private class TranslationCardAdapter extends RecyclerView.Adapter<TranslationCardHolder> {

        private List<TranslationCard> mTranslationCards;

        public TranslationCardAdapter(List<TranslationCard> translationCards) {
            mTranslationCards = translationCards;
            Log.d("TransListFragment", "" + mTranslationCards.size());
//            setHasStableIds(true);
        }

        @Override
        public TranslationCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
//            View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_2, parent, false);
            return new TranslationCardHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(TranslationCardHolder holder, int position) {
//            holder.setIsRecyclable(false);
            TranslationCard translationCard = mTranslationCards.get(position);
            holder.bindTranslationCard(translationCard);
        }

        @Override
        public int getItemCount() {
            return mTranslationCards.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

}
