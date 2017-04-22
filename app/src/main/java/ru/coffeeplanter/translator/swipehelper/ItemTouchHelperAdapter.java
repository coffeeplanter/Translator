package ru.coffeeplanter.translator.swipehelper;

public interface ItemTouchHelperAdapter {

    /**
     * Вызывается при удалении из RecyclerView при помощи свайпа.
     */
    void onItemDismiss(int position);

}
