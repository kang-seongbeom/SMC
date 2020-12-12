package com.example.computervisionandstt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OnlyImage extends AppCompatActivity {

    private SubsamplingScaleImageView onlyImageResize;
    private DrawerLayout mDrawerLayout;
    private int degree=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_image);

        //기본 화면 셋팅
        //메뉴
        NavigationView mNavigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View mHeaderView = mNavigationViewing.getHeaderView(0);
        ImageView mVaviUserImage = mHeaderView.findViewById(R.id.navi_user_image);
        Glide.with(this).load(R.drawable.smartphone).into(mVaviUserImage);

        //actionbar
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        mActionBar.setDisplayHomeAsUpEnabled(true); // 메뉴 버튼 만들기
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //메뉴 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int id = menuItem.getItemId();
                //계정정보로 이동
                if(id == R.id.account){
                    Toast.makeText(getApplicationContext(),"이미 계정 정보 입니다.",Toast.LENGTH_SHORT).show();
                }
                //FileView로 가기
                else if(id == R.id.toFileView) {
                    Intent intent=new Intent(getApplicationContext(),FileView.class);
                    startActivity(intent);
                    finish();

                }
                return true;
            }
        });
        //기본화면 셋팅 끝

        onlyImageResize =(SubsamplingScaleImageView) findViewById(R.id.onlyImageResize);

        Intent intent=getIntent();
        String getPath=intent.getStringExtra("folderPath");

        Log.d("getPath",getPath);

        File checkExitImage=new File(getPath+"/"+"image.jpg");
        if(checkExitImage.exists()) {
            onlyImageResize.setImage(ImageSource.bitmap(rotateImage(BitmapFactory.decodeFile(getPath + "/" + "image.jpg"),degree)));
        }


        ImageView onlyImageRotate=findViewById(R.id.onlyImageRotate);
        onlyImageRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degree+=90;
                if (getPath != null) {
                    File mfile = new File(getPath + "/" + "image.jpg");
                    if (mfile.exists()) {
                        onlyImageResize.setImage(ImageSource.bitmap(rotateImage(BitmapFactory.decodeFile(getPath + "/" + "image.jpg"),degree)));
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private Bitmap rotateImage(Bitmap bitmap,float degree){
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }
}