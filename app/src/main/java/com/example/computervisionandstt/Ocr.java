package com.example.computervisionandstt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Ocr extends AppCompatActivity {

    private    VisionServiceRestClient visionServiceRestClient;
    private LinearLayout firstConstraintLayout,secondConstraintLayout;
    private SubsamplingScaleImageView ocrImage;
    private TextView ocrImageToText;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_ALBUM = 101;
    //서버에 이미지 업로드를 파일로 하는 경우 해당 cacheFile 로 업로드 요청을 합니다.
    // 흔히 RealPath 라 불리는 경로로 보내면 퍼미션 에러가 나서 업로드 진행이 안됩니다.
    private String cacheFilePath = null;
    private String ocrSubscribeKey ="e6fa0dbec3e84f249798724b60ee8489";
    private String ocrEndPoint ="https://kangvision.cognitiveservices.azure.com/vision/v2.0/";

    //sharedpreference
    private ArrayList<String> mCategoryArrayList;
    private final String sharedPreferenceKey="saveArrayListToSharedPreference";

    //tts pitch and speed
    private int ttsPitch =50;   //0~100
    private int ttsSpeed =100; //0~200

    @Override
    protected void onStart() {
        super.onStart();
        visionServiceRestClient=new VisionServiceRestClient(ocrSubscribeKey, ocrEndPoint);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        ocrImage=(SubsamplingScaleImageView)findViewById(R.id.ocrImage);
        ocrImageToText=findViewById(R.id.ocrImageToText);
        ocrImageToText.setMovementMethod(new ScrollingMovementMethod());

        //버튼
        LinearLayout toFileView=findViewById(R.id.toFileView);
        ImageView settingImage=findViewById(R.id.settingImage);
        ImageView getImageButton=findViewById(R.id.getImageButton);
        ImageView captureButton=findViewById(R.id.captureButton);
        ImageView ocrStartButton=findViewById(R.id.ocrStartButton);
        ImageView ttsStartButton=findViewById(R.id.ttsStartButton);
        ImageView saveButton=findViewById(R.id.saveButton);

        //Linear
        firstConstraintLayout=findViewById(R.id.firstConstraintLayout);
        secondConstraintLayout=findViewById(R.id.secondConstraintLayout);

        //카테고리에 저장된 정보를 받아옴
        //카테고리 최상단은 항상 기본 카테고리
        mCategoryArrayList=new ArrayList<>();
        Context mContext=getApplicationContext();
        mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
        if(mCategoryArrayList.size()==0) mCategoryArrayList.add("tmp");
        mCategoryArrayList.set(0,"기본 카테고리");
        setStringArrayPref(mContext,sharedPreferenceKey,mCategoryArrayList);

        //FileView로 이동
        toFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),FileView.class);
                startActivity(intent);
            }
        });

        //setting
        settingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout settingView = (LinearLayout) vi.inflate(R.layout.setting_seekbar, null);

                final SeekBar setPitchSeekBar=settingView.findViewById(R.id.setPitchSeekBar);
                final SeekBar setSpeechRateSeekBar=settingView.findViewById(R.id.setSpeechRateSeekBar);

                //기본 값
                setPitchSeekBar.setProgress((int) ttsPitch);
                setSpeechRateSeekBar.setProgress((int) ttsSpeed);

                //임시 저장 값
                final int[] tmpPitch = {ttsPitch};
                final int[] tmpSpeed={ttsSpeed};

                setPitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tmpPitch[0]=progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                setSpeechRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        tmpSpeed[0]=progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });


                new AlertDialog.Builder(Ocr.this).setMessage("속도, 음높이 조절").setView(settingView).setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ttsPitch=tmpPitch[0];
                        ttsSpeed=tmpSpeed[0];
                        Log.d("pitch",ttsPitch+"");
                        Log.d("speed",ttsSpeed+"");
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }

        });

        //getImageButton
        getImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cacheDirFileClear();
                onAlbum( REQUEST_ALBUM );

            }
        });

        //captureButton
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cacheDirFileClear();
                requestPermissions( new String[]{ Manifest.permission.CAMERA }, REQUEST_CAMERA );

            }
        });

        //ocrStartButton
        ocrStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cacheFilePath!=null){
                    Bitmap bitmap= BitmapFactory.decodeFile(cacheFilePath);
                    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    final ByteArrayInputStream inputStream=new ByteArrayInputStream(outputStream.toByteArray());
                    Ocr.ImageAnalytics imageAnalytics=new Ocr.ImageAnalytics();
                    imageAnalytics.execute(inputStream);
                }
            }
        });

        //ttsStartButton
        ttsStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTTS(1);
            }
        });

        //saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText saveFileName;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Ocr.this);
                alertDialog.setMessage("파일이름");

                //저장 팝업창은 dialog_spinner을 사용
                View mView=getLayoutInflater().inflate(R.layout.dialog_spinner,null);
                final EditText mSaveFileName=(EditText) mView.findViewById(R.id.saveFileName);
                final ImageView addCategory=(ImageView)mView.findViewById(R.id.addCategory);
                final Spinner dialogSpinner=(Spinner)mView.findViewById(R.id.dialogSpinner);

                //dialog창에 구성요소들 추가
                alertDialog.setView(mSaveFileName);
                alertDialog.setView(addCategory);
                alertDialog.setView(dialogSpinner);
                alertDialog.setView(mView);

                //카테고리 배열에 카테고리를 불러옴
                mCategoryArrayList.clear();
                mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
                ArrayAdapter<String> mArrayAdapter=new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.support_simple_spinner_dropdown_item,mCategoryArrayList);
                mArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                dialogSpinner.setAdapter(mArrayAdapter);

                //'+'버튼을 누르면 카토고리 추가 dialog창뜨게 함
                addCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder mCategoryAlert = new AlertDialog.Builder(Ocr.this);
                        EditText addCategoryEditText=new EditText(Ocr.this);
                        mCategoryAlert.setMessage("카테고리 이름");
                        mCategoryAlert.setView(addCategoryEditText);
                        mCategoryAlert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //카테고리배열에 카테고리정보를 불러옴
                                mCategoryArrayList.clear();
                                mCategoryArrayList=getStringArrayPref(mContext,sharedPreferenceKey);
                                mCategoryArrayList.add(addCategoryEditText.getText().toString());
                                setStringArrayPref(mContext,sharedPreferenceKey,mCategoryArrayList);

                                //카테고리 생성시 스피너가 클릭되지 않는 버그가 있어서 강제로 스피너 refresh
                                ArrayAdapter<String> mCategoryArrayAdapter=new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.support_simple_spinner_dropdown_item,mCategoryArrayList);
                                mCategoryArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                                dialogSpinner.setAdapter(mCategoryArrayAdapter);
                            }
                        });
                        mCategoryAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog mCategoryAlertDialog=mCategoryAlert.create();
                        mCategoryAlertDialog.show();
                    }
                });


                alertDialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveFile checkTypesTask=new SaveFile();
                        String fileName=mSaveFileName.getText().toString();
                        String Contents=ocrImageToText.getText().toString();
                        String mTmpCategory=dialogSpinner.getSelectedItem().toString();
                        GetSet getSet=new GetSet();
                        getSet.setFileName(mTmpCategory+"#"+fileName);
                        getSet.setContents(Contents);
                        checkTypesTask.execute((GetSet) getSet);
                    }
                });

                alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"저장을 취소 했습니다.",Toast.LENGTH_SHORT).show();

                    }
                });
                alertDialog.show();
            }
        });



    }

    //이미지의 텍스트 추출
    public class ImageAnalytics extends AsyncTask<InputStream,String,String> {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Ocr.this);
        AlertDialog alertDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialogBuilder.setMessage("이미지 분석을 진행하고 있습니다.");
            alertDialog=alertDialogBuilder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(InputStream... inputStreams) {

            try{
                Log.d("doInBackground:","doInBackground");
                OCR ocr=visionServiceRestClient.recognizeText(inputStreams[0], LanguageCodes.Korean,true);
                String result =new Gson().toJson(ocr);
                return result;
            }catch (Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            OCR ocr=new Gson().fromJson(s,OCR.class);
            StringBuilder stringBuilder=new StringBuilder();
            for (Region region : ocr.regions) {
                for (Line line : region.lines) {
                    for (Word word : line.words) {
                        stringBuilder.append(word.text + " ");
                    }
                    stringBuilder.append("\n");
                }
                stringBuilder.append("\n\n");
            }
            ocrImageToText.setText(stringBuilder);
            Toast.makeText(getApplicationContext(),"분석완료!!",Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        }
    }

    //파일저장
    private class SaveFile extends AsyncTask<GetSet,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(),"저장완료!!",Toast.LENGTH_SHORT).show();
            Intent mRestartIntent = getIntent();
            finish();
            startActivity(mRestartIntent);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(GetSet... Received) {

            String receivedFileName=Received[0].fileName;
            String receivedContents=Received[0].Contents;

            Context context=getApplicationContext();

            //저장 할 파일 경로
            String createFilePath= context.getFilesDir()+"/"+ receivedFileName+"#"+getCurrentTime();
            String ttsName="TTStext.txt";
            String imageName="image.jpg";
            String ttsFilePath = createFilePath + "/" + ttsName;
            String imageFilePath=createFilePath+"/"+imageName;

            //저장시킬 파일 만들기
            File createFolder=new File(createFilePath);
            if(!createFolder.exists())
                createFolder.mkdirs();

            if(createFolder.exists()) {
                //TTS텍스트 저장
                if (receivedContents != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(ttsFilePath, true);
                        Log.d("receivedContents",receivedContents);
                        BufferedWriter mWriter = new BufferedWriter(new OutputStreamWriter(fos));
                        mWriter.write(receivedContents);
                        mWriter.flush();
                        mWriter.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //image저장
                if(cacheFilePath!=null){
                    FileOutputStream fos=null;
                    try {
                        File tmpImageFile=new File(imageFilePath);
                        tmpImageFile.createNewFile();
                        fos = new FileOutputStream(tmpImageFile);
                        Bitmap bitmap= BitmapFactory.decodeFile(cacheFilePath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //음성성파일 저장
                File voiceFile=new File(createFolder,"ttsAudio.mp3");
                if(textToSpeech==null) {
                    setTTS(0);
                }
                Bundle audioBundleTts = new Bundle();
                audioBundleTts.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"tTs");
                Log.d("audioBundleTts",audioBundleTts.toString());
                textToSpeech.synthesizeToFile((CharSequence)receivedContents,audioBundleTts,voiceFile,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
           }
            return null;
        }
    }


    //flag가 1일때만 speak
    //tts를 실행시키지 않고 저장을 할 때 tts가 null인 경우를 방지
    private void setTTS(int flag){
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS){
                    int result=textToSpeech.setLanguage(Locale.KOREA);
                    if(result==TextToSpeech.LANG_MISSING_DATA || result== TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.d("TTS","언어미지원");
                    else
                        if(flag==1) speackOut();
                }else
                    Log.d("TTS","초기화 실패");
            }
        });
        if(flag==1) speackOut();
    }
    //tts
    private void speackOut(){
        CharSequence charSequence=ocrImageToText.getText();
        if(charSequence!=null){
            float pitch=(float) ((ttsPitch/100.0));
            float speed=(float) ((ttsSpeed/100.0));
            textToSpeech.setPitch(pitch);
            textToSpeech.setSpeechRate(speed);
            Bundle bundleTts = new Bundle();
            bundleTts.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tTs");
            Log.d("audioBundleTts",bundleTts.toString());
            textToSpeech.speak(charSequence,TextToSpeech.QUEUE_FLUSH,bundleTts,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }else
            Toast.makeText(getApplicationContext(),"이미지 분석을 해주세요!",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private String getCurrentTime(){
        SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
        Date mCurDate   = new Date(System.currentTimeMillis());
        String fileDate  = mFormatter.format(mCurDate);
        return fileDate;
    }

    //camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if ( requestCode == REQUEST_CAMERA ) {
            for ( int g : grantResults ) {
                if ( g == PackageManager.PERMISSION_DENIED ) {
                    //권한거부
                    return;
                }
            }
            //임시파일 생성
            File file = createImgCacheFile( );
            cacheFilePath = file.getAbsolutePath( );
            //카메라 호출
            onCamera( REQUEST_CAMERA, file );
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ) {

            AlbumAdd( cacheFilePath );
            Log.d("cacheFilePathCamera",cacheFilePath);

            ocrImage.setImage(ImageSource.bitmap(getBitmapCamera( ocrImage, cacheFilePath )));

            firstConstraintLayout.setVisibility(View.GONE);
            secondConstraintLayout.setVisibility(View.VISIBLE);

        } else if ( requestCode == REQUEST_ALBUM && resultCode == RESULT_OK ) {

            Uri albumUri = data.getData( );
            String fileName = getFileName( albumUri );
            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( albumUri, "r" );
                if ( parcelFileDescriptor == null ) return;
                FileInputStream inputStream = new FileInputStream( parcelFileDescriptor.getFileDescriptor( ) );
                File cacheFile = new File( this.getCacheDir( ), fileName );
                FileOutputStream outputStream = new FileOutputStream( cacheFile );
                IOUtils.copy( inputStream, outputStream );

                cacheFilePath = cacheFile.getAbsolutePath( );
                Log.d("cacheFilePathImage",cacheFilePath);

                ocrImage.setImage(ImageSource.bitmap(getBitmapAlbum( ocrImage, albumUri )));

                firstConstraintLayout.setVisibility(View.GONE);
                secondConstraintLayout.setVisibility(View.VISIBLE);

            } catch ( Exception e ) {
                e.printStackTrace( );
            }

        } else if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_CANCELED ) {
            fileDelete( cacheFilePath );
            cacheFilePath = null;
        }
    }

    //캐시파일 생성
    public File createImgCacheFile( ) {
        File cacheFile = new File( getCacheDir( ), new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.US ).format( new Date( ) ) + ".jpg" );
        return cacheFile;
    }

    //카메라 호출
    public void onCamera( int requestCode, File createTempFile ) {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        if ( intent.resolveActivity( getPackageManager( ) ) != null ) {
            if ( createTempFile != null ) {
                Uri photoURI = FileProvider.getUriForFile( this, getPackageName(), createTempFile );
                intent.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );
                startActivityForResult( intent, requestCode );
            }
        }
    }

    //앨범 호출
    public void onAlbum( int requestCode ) {
        Intent intent = new Intent( Intent.ACTION_PICK );
        intent.setType( MediaStore.Images.Media.CONTENT_TYPE );
        startActivityForResult( intent, requestCode );
    }

    //앨범 저장
    public void AlbumAdd( String cacheFilePath ) {
        if ( cacheFilePath == null ) return;
        BitmapFactory.Options options = new BitmapFactory.Options( );
        ExifInterface exifInterface = null;

        try {
            exifInterface = new ExifInterface( cacheFilePath );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }

        int exifOrientation;
        int exifDegree = 0;

        //사진 회전값 구하기
        if ( exifInterface != null ) {
            exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

            if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                exifDegree = 90;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                exifDegree = 180;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                exifDegree = 270;
            }
        }

        Bitmap bitmap = BitmapFactory.decodeFile( cacheFilePath, options );
        Matrix matrix = new Matrix( );
        matrix.postRotate( exifDegree );

        Bitmap exifBit = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth( ), bitmap.getHeight( ), matrix, true );

        ContentValues values = new ContentValues( );
        //실제 앨범에 저장될 이미지이름
        values.put( MediaStore.Images.Media.DISPLAY_NAME, new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.US ).format( new Date( ) ) + ".jpg" );
        values.put( MediaStore.Images.Media.MIME_TYPE, "image/*" );
        //저장될 경로
        values.put( MediaStore.Images.Media.RELATIVE_PATH, "DCIM/AndroidQ" );
        values.put( MediaStore.Images.Media.ORIENTATION, exifDegree );
        values.put( MediaStore.Images.Media.IS_PENDING, 1 );

        Uri u = MediaStore.Images.Media.getContentUri( MediaStore.VOLUME_EXTERNAL );
        Uri uri = getContentResolver( ).insert( u, values );

        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( uri, "w", null );
            if ( parcelFileDescriptor == null ) return;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
            exifBit.compress( Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream );
            byte[] b = byteArrayOutputStream.toByteArray( );
            InputStream inputStream = new ByteArrayInputStream( b );

            ByteArrayOutputStream buffer = new ByteArrayOutputStream( );
            int bufferSize = 1024;
            byte[] buffers = new byte[ bufferSize ];

            int len = 0;
            while ( ( len = inputStream.read( buffers ) ) != -1 ) {
                buffer.write( buffers, 0, len );
            }

            byte[] bs = buffer.toByteArray( );
            FileOutputStream fileOutputStream = new FileOutputStream( parcelFileDescriptor.getFileDescriptor( ) );
            fileOutputStream.write( bs );
            fileOutputStream.close( );
            inputStream.close( );
            parcelFileDescriptor.close( );

            getContentResolver( ).update( uri, values, null, null );

        } catch ( Exception e ) {
            e.printStackTrace( );
        }

        values.clear( );
        values.put( MediaStore.Images.Media.IS_PENDING, 0 );
        getContentResolver( ).update( uri, values, null, null );
    }

    //이미지뷰에 뿌려질 앨범 비트맵 반환
    public Bitmap getBitmapAlbum( View targetView, Uri uri ) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver( ).openFileDescriptor( uri, "r" );
            if ( parcelFileDescriptor == null ) return null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor( );
            if ( fileDescriptor == null ) return null;

            int targetW = targetView.getWidth( );
            int targetH = targetView.getHeight( );

            BitmapFactory.Options options = new BitmapFactory.Options( );
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            int scaleFactor = Math.min( photoW / targetW, photoH / targetH );
            if ( scaleFactor >= 8 ) {
                options.inSampleSize = 8;
            } else if ( scaleFactor >= 4 ) {
                options.inSampleSize = 4;
            } else {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;

            Bitmap reSizeBit = BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            ExifInterface exifInterface = null;
            try {
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                    exifInterface = new ExifInterface( fileDescriptor );
                }
            } catch ( IOException e ) {
                e.printStackTrace( );
            }

            int exifOrientation;
            int exifDegree = 0;

            //사진 회전값 구하기
            if ( exifInterface != null ) {
                exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

                if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                    exifDegree = 90;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                    exifDegree = 180;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                    exifDegree = 270;
                }
            }

            parcelFileDescriptor.close( );
            Matrix matrix = new Matrix( );
            matrix.postRotate( exifDegree );

            Bitmap reSizeExifBitmap = Bitmap.createBitmap( reSizeBit, 0, 0, reSizeBit.getWidth( ), reSizeBit.getHeight( ), matrix, true );
            return reSizeExifBitmap;

        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }
    }

    //이미지뷰에 뿌려질 카메라 비트맵 반환
    public Bitmap getBitmapCamera( View targetView, String filePath ) {
        int targetW = targetView.getWidth( );
        int targetH = targetView.getHeight( );

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options( );
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile( filePath, bmOptions );

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        double scaleFactor = Math.min( photoW / targetW, photoH / targetH );
        if ( scaleFactor >= 8 ) {
            bmOptions.inSampleSize = 8;
        } else if ( scaleFactor >= 4 ) {
            bmOptions.inSampleSize = 4;
        } else {
            bmOptions.inSampleSize = 2;
        }


        bmOptions.inJustDecodeBounds = false;

        Bitmap originalBitmap = BitmapFactory.decodeFile( filePath, bmOptions );

        ExifInterface exifInterface = null;
        try {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                exifInterface = new ExifInterface( filePath );
            }
        } catch ( IOException e ) {
            e.printStackTrace( );
        }

        int exifOrientation;
        int exifDegree = 0;

        //사진 회전값 구하기
        if ( exifInterface != null ) {
            exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

            if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                exifDegree = 90;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                exifDegree = 180;
            } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                exifDegree = 270;
            }
        }

        Matrix matrix = new Matrix( );
        matrix.postRotate( exifDegree );

        Bitmap reSizeExifBitmap = Bitmap.createBitmap( originalBitmap, 0, 0, originalBitmap.getWidth( ), originalBitmap.getHeight( ), matrix, true );
        return reSizeExifBitmap;

    }

    //앨범에서 선택한 사진이름 가져오기
    public String getFileName( Uri uri ) {
        Cursor cursor = getContentResolver( ).query( uri, null, null, null, null );
        try {
            if ( cursor == null ) return null;
            cursor.moveToFirst( );
            String fileName = cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );
            cursor.close( );
            return fileName;

        } catch ( Exception e ) {
            e.printStackTrace( );
            cursor.close( );
            return null;
        }
    }

    //파일삭제
    public void fileDelete( String filePath ) {
        if ( filePath == null ) return;
        try {
            File f = new File( filePath );
            if ( f.exists( ) ) {
                f.delete( );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
    }

    //실제 앨범경로가 아닌 앱 내에 캐시디렉토리에 존재하는 이미지 캐시파일삭제
    //확장자 .jpg 필터링해서 제거
    public void cacheDirFileClear( ) {
        File cacheDir = new File( getCacheDir( ).getAbsolutePath( ) );
        File[] cacheFiles = cacheDir.listFiles( new FileFilter( ) {
            @Override
            public boolean accept( File pathname ) {
                return pathname.getName( ).endsWith( "jpg" );
            }
        } );
        if ( cacheFiles == null ) return;
        for ( File c : cacheFiles ) {
            fileDelete( c.getAbsolutePath( ) );
        }
    }

    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}