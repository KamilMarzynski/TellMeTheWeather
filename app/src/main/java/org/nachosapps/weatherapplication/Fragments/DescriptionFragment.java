package org.nachosapps.weatherapplication.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nachosapps.weatherapplication.R;


public class DescriptionFragment extends Fragment {
    public static final String ARG_DESC = "ARG_DESC";

    private String mDescription;

    public static DescriptionFragment newInstance(String description) {
        Bundle args = new Bundle();
        args.putString(ARG_DESC, description);
        DescriptionFragment fragment = new DescriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        TransitionManager.beginDelayedTransition(container, new Slide());
        mDescription = getArguments().getString(ARG_DESC);
        View view = inflater.inflate(R.layout.fragment_description, container, false);
        TextView textView = (TextView) view;
        ((TextView) view).setMovementMethod(new ScrollingMovementMethod());
        textView.setText(mDescription);
        return view;
    }
}

