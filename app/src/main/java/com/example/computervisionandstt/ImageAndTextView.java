package com.example.computervisionandstt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class ImageAndTextView extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView textView;

    // 시작 위치를 저장을 위한 변수
    private float mLastMotionX = 0;
    private float mLastMotionY = 0;
    // 마우스 move 로 일정범위 벗어나면 취소하기 위한 값
    private int mTouchSlop;

    // long click을 위한 변수들
    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;

    private Handler mHandler = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_and_text_view);

        ImageView imageView=findViewById(R.id.savedImage);
        ImageView reSizeHeight=findViewById(R.id.reSizeHeight);
        textView=findViewById(R.id.savedText);
        Button ttsButton=findViewById(R.id.ttsStart);

        Intent intent=getIntent();
        String path=intent.getStringExtra("paths");
        if(path!=null){
            Log.d("pathing",path);
            Glide.with(this).load(path+"/"+"image.jpg").into(imageView);
            textView.setText(ReadTextFile(path+"/"+"TTStext.txt"));
        }
        else{
            Log.d("pathing","nope");
        }

        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status==TextToSpeech.SUCCESS){
                            int result=textToSpeech.setLanguage(Locale.KOREA);
                            if(result==TextToSpeech.LANG_MISSING_DATA || result== TextToSpeech.LANG_NOT_SUPPORTED)
                                Log.d("TTS","언어미지원");
                            else
                                speackOut();
                        }else
                            Log.d("TTS","초기화 실패");
                    }
                });
                speackOut();
            }
        });

        reSizeHeight.setBackgroundColor(Color.parseColor("#000000"));
        reSizeHeight.setLongClickable(true);
        mHandler = new Handler();
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        reSizeHeight.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        Log.d("CLICK", "ACTION_DOWN");

                        mLastMotionX = event.getX();
                        mLastMotionY = event.getY();   // 시작 위치 저장

                        mHasPerformedLongPress = false;

                        postCheckForLongClick(0);     //  Long click message 설정

                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.d("CLICK", "ACTION_MOVE");

                        final float x = event.getX();
                        final float y = event.getY();
                        final int deltaX = Math.abs((int) (mLastMotionX - x));
                        final int deltaY = Math.abs((int) (mLastMotionY - y));
                        Log.e("CLICK",deltaX+"");
                        Log.e("CLICK",deltaY+"");

                        // 일정 범위 벗어나면  취소함
                        if (deltaX >= mTouchSlop || deltaY >= mTouchSlop) {
                            if (!mHasPerformedLongPress) {
                                // This is a tap, so remove the longpress check
                                removeLongPressCallback();
                            }
                        }

                        break;

                    case MotionEvent.ACTION_CANCEL:
                        if (!mHasPerformedLongPress) {
                            // This is a tap, so remove the longpress check
                            removeLongPressCallback();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d("CLICK", "ACTION_UP");

                        if (!mHasPerformedLongPress) {
                            // Long Click을 처리되지 않았으면 제거함.
                            removeLongPressCallback();

                            // Short Click 처리 루틴을 여기에 넣으면 됩니다.
                            performOneClick();

                        }

                        break;

                    default:
                        break;
                }

                return false;
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    //tts
    private void speackOut(){
        CharSequence charSequence=textView.getText();
        if(charSequence!=null){
            textToSpeech.setPitch((float)0.6);
            textToSpeech.setSpeechRate((float)1.0);
            textToSpeech.speak(charSequence,TextToSpeech.QUEUE_FLUSH,null,"id1");
        }else
            Toast.makeText(getApplicationContext(),"이미지 분석을 해주세요!",Toast.LENGTH_SHORT).show();
    }


    // Long Click을 처리할  Runnable 입니다.
    class CheckForLongPress implements Runnable {

        public void run() {
            if (performLongClick()) {
                mHasPerformedLongPress = true;
            }
        }
    }

    // Long Click 처리 설정을 위한 함수
    private void postCheckForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }

        mHandler.postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - delayOffset);
        // 여기서  시스템의  getLongPressTimeout() 후에 message 수행하게 합니다.
        // 추가 delay가 필요한 경우를 위해서  파라미터로 조절가능하게 합니다.
    }


    /**
     * Remove the longpress detection timer.
     * 중간에 취소하는 용도입니다.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
        }
    }

    public boolean performLongClick() {
        //  실제 Long Click 처리하는 부분을 여기 둡니다.
        Log.d("CLICK", "Long Click OK");
        //Toast.makeText(ImageAndTextView.this, "Long Click OK!!", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void performOneClick() {
        Log.d("CLICK", "One Click OK");
        //Toast.makeText(ImageAndTextView.this, "One Click OK!!", Toast.LENGTH_SHORT).show();
    }
}