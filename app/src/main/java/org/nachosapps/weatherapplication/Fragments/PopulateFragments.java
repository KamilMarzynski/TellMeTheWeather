package org.nachosapps.weatherapplication.Fragments;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.nachosapps.weatherapplication.Adapters.CustomFragmentAdapter;
import org.nachosapps.weatherapplication.Helpers.Weather.WeatherHelpers;

/**
 * Created by Kamil on 2018-03-06.
 */

public class PopulateFragments {

    public static void populateFragments(FragmentManager fm,
            ViewPager viewPager, Context context,
            String description,
            String images[],
            String temperatures[],
            String dates[]) {
        viewPager.setAdapter(new CustomFragmentAdapter(fm,
                context, WeatherHelpers.descriptionPicker(description,
                context), images,
                temperatures, dates));
    }

    public static void populateFragments(FragmentManager fm,
            ViewPager viewPager, Context context, String description) {
        viewPager.setAdapter(new CustomFragmentAdapter(fm, context,
                WeatherHelpers.descriptionPicker(description, context)));
    }
}
