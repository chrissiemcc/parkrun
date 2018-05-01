package com.parkrun.main.fragments.myparkrun;


        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import com.parkrun.main.R;

public class MyParkrunNewsFragment extends MyParkrunMainFragment
{
    public MyParkrunNewsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_parkrun_news, container, false);
    }

}
