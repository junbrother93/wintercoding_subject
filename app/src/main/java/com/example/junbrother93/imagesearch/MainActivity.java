package com.example.junbrother93.imagesearch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    EditText eTxtSearch = (EditText)findViewById(R.id.txtSearch);
    Button btnSearch = (Button)findViewById(R.id.btnSearch);
    //ScrollView scrResult = (ScrollView)findViewById(R.id.scrResult);
    //TextView txtResult = (TextView)findViewById(R.id.txtResult);

    public void onClick(View view) {
        String strSearch = "";
        switch(view.getId())
        {
            case R.id.btnSearch:
                strSearch.concat(eTxtSearch.getText().toString());
                //txtResult.setText(strSearch);
                break;
        }
    }
}
