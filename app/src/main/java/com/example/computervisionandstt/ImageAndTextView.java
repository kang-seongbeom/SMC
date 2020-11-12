package com.example.computervisionandstt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImageAndTextView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_and_text_view);

        ImageView imageView=findViewById(R.id.savedImage);
        TextView textView=findViewById(R.id.saveButton);
        Button button=findViewById(R.id.ttsStart);

        Intent intent=getIntent();
        String path=intent.getStringExtra("paths");
        if(path!=null){
            Log.d("pathing",path);
//            Glide.with(this).load(path+"/"+"image.jpg").into(imageView);
//            textView.setText(ReadTextFile(path+"/"+"TTStext.txt"));
        }
        else{
            Log.d("pathing","nope");
        }
    }

    //경로의 텍스트 파일읽기
    private String ReadTextFile(String path){
        StringBuffer strBuffer = new StringBuffer();
        try{
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line="";
            while((line=reader.readLine())!=null){
                strBuffer.append(line+"\n");
            }
            reader.close();
            is.close();
        }catch (IOException e){
            e.printStackTrace();
            return "";
        }
        return strBuffer.toString();
    }
}