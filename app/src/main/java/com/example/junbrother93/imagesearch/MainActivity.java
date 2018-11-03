package com.example.junbrother93.imagesearch;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.DynamicLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.GridLayout.*;

public class MainActivity extends AppCompatActivity {

    private EditText eTxtSearch;
    private Button btnSearch;
    private ScrollView scrResult;
    private TextView txtResult;
    private GridLayout dynamicLayout;
    private final int DYNAMIC_VIEW_ID = 0x8000;
    private Bitmap bitmap;
    private int nImage;
    private PhotoInfo[] photoInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eTxtSearch = (EditText)findViewById(R.id.txtSearch);
        btnSearch = (Button)findViewById(R.id.btnSearch);
       // txtResult = (TextView)findViewById(R.id.txtResult);
       // txtResult.setMovementMethod(new ScrollingMovementMethod());
        dynamicLayout = (GridLayout) findViewById(R.id.dynamicLayout);
        nImage = 0;

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = "https://secure.flickr.com/services/rest/?method=flickr.photos.search&api_key=6832a4fb7e1f14b87fa3cac4f52e0594&text=";
                String url3 = "&safe_search=1&content_type=1&sort=interestingness-desc&format=json";

                url = url + eTxtSearch.getText() + url3;
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        //요청 성공 시
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("result", "[" + response + "]");
                                String result = response;
                                result = result.replace("jsonFlickrApi(", "");
                                result = result.replace(")","");

                                try{
                                    deleteDynamicArea();    //  그리드 뷰 내 이미지 뷰 다 지우기

                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject jsonObjectPhotos = jsonObject.getJSONObject("photos");
                                    JSONArray jsonArrayPhoto = jsonObjectPhotos.getJSONArray("photo");

                                    int length = jsonArrayPhoto.length();

                                    photoInfo = new PhotoInfo[length];


                                    for(int i=0; i < 12; i++)
                                    {
                                        JSONObject jsonObjectPhotoInfo = jsonArrayPhoto.getJSONObject(i);
                                        photoInfo[i] = new PhotoInfo();
                                        photoInfo[i].setFarm_id(jsonObjectPhotoInfo.optString("farm"));
                                        photoInfo[i].setServer_id(jsonObjectPhotoInfo.optString("server"));
                                        photoInfo[i].setId(jsonObjectPhotoInfo.optString("id"));
                                        photoInfo[i].setSecret(jsonObjectPhotoInfo.optString("secret"));
                                        addDynamicArea();   // 그리드 내 이미지 뷰 추가
                                    }
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // txtResult.setText(result);

                            }
                        },
                        // 에러 발생 시
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("error", "[" + error.getMessage() + "]");
                            }
                        }) ;
                queue.add(request);
        }
    }

    private void addDynamicArea() throws IOException {
        nImage++;
        String tempURL="https://farm";

        String farm_id = photoInfo[nImage-1].getFarm_id();
        String server_id = photoInfo[nImage-1].getServer_id();
        String id = photoInfo[nImage-1].getId();
        String secret = photoInfo[nImage-1].getSecret();

        tempURL = tempURL + farm_id + ".staticflickr.com/" + server_id + "/" + id + "_" + secret + "_n.jpg";

        final URL url = new URL(tempURL);


        Thread mThread = new Thread(){
            @Override
            public void run(){
                try {
                    Log.d("0", url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream inputStream = conn.getInputStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                } catch(MalformedURLException e)
                {
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };

        mThread.start();


        try{
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        final ImageView dynamicImageView = new ImageView(this);
        dynamicImageView.setId(DYNAMIC_VIEW_ID + nImage);
        dynamicImageView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("url", v.getTag().toString());
                startActivity(intent);
            }
        });
        dynamicImageView.setMaxHeight(1);
        dynamicImageView.setMaxWidth(1);
        dynamicImageView.setPadding(20,20,20,20);
        dynamicImageView.setScaleType(ImageView.ScaleType.FIT_START);
        dynamicImageView.setTag(tempURL);
        dynamicImageView.setImageBitmap(bitmap);
        Log.d("setImage", "setImage");
        dynamicLayout.addView(dynamicImageView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    private void deleteDynamicArea() {
        dynamicLayout.removeAllViews();
        nImage = 0;
    }
}
