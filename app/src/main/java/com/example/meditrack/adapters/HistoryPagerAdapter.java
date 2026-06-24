package com.example.meditrack.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.meditrack.fragments.HistoryDoctorFragment;
import com.example.meditrack.fragments.HistorySymptomsFragment;
import com.example.meditrack.fragments.*;

public class HistoryPagerAdapter extends FragmentStateAdapter {

    public HistoryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new com.meditrack.app.fragments.HistoryVitalsFragment();
            case 1:
                return new HistorySymptomsFragment();
            case 2:
                return new HistoryDoctorFragment();
            default:
                return new com.meditrack.app.fragments.HistoryVitalsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}