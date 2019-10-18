package com.biomedicalprojects.heartbeatdetection;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TerminalFragment extends Fragment {
    TextView terminal;
    ScrollView scrollView;
    View view;
    private Handler handler;
    private Runnable handlerTask;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_terminal, container, false);
        terminal = view.findViewById(R.id.terminal);
        scrollView = view.findViewById(R.id.terminalScroll);
        StartTimer();
        return view;
    }

    void StartTimer() {
        handler = new Handler();
        handlerTask = new Runnable() {
            @Override
            public void run() {
                terminal.setText(StaticHelper.TERMINAL_MESSAGE);
                scrollView.fullScroll(View.FOCUS_DOWN);
                handler.postDelayed(handlerTask, 1000);
            }
        };
        handlerTask.run();
    }

}
