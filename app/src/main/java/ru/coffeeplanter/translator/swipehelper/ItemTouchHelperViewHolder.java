package ru.coffeeplanter.translator.swipehelper;

public interface ItemTouchHelperViewHolder {

    /**
     * Вызывается при начале свайпа.
     */
    void onItemSelected();

    /**
     * Вызывается по окончании свайпа.
     */
    void onItemClear();

}
