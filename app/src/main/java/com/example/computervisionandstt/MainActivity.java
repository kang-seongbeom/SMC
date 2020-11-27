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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toOcr=findViewById(R.id.toOcr);
        Button toFileView=findViewById(R.id.toFileView);


        toOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ocrIntent=new Intent(getApplicationContext(),Ocr.class);
                startActivity(ocrIntent);
            }
        });

        toFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileViewIntent=new Intent(getApplicationContext(),FileView.class);
                startActivity(fileViewIntent);
            }
        });
    }
}