package com.general.files;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class ManageScroll {
    CollapsingToolbarLayout collapsing_toolbar;

    public ManageScroll(CollapsingToolbarLayout collapsing_toolbar) {
        this.collapsing_toolbar = collapsing_toolbar;
    }

    public    void scroll(boolean scroll) {
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) collapsing_toolbar.getLayoutParams();
        if (scroll){
            toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        }else {
            toolbarLayoutParams.setScrollFlags(0);
        }
        collapsing_toolbar.setLayoutParams(toolbarLayoutParams);
    }

}
