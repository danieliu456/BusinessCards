package com.mif.zxcrew.ocrcards;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * TestFragmentAdapter.java
 *
 * Purpose: Set up Scrollable pages with given parameters and information;
 *
 * instantiateItem() new instance of scroll page is created;
 *
 * TestFragmentAdapter() constructor;
 *
 * getItemPosition() returns position of given object;
 *
 * getItem() returns item of given position;
 *
 * getCount() returns length of String (how many pages in ViewPage);
 *
 * getPageTitle() return page title;
 *
 * @author Aivaras Ivoskus
 * @author Ugnius Versekenas
 */

public class TestFragmentAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private String[] content;
    public int contactKey;
    public int cardKey;

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        Object obj = super.instantiateItem(container, position);
        return obj;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        if (object != null) {
            return ((Fragment) object).getView() == view;
        } else {
            return false;
        }
    }

    public TestFragmentAdapter(FragmentManager fm,
                               Context context, String[] data, int contactKey, int cardKey) {
        super(fm);
        this.context = context;
        content = data;
        this.contactKey=contactKey;
        this.cardKey=cardKey;
    }

    @Override
    public int getItemPosition(Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // Pass position to cardKey
        return TestFragment.newInstance(content[position],
                context, contactKey, position);
    }

    @Override
    public int getCount() {
        return content == null ? 0 : content.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return content[position];
    }

}