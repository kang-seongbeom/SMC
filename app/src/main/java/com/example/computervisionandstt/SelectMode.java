package com.example.computervisionandstt;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SelectMode extends AppCompatActivity{
    private DrawerLayout mDrawerLayout;
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

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
                }
                return true;
            }
        });

        Button mMode1=findViewById(R.id.selectOCR);
        mMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(SelectMode.this,Ocr.class);
                startActivity(mIntent);
            }
        });

        Button mSelectFile=findViewById(R.id.SelectFile);
        mSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(SelectMode.this,FileView.class);
                startActivity(mIntent);
            }
        });

        backPressCloseHandler = new BackPressCloseHandler(this);
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


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
