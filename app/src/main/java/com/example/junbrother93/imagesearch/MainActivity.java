package com.example.junbrother93.imagesearch;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.DynamicLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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

        eTxtSearch = (EditText) findViewById(R.id.txtSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        dynamicLayout = (GridLayout) findViewById(R.id.dynamicLayout);

        nImage = 0;

        eTxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    default:
                        Volley();
                        return false;
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:
                Volley();
        }
    }

    private void Volley()
    {
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
                        result = result.replace(")", "");

                        try {
                            deleteDynamicArea();    //  그리드 뷰 내 이미지 뷰 다 지우기

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject jsonObjectPhotos = jsonObject.getJSONObject("photos");
                            JSONArray jsonArrayPhoto = jsonObjectPhotos.getJSONArray("photo");

                            int length = jsonArrayPhoto.length();

                            photoInfo = new PhotoInfo[length];

                            for (int i = 0; i < 30; i++) {  // 30개만..
                                volleyThread(jsonArrayPhoto, i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                // 에러 발생 시
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "[" + error.getMessage() + "]");
                    }
                });
        queue.add(request);
    }

    private void deleteDynamicArea() {
        dynamicLayout.removeAllViews();
        nImage = 0;
    }

    private void volleyThread(final JSONArray jsonArrayPhoto, final int i) {
        final Thread mThread = new Thread() {
            @Override
            public void run() {
                JSONObject jsonObjectPhotoInfo = null;
                try {
                    jsonObjectPhotoInfo = jsonArrayPhoto.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                photoInfo[i] = new PhotoInfo();
                photoInfo[i].setFarm_id(jsonObjectPhotoInfo.optString("farm"));
                photoInfo[i].setServer_id(jsonObjectPhotoInfo.optString("server"));
                photoInfo[i].setId(jsonObjectPhotoInfo.optString("id"));
                photoInfo[i].setSecret(jsonObjectPhotoInfo.optString("secret"));
                imageThread(i);   // 그리드 내 이미지 뷰 추가
            }
        };
        mThread.start();
    }

    private void imageThread(final int nCount) {
        nImage++;


        String tempURL = "https://farm";

        String farm_id = photoInfo[nCount].getFarm_id();
        String server_id = photoInfo[nCount].getServer_id();
        final String id = photoInfo[nCount].getId();
        String secret = photoInfo[nCount].getSecret();

        tempURL = tempURL + farm_id + ".staticflickr.com/" + server_id + "/" + id + "_" + secret + "_n.jpg";

        URL url = null;
        try {
            url = new URL(tempURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final URL finalUrl = url;

        final String finalTempURL = tempURL;
        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("0", finalUrl.toString());
                    HttpURLConnection conn = (HttpURLConnection) finalUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream inputStream = conn.getInputStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    addDynamicArea(nImage, finalTempURL, bitmap, id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
    }

    private void addDynamicArea(int n, final String url, Bitmap b, final String id) throws IOException {
        final ImageView dynamicImageView = new ImageView(this);
        dynamicImageView.setId(DYNAMIC_VIEW_ID + n);
        dynamicImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        dynamicImageView.setPadding(20, 20, 20, 20);
        dynamicImageView.setScaleType(ImageView.ScaleType.FIT_START);
        dynamicImageView.setTag(url);
        dynamicImageView.setImageBitmap(b);
        Log.d("setImage", "setImage");

        final Handler handler = new Handler(Looper.getMainLooper()) //UI 작업 처리를 위해 UI쓰레드에 바인딩 된 handler를 만듦
        {
            public void handleMessage(Message mag) {
                dynamicLayout.addView(dynamicImageView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        };
        new Thread()    // UI작업 처리 쓰레드
        {
            public void run() {
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        }.start();
    }

    private void setURLThread(final int n) {

        String getimageURL = "https://secure.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=6832a4fb7e1f14b87fa3cac4f52e0594&photo_id=";
        getimageURL = getimageURL + photoInfo[n].getId() + "&format=json";
        final String finalGetimageURL = getimageURL;

        final Thread mThread = new Thread() {
            @Override
            public void run() {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

                StringRequest request = new StringRequest(Request.Method.POST, finalGetimageURL,
                        //요청 성공 시
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("result", "[" + response + "]");
                                String result = response;
                                result = result.replace("jsonFlickrApi(", "");
                                result = result.replace(")", "");
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject jsonObjectSizes = jsonObject.getJSONObject("sizes");
                                    JSONArray jsonArraySize = jsonObjectSizes.getJSONArray("size");
                                    int length = jsonArraySize.length();

                                    for (int i = 0; i < length; i++) {
                                        JSONObject jsonObjectPhotoInfo = jsonArraySize.getJSONObject(i);
                                        if(jsonObjectPhotoInfo.optString("label").equals("Small"))
                                        {
                                            photoInfo[n].setsURL(jsonObjectPhotoInfo.optString("label"));
                                            imageThread(n);
                                        }
                                        else if(jsonObjectPhotoInfo.optString("label").equals("Original"))
                                        {
                                            photoInfo[n].setoURL(jsonObjectPhotoInfo.optString("label"));
                                        }
                                        else
                                            continue;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        // 에러 발생 시
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("error", "[" + error.getMessage() + "]");
                            }
                        });
                queue.add(request);
            }
        };
        mThread.start();
    }
}

