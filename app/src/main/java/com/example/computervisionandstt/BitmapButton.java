package com.example.computervisionandstt;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatButton;

public class BitmapButton extends AppCompatButton {

    int iconNormal = R.drawable.custom_button; //노멀 상태의 비트맵 이미지
    int iconClicked = R.drawable.pressed_custom_button; //클릭 상태의 비트맵 이미지

    public BitmapButton(Context context){
        super(context);
        init(); //객체 생성 후 초기화
    }

    public BitmapButton(Context context, AttributeSet attrs){
        super(context, attrs);
        init();//객체 생성 후 초기화
    }

    public void init(){
        setBackgroundResource(iconNormal); //비트맵 버튼 노말 상태
        setTextColor(Color.WHITE); //텍스트 색상 흰색
        setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction();

        switch(action){
            case MotionEvent.ACTION_DOWN: // 버튼 눌렀을 때는 클릭 상태 비트맵 이미지 설정
                setBackgroundResource(iconClicked);
                break;

            case MotionEvent.ACTION_UP: // 버튼에서 손 떼면 노멀 상태 비트맵 이미지 설정
                setBackgroundResource(iconNormal);
                break;
        }

        invalidate(); //다시 그리기

        return true;
    }
}