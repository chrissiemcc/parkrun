package com.parkrun.main.fragments.results;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parkrun.main.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResultsYouFragment extends Fragment {


    public ResultsYouFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results_you, container, false);
    }

}
