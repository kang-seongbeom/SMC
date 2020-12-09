package com.example.computervisionandstt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OnlyText extends AppCompatActivity{
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        TextView setText=findViewById(R.id.setText);

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
                    Intent mAccountIntent=new Intent(getApplicationContext(),UserAccount.class);
                    startActivity(mAccountIntent);
                }

                else if(id == R.id.toFileView) {
                    Intent mIntent = new Intent(getApplicationContext(), FileView.class);
                    startActivity(mIntent);
                    finish();
                }
                return true;
            }
        });

        Intent getPathIntent=getIntent();
        String getPathIntentStringExtra=getPathIntent.getStringExtra("folderpath");
        if(getPathIntentStringExtra!=null){
            setText.setText(ReadTextFile(getPathIntentStringExtra));
        }


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

    //경로의 텍스트 파일읽기
    private String ReadTextFile(String path) {
        StringBuffer strBuffer = new StringBuffer();
        try {
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line + "\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return strBuffer.toString();
    }

}
