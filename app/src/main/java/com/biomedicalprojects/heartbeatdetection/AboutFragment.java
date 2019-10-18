package com.biomedicalprojects.heartbeatdetection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutFragment extends BaseFragment {
    private View view;
    private TextView about;

    public static AboutFragment newInstance(String param1, String param2) {
        AboutFragment fragment = new AboutFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =   inflater.inflate(R.layout.fragment_about, container, false);
        about = view.findViewById(R.id.about);
        String aboutText = about.getText() +"\n Version: "+ appVersion();
        about.setText(aboutText);

        return view;
    }


}
