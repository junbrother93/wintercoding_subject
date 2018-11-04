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
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

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
    private String URL;
    private String Id;
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        selectImage = (ImageView)findViewById(R.id.selectImage);
        mAttacher = new PhotoViewAttacher(selectImage);
        btnSave = (Button)findViewById(R.id.btnSave);

        Intent intent = getIntent();

        URL = intent.getStringExtra("url");
        Id = intent.getStringExtra("Id");

        URL = URL.replaceAll("_n.jpg", ".jpg");

        filename = intent.getStringExtra("filename");

        Log.d("0", URL);
        URL url2 = null;
        try {
            url2 = new URL(URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        final URL finalUrl = url2;

        Thread mThread = new Thread(){
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
        selectImage.setImageBitmap(bitmap);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
            {
                saveImage(filename, bitmap);
            }
        }
    }

    private void saveImage(String filename, Bitmap bitmap) {
        String StoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String savePath = StoragePath;
        showPermissionDialog(savePath);
        File f = new File(savePath);
        if(!f.isDirectory())f.mkdirs();
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(savePath+"/Download/"+filename+".jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showPermissionDialog(final String savePath) {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(SubActivity.this, savePath + "/Downloads/ 폴더에 저장", Toast.LENGTH_SHORT).show();
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
}
