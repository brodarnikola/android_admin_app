package hr.sil.android.smartlockers.adminapp.data

import hr.sil.android.smartlockers.adminapp.store.model.MPLDevice

sealed class ItemHomeScreen {
    abstract fun getRecyclerviewItemType(): Int

    class Header : ItemHomeScreen() {
        private val ITEM_HEADER_HOME_SCREEN = 0

        override fun getRecyclerviewItemType(): Int {
            return ITEM_HEADER_HOME_SCREEN
        }

        var headerTitle: String = ""
    }

    class Child(var mplOrSplDevice: MPLDevice? = null) : ItemHomeScreen() {
        private val ITEM_CHILD_HOME_SCREEN = 1

        override fun getRecyclerviewItemType(): Int {
            return ITEM_CHILD_HOME_SCREEN
        }

    }
}

