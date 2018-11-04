package com.example.junbrother93.imagesearch;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;


public class SubActivity extends Activity {

    private Bitmap bitmap;
    private ImageView selectImage;
    PhotoViewAttacher mAttacher;
    private Button btnSave;
    private String originalSizeURL;
    private String id;
    private String filename;
    private TextView txtLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        selectImage = (ImageView)findViewById(R.id.selectImage);
        btnSave = (Button)findViewById(R.id.btnSave);
        txtLoading = (TextView)findViewById(R.id.txtLoading);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        Volley(id);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
            {
                saveImage(id, bitmap);
            }
        }
    }

    private void saveImage(String filename, Bitmap bitmap) {
        showPermissionDialog();
        String StoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String savePath = StoragePath;
        File f = new File(savePath);
        if(!f.isDirectory())f.mkdirs();
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(savePath+"/Download/"+filename+".jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            Toast.makeText(SubActivity.this, savePath + "/Downloads/ 폴더에 저장", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showPermissionDialog() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(SubActivity.this, "권한 Ok.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(SubActivity.this, "권한 없음.", Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("쓰기 권한 필요")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void Volley(String id)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://secure.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=6832a4fb7e1f14b87fa3cac4f52e0594&photo_id=";
        url = url + id + "&format=json";
        Log.d("url " , url);

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
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject jsonObjectSizes = jsonObject.getJSONObject("sizes");
                            JSONArray jsonArraySize = jsonObjectSizes.getJSONArray("size");
                            int length = jsonArraySize.length();

                            originalSizeURL = jsonArraySize.getJSONObject(length-1).optString("source");
                            Log.d("URL", originalSizeURL);
                            imageLoadingThread(originalSizeURL);

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

    private void imageLoadingThread(final String originalSizeURL) {
        URL url = null;
        try {
            url = new URL(originalSizeURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final URL finalUrl = url;

        final Thread mThread = new Thread(){
            @Override
            public void run(){
                try {
                    HttpURLConnection conn = (HttpURLConnection) finalUrl.openConnection();
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
        selectImage.setScaleType(ImageView.ScaleType.FIT_START);
        selectImage.setImageBitmap(bitmap);
        mAttacher = new PhotoViewAttacher(selectImage);
        txtLoading.setVisibility(View.INVISIBLE);
        btnSave.setEnabled(true);
    }
}
