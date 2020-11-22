package com.example.computervisionandstt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class ImageAndTextView extends AppCompatActivity {

    private TextView savedText;

    private int degree=0;
    private String path;
    SubsamplingScaleImageView savedImage;

    private MediaPlayer mMediaPlayer;
    private int mAudioDuration;
    private int starting=0;
    public boolean mPlayAndCancelCheck =true, mIsPause=false;
    private SeekBar mAudioSeekBar;

    // 시작 위치를 저장을 위한 변수
    private float mLastMotionX = 0;
    private float mLastMotionY = 0;
    // 마우스 move 로 일정범위 벗어나면 취소하기 위한 값
    private int mTouchSlop;

    // long click을 위한 변수들
    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
    private int currentHeight = 0;

    private Handler mHandler = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_and_text_view);

        savedImage =(SubsamplingScaleImageView) findViewById(R.id.savedImage);
        ImageView reSizeHeight = findViewById(R.id.reSizeHeight);
        savedText = findViewById(R.id.savedText);
        ImageView ttsButton = findViewById(R.id.ttsStart);
        ImageView roateButton=findViewById(R.id.imageRotate);
        LinearLayout linearView = findViewById(R.id.linearView);
        ImageView settingImage=findViewById(R.id.settingImage);

        //스크롤
        savedText.setMovementMethod(new ScrollingMovementMethod());

        Intent intent = getIntent();
        path = intent.getStringExtra("paths");
        if (path != null) {
            File imgFile = new  File(path + "/" + "image.jpg");
            if(imgFile.exists()){
                Bitmap mBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                savedImage.setImage(ImageSource.bitmap(rotateImage(BitmapFactory.decodeFile(path + "/" + "image.jpg"),degree)));
            }

            savedText.setText(ReadTextFile(path + "/" + "TTStext.txt"));
        } else {
            Log.d("pathing", "nope");
        }


        mAudioSeekBar = (SeekBar) findViewById(R.id.AudioSeekBar) ;
        ttsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mIWantPlayAudio = path +"/"+ "ttsAudio.3gp";
                PlayAndCancel(mIWantPlayAudio);
                if(mPlayAndCancelCheck ==true) {
                    ttsButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_play_circle_filled_24, getApplicationContext().getTheme()));
                }
                else{
                    ttsButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_pause_circle_filled_24, getApplicationContext().getTheme()));
                }
            }
        });


        roateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degree-=90;
                if (path != null) {
                    File mfile = new File(path + "/" + "image.jpg");
                    if (mfile.exists()) {
                        savedImage.setImage(ImageSource.bitmap(rotateImage(BitmapFactory.decodeFile(path + "/" + "image.jpg"),degree)));
                    }
                }
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

                        currentHeight = savedImage.getHeight() + (int)y;
                        if (currentHeight < linearView.getHeight() - 150) {
                            savedImage.getLayoutParams().height = currentHeight;
                            savedImage.requestLayout();

//                            //SubsamplingScaleImageView가 이미지 자체가 줄어들지 않은 버그가 있음
                           if(currentHeight%3==0 && y<0)
                                //savedImage.setImageBitmap(rotateImage(BitmapFactory.decodeFile(path + "/" + "image.jpg"),0));
                               if (path != null) {
                                   File mfile = new File(path + "/" + "image.jpg");
                                   if (mfile.exists()) {
                                       savedImage.setImage(ImageSource.bitmap(rotateImage(BitmapFactory.decodeFile(path + "/" + "image.jpg"),degree)));
                                  }
                               }



                            Log.d("currentHeight",currentHeight+"");

                            savedText.getLayoutParams().height = savedText.getHeight() + (int) y;
                            savedText.requestLayout();
                        }


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


    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
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


    private Bitmap rotateImage(Bitmap bitmap,float degree){
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    //image클릭시
    private void PlayAndCancel(String path){
        if(starting==0){
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
            starting=1;
        }
        if(mPlayAndCancelCheck ==true && starting==1) {

            mAudioDuration = mMediaPlayer.getDuration();
            mAudioSeekBar.setMax(mAudioDuration);
            mAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser)
                        mMediaPlayer.seekTo(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            mMediaPlayer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(mMediaPlayer.isPlaying()){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mAudioSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
                }
            }).start();
            mPlayAndCancelCheck =false;
        }
        else if(mPlayAndCancelCheck ==false && starting==1){
            Log.e("녹음파일 재생 중지","중지");
            if (mMediaPlayer != null) {
                mIsPause =true;
                mMediaPlayer.pause();
            }
            mPlayAndCancelCheck =true;
        }
    }
}