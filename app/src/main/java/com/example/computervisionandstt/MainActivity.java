package com.example.computervisionandstt;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private int WRITE_REQUEST_CODE = 43;
    private ParcelFileDescriptor pfd;
    private FileOutputStream fileOutputStream;
    public static final String topFile ="TopFile";

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button toOcr=findViewById(R.id.toOcr);

        //내부저장소 파일 쓰기
        FileOutputStream fos=null;
        Context context=getApplicationContext();
        try{
            fos=openFileOutput(topFile, Context.MODE_PRIVATE);
            fos.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


        String folderPath=context.getFilesDir().toString()+"/"+"foder1";

        File mkFoilderFile=new File(folderPath);

        if(!mkFoilderFile.exists())
            mkFoilderFile.mkdirs();

        toOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ocrintent=new Intent(getApplicationContext(),Ocr.class);
                startActivity(ocrintent);
            }
        });
    }


    //외부저장소 파일 쓰기
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void StartRecord(){
        try {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfNow
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDate = sdfNow.format(date);

            /**
             * SAF 파일 편집
             * */
            String fileName = "OCRTTS"+formatDate+".txt";

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain" );
            intent.putExtra(Intent.EXTRA_TITLE,fileName);

            startActivityForResult(intent, WRITE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.d("location",uri+"");
            addText(uri);
        }
    }

    public void addText(Uri uri){
        try {
            pfd = this.getContentResolver().openFileDescriptor(uri, "w");
            fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}