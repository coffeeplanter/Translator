package ru.coffeeplanter.translator;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.coffeeplanter.translator.swipehelper.CardTouchHelperCallback;
import ru.coffeeplanter.translator.swipehelper.ItemTouchHelperAdapter;
import ru.coffeeplanter.translator.swipehelper.ItemTouchHelperViewHolder;

/**
 * Класс фрагмента для списков истории и закладок.
 */

public class TranslationsListFragment extends Fragment {

    private static final String ARG_IS_BOOKMARKS = "crime_id";

    private final String TAG = "TranslationsListFrag";

    private boolean isBookmarks; // Если false, загружаем историю, если true — избранные карточки.

    private List<TranslationCard> mTranslationCards;
    private TranslationCardAdapter mTranslationCardAdapter;
    private RecyclerView mTranslationCardsRecyclerView;
    private TextView mEmptyListTextView;
    Toolbar mToolbar;

    private ItemTouchHelper mItemTouchHelper;


    public static TranslationsListFragment newInstance(boolean isBookmarks) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_BOOKMARKS, isBookmarks);
        TranslationsListFragment fragment = new TranslationsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isBookmarks = getArguments().getBoolean(ARG_IS_BOOKMARKS, false);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_translations_list, container, false);

        mTranslationCardsRecyclerView = (RecyclerView) view.findViewById(R.id.translation_cards_recycler_view);
        mTranslationCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mTranslationCards == null) {
            mTranslationCards = new ArrayList<>();
        }

        if (isBookmarks) {
            mTranslationCards = TranslatorLab.get(getActivity()).getBookmarkedTranslationCards();
        } else {
            mTranslationCards = TranslatorLab.get(getActivity()).getAllTranslationCards();
        }

        if (mTranslationCardAdapter == null) {
            mTranslationCardAdapter = new TranslationCardAdapter(mTranslationCards);
            mTranslationCardsRecyclerView.setAdapter(mTranslationCardAdapter);
        }

        mToolbar = (Toolbar) view.findViewById(R.id.fragment_list_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        if (isBookmarks) {
            mToolbar.setTitle(R.string.bookmarks_header);
        } else {
            mToolbar.setTitle(R.string.history_header);
        }
        try {
            //noinspection ConstantConditions
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Error while setting Back button", npe);
        }

        mEmptyListTextView = (TextView) view.findViewById(R.id.empty_list_text_view);
        switchViewsOnRecyclerViewEmpty();

        return view;
    }

    private void switchViewsOnRecyclerViewEmpty() {
        if (mTranslationCardAdapter.getItemCount() > 0) {
            mTranslationCardsRecyclerView.setVisibility(View.VISIBLE);
            mEmptyListTextView.setVisibility(View.GONE);
        } else {
            mTranslationCardsRecyclerView.setVisibility(View.GONE);
            mEmptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ItemTouchHelper.Callback callback = new CardTouchHelperCallback(mTranslationCardAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mTranslationCardsRecyclerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_translations_list_menu, menu);
        if (isBookmarks) {
            menu.findItem(R.id.menu_item_clear_all).setTitle(R.string.clear_all_bookmarks_menu_item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_all:
                if (isBookmarks) {
                    TranslatorLab.get(getActivity()).deleteTranslationCards(true);
                    mTranslationCards.clear();
                    mTranslationCardAdapter.notifyDataSetChanged();
                    switchViewsOnRecyclerViewEmpty();
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                        return true;
                    }
                } else {
                    TranslatorLab.get(getActivity()).deleteTranslationCards(false);
                    mTranslationCards.clear();
                    mTranslationCards.addAll(TranslatorLab.get(getActivity()).getAllTranslationCards());
                    mTranslationCardAdapter.notifyDataSetChanged();
                    switchViewsOnRecyclerViewEmpty();
                    if (mTranslationCards.isEmpty()) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        if (fm.getBackStackEntryCount() > 0) {
                            fm.popBackStack();
                            return true;
                        }
                    } else {
                        Snackbar.make(mTranslationCardsRecyclerView,
                                R.string.bookmarks_are_being_deleted_separately_message, Snackbar.LENGTH_LONG)
                                .setAction("Action", null)
                                .show();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
    private class TranslationCardHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private TextView mTextToTranslateTextView, mTranslatedTextTextView, mTranslationDirectionTextView;
        private ImageButton mBookmarkedImageButton;
        private TranslationCard mTranslationCard;
        private Drawable mBackgroundBuffer;

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
            if (translationCard.isBookmarked()) {
                mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_teal_24dp);
            } else {
                mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_24dp);
            }
        }

        @Override
        public void onItemSelected() {
            mBackgroundBuffer = itemView.getBackground();
            itemView.setBackgroundColor(0x11ffffff);
//            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        public void onItemClear() {
            itemView.setBackground(mBackgroundBuffer);
        }

    }

    // Класс адаптера для RecyclerView
    private class TranslationCardAdapter extends RecyclerView.Adapter<TranslationCardHolder> implements ItemTouchHelperAdapter {

        private List<TranslationCard> mTranslationCards;

        public TranslationCardAdapter(List<TranslationCard> translationCards) {
            mTranslationCards = translationCards;
        }

        @Override
        public TranslationCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TranslationCardHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(final TranslationCardHolder holder, int position) {
            final TranslationCard translationCard = mTranslationCards.get(position);
            holder.bindTranslationCard(translationCard);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (translationCard.isBookmarked()) {
                        holder.mTranslationCard.setBookmarked(false);
                        holder.mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_24dp);
                        TranslatorLab.get(getActivity()).updateTranslationCard(translationCard);
                        if (isBookmarks) {
                            mTranslationCards.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        }
                    } else {
                        holder.mTranslationCard.setBookmarked(true);
                        if (!TranslatorLab.get(getActivity()).updateTranslationCard(translationCard)) {
                            holder.mTranslationCard.setBookmarked(false);
                            Snackbar.make(holder.mBookmarkedImageButton,
                                    R.string.bookmarks_limit_achieved_message, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null)
                                    .show();
                        } else {
                            holder.mBookmarkedImageButton.setImageResource(R.drawable.ic_bookmark_teal_24dp);
                        }
                    }
                }
            };
            holder.mBookmarkedImageButton.setOnClickListener(listener);
            holder.mTranslationDirectionTextView.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return mTranslationCards.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public void onItemDismiss(int position) {
            TranslatorLab.get(getActivity()).deleteTranslationCard(mTranslationCards.get(position));
            mTranslationCards.remove(position);
            notifyItemRemoved(position);
        }

    }

}
