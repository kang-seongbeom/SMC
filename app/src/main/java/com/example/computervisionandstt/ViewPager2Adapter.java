package com.example.computervisionandstt;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPager2Adapter extends FragmentStateAdapter {

    public int mCount;

    public ViewPager2Adapter(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);

        if(index==0) return new ImageFragment();
        else return new TextFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }


    public int getRealPosition(int position) { return position % mCount; }

}