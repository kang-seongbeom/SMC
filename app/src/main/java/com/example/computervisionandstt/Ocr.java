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
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;

import org.apache.commons.io.IOUtils;

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
import java.util.Date;
import java.util.Locale;

public class Ocr extends AppCompatActivity {

    private VisionServiceRestClient visionServiceRestClient;
    private ImageView ocrImage;
    private TextView ocrImageToText;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_ALBUM = 101;
    //서버에 이미지 업로드를 파일로 하는 경우 해당 cacheFile 로 업로드 요청을 합니다.
    // 흔히 RealPath 라 불리는 경로로 보내면 퍼미션 에러가 나서 업로드 진행이 안됩니다.
    private String cacheFilePath = null;
    private String ocrSubscribeKey ="e6fa0dbec3e84f249798724b60ee8489";
    private String ocrEndPoint ="https://kangvision.cognitiveservices.azure.com/vision/v2.0/";


    @Override
    protected void onStart() {
        super.onStart();
        visionServiceRestClient=new VisionServiceRestClient(ocrSubscribeKey, ocrEndPoint);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        ocrImage=findViewById(R.id.ocrImage);
        ocrImageToText=findViewById(R.id.ocrImageToText);
        ocrImageToText.setMovementMethod(new ScrollingMovementMethod());

        //버튼
        Button getImageButton=findViewById(R.id.getImageButton);
        Button captureButton=findViewById(R.id.captureButton);
        Button ocrStartButton=findViewById(R.id.ocrStartButton);
        Button ttsStartButton=findViewById(R.id.ttsStartButton);
        Button saveButton=findViewById(R.id.saveButton);

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

        //saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText saveFileName;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Ocr.this);
                alertDialog.setMessage("파일이름");

                saveFileName=new EditText(Ocr.this);
                alertDialog.setView(saveFileName);

                alertDialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveFile checkTypesTask=new SaveFile();
                        String fileName=saveFileName.getText().toString();
                        String Contents=ocrImageToText.toString();
                        GetSet getSet=new GetSet();
                        getSet.setFileName(fileName);
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
            onBackPressed();
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
            }
            return null;
        }
    }

    //tts
    private void speackOut(){
        CharSequence charSequence=ocrImageToText.getText();
        if(charSequence!=null){
            textToSpeech.setPitch((float)0.6);
            textToSpeech.setSpeechRate((float)1.0);
            textToSpeech.speak(charSequence,TextToSpeech.QUEUE_FLUSH,null,"id1");
        }else
            Toast.makeText(getApplicationContext(),"이미지 분석을 해주세요!",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech!=null){
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
            ocrImage.setImageBitmap( getBitmapCamera( ocrImage, cacheFilePath ) );

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

                ocrImage.setImageBitmap( getBitmapAlbum( ocrImage, albumUri ) );

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
}