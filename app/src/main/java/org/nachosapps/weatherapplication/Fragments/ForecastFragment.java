package org.nachosapps.weatherapplication.Fragments;

import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nachosapps.weatherapplication.Adapters.GridElementAdapter;
import org.nachosapps.weatherapplication.R;


public class ForecastFragment extends Fragment {

    private String[] dates;
    private String[] images;
    private String[] temperatures;
    private HorizontalGridView horizontalGridView;

    public ForecastFragment() {
        // Required empty public constructor
    }

    public static ForecastFragment newInstance(String[] dates, String[] images, String[]
            temperatures) {
        ForecastFragment fragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putStringArray("dates", dates);
        args.putStringArray("temps", temperatures);
        args.putStringArray("images", images);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        if (getArguments() != null) {
            dates = getArguments().getStringArray("dates");
            images = getArguments().getStringArray("images");
            temperatures = getArguments().getStringArray("temps");

        }

        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        horizontalGridView = (HorizontalGridView) view;
        GridElementAdapter adapter = new GridElementAdapter(dates, images, temperatures,
                getContext());
        horizontalGridView.setAdapter(adapter);
        return view;
    }
}
