package com.example.junbrother93.imagesearch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText eTxtSearch;
    private Button btnSearch;
    private ScrollView scrResult;
    private TextView txtResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eTxtSearch = (EditText)findViewById(R.id.txtSearch);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        scrResult = (ScrollView)findViewById(R.id.scrResult);
        txtResult = (TextView)findViewById(R.id.txtResult);
        txtResult.setMovementMethod(new ScrollingMovementMethod());

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "https://secure.flickr.com/services/rest/?method=flickr.photos.search&api_key=6832a4fb7e1f14b87fa3cac4f52e0594&text='cat'&safe_search=1&content_type=1&sort=interestingness-desc";

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        //요청 성공 시
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("result", "[" + response + "]");
                                txtResult.setText(response);
                            }
                        },
                        // 에러 발생 시
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("error", "[" + error.getMessage() + "]");
                            }
                        }) {
                    //요청보낼 때 추가로 파라미터가 필요할 경우
                    //url?a=xxx 이런식으로 보내는 대신에 아래처럼 가능.
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("param1", "isGood");
                        return params;
                    }
                };


                queue.add(request);
        }
    }
}
