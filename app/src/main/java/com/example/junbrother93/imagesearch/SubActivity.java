package com.example.junbrother93.imagesearch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class SubActivity extends Activity {

    private Bitmap bitmap;
    private ImageView selectImage;
    private URL url2;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        selectImage = (ImageView)findViewById(R.id.selectImage);

        Intent intent = getIntent();
        String tempURL2 = intent.getStringExtra("url");
        tempURL2.replace("_n.jpg", ".jpg");

        URL url2 = null;
        try {
            url2 = new URL(tempURL2);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final URL finalUrl = url2;
        Thread mThread = new Thread(){
            @Override
            public void run(){
                try {
                    Log.d("0", finalUrl.toString());
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

        selectImage.setImageBitmap(bitmap);
    }
}
