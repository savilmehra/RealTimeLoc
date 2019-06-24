package com.easy_locater.realtimeloc;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class ExitScreen extends Activity {

private TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splace_activity);

        name= (TextView)findViewById(R.id.name);
        name.setText("Thanks For Using Our App");

    }
}
