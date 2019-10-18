package com.biomedicalprojects.heartbeatdetection;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    HomeFragment homeFragment = new HomeFragment();
                    ft.replace(R.id.container, homeFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_terminal:
                   TerminalFragment terminalFragment = new TerminalFragment();
                    ft.replace(R.id.container, terminalFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_setting:
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new SettingFragment())
                            .commit();
                    return true;
                case R.id.navigation_about:
                    AboutFragment aboutFragment = new AboutFragment();
                    ft.replace(R.id.container, aboutFragment);
                    ft.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        ft.replace(R.id.container, homeFragment);
        ft.commit();
    }

}
