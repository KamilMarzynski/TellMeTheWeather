package org.nachosapps.weatherapplication.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.nachosapps.weatherapplication.Fragments.DescriptionFragment;
import org.nachosapps.weatherapplication.Fragments.ForecastFragment;
import org.nachosapps.weatherapplication.R;

/**
 * Created by Kamil on 2018-02-26.
 */

public class CustomFragmentAdapter extends FragmentStatePagerAdapter {
    private String tabTitles[] = new String[]{"DESCRIPTION", "FORECAST"};
    private Context context;
    private String description;
    private String[] images;
    private String[] temperatures;
    private String[] dates;
    private final int PAGE_COUNT = 2;

    public CustomFragmentAdapter(FragmentManager fm, Context context, String description,
            String[] images,
            String[] temperatures, String[] dates) {
        super(fm);

        this.images = images;
        this.temperatures = temperatures;
        this.dates = dates;
        this.context = context;
        this.description = description;
    }

    public CustomFragmentAdapter(FragmentManager fm, Context context, String description) {
        super(fm);
        this.context = context;
        this.description = description;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return DescriptionFragment.newInstance(description);
        } else if (position == 1 && images != null && temperatures != null && dates != null) {
            return ForecastFragment.newInstance(dates, images, temperatures);
        } else {
            //if there is no data for forecast available, this information will be showed to user
            // it place of forecast
            return DescriptionFragment.newInstance(context.getString(R.string.no_forecast));
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}