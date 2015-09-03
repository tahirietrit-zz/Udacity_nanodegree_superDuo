package it.jaschke.alexandria;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class About extends Fragment {

    public About() {

    }

    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        activity.setTitle(R.string.about);
    }


    String title = "About this App";

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.setTitleInterface.onSetTitle(title);
    }


}
