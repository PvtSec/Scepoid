package com.scepclient.jscep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;

public class InitActivity extends AppCompatActivity {
    public EditText scep_url, common_name, country_name, state_name, org_name, org_unit_name;
    public Button enroll_button;
    public String iscep_url, icommon_name, icountry_name, istate_name, iorg_name, iorg_unit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_init);

        scep_url = findViewById(R.id.scep_url);
        common_name = findViewById(R.id.common_name);
        country_name = findViewById(R.id.country_name);
        state_name = findViewById(R.id.state_name);
        org_name = findViewById(R.id.org_name);
        org_unit_name = findViewById(R.id.org_unit_name);
        enroll_button = findViewById(R.id.enroll_btn);

        enroll_button.setOnClickListener(view -> {
            iscep_url = scep_url.getText().toString();
            icommon_name = common_name.getText().toString();
            icountry_name = country_name.getText().toString();
            istate_name = state_name.getText().toString();
            iorg_name = org_name.getText().toString();
            iorg_unit_name = org_unit_name.getText().toString();

            scepEnroll scepobject = new scepEnroll();
            scepobject.startEnroll(iscep_url, icommon_name, icountry_name, istate_name, iorg_name, iorg_unit_name);
        });
    }
}