package com.example.computervisionandstt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

public class FileView extends AppCompatActivity {
    private CustomAdapter mAdapter;
    private ArrayList<String> mCategotyList;
    private ArrayList<GetSet> mVariable = new ArrayList<>();
    private File[] mFiles;

    //checkBox
    private int mModifyFlag;
    private int mChecked;

    private final String sharedPreferenceKey = "saveArrayListToSharedPreference";
    private ArrayList<String> filesCategoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_list);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new CustomAdapter(mVariable);
        mRecyclerView.setAdapter(mAdapter);

        //checkBox관련
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        final CheckBox check_all = findViewById(R.id.check_all);
        mModifyFlag = 0;

        //actionbar
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 찾기 버튼 만들기

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            //파일 클릭 시
            @Override
            public void onClick(View view, int position) {
                //파일값 넘기기
                if (mModifyFlag == 0) {
                    Intent intent = new Intent(getApplicationContext(), ImageAndTextView.class);
                    intent.putExtra("paths", mVariable.get(position).getmFilePath());
                    startActivity(intent);
                }else{
                    mChecked = mVariable.get(position).getChecked();
                    if (mChecked == 0)
                        mVariable.get(position).setChecked(1);
                    else
                        mVariable.get(position).setChecked(0);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                mModifyFlag = 1;
                handleVisible(mModifyFlag);
                mAdapter.notifyDataSetChanged();
            }
        }));

        Context context = getApplicationContext();
        String path = context.getFilesDir() + "/";
        File directory = new File(path);
        mFiles = directory.listFiles();
        Log.d("mFiles", mFiles.toString());
        if (directory.exists()) {
            for (int i = 0; i < (mFiles.length); i++) {
                String name = mFiles[i].getName();
                if(name.equals("generatefid.lock") ||
                        name.equals("PersistedInstallation.W0RFRkFVTFRd+MToyMzc3MTEzNDM4MzM6YW5kcm9pZDo2NDdhYjM4Yzc2YzE4MjFlNTRiZWM4.json")) {
                    continue;
                }else{
                    String[] result = name.split("#");
                    filesCategoryList.add(result[0]);
                    String tmp = result[2];
                    String mDate = tmp.substring(0, 11);
                    String mHour = tmp.substring(11, 13);
                    String mMinute = tmp.substring(13, 15);
                    String mSecond = tmp.substring(15, 17);
                    mVariable.add(new GetSet("카테고리:" + result[0],
                            "파일이름 : " + "[" + result[0] + "]" + result[1],
                            "날짜:" + mDate + mHour + "시" + mMinute + "분" + mSecond + "초",mFiles[i].getPath()));

                }
            }
        }

        Spinner mCategorySpinner = findViewById(R.id.categorySpinner);
        mCategotyList = new ArrayList<>();
        Context mContext = getApplicationContext();
        mCategotyList = getStringArrayPref(mContext, sharedPreferenceKey);
        ArrayAdapter<String> mSpinnerAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.support_simple_spinner_dropdown_item, mCategotyList);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(mSpinnerAdapter);

        //카테고리를 선택 했을 경우 해당 카테고리에 속한 파일들만 보일 수 있도록 함
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mCategorySpinner.getSelectedItem().toString().equalsIgnoreCase("기본 카테고리")) {
                    fileterCategoty(mCategorySpinner.getItemAtPosition(position).toString());
                } else {
                    fileterCategoty("");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //'+'버튼을 눌렀을 시 카테고리를 추가 할 수 있도록 함
        ImageView addCategoryOfFileView = findViewById(R.id.addCategoryOfFileView);
        addCategoryOfFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mAddCategoryAlertofFileView = new AlertDialog.Builder(FileView.this);
                EditText mAddCategoryEditTextofFileView = new EditText(FileView.this);
                mAddCategoryAlertofFileView.setMessage("카테고리 이름");
                mAddCategoryAlertofFileView.setView(mAddCategoryEditTextofFileView);
                mAddCategoryAlertofFileView.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //카테고리 배열에 카테고리를 불러옴
                        Context mContext = getApplicationContext();
                        mCategotyList.clear();
                        mCategotyList = getStringArrayPref(mContext, sharedPreferenceKey);
                        mCategotyList.add(mAddCategoryEditTextofFileView.getText().toString());
                        setStringArrayPref(mContext, sharedPreferenceKey, mCategotyList);

                        //카테고리 생성시 스피너가 클릭되지 않는 버그가 있어서 강제로 스피너 refresh
                        ArrayAdapter<String> mCategoryArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                R.layout.support_simple_spinner_dropdown_item, mCategotyList);
                        mCategoryArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                        mCategorySpinner.setAdapter(mCategoryArrayAdapter);
                    }
                });
                mAddCategoryAlertofFileView.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("categoty", "취소");
                    }
                });
                AlertDialog mAddCategoryAlertDialogofFileView = mAddCategoryAlertofFileView.create();
                mAddCategoryAlertDialogofFileView.show();
            }
        });

        ////'-'버튼을 눌렀을 시 카테고리를 삭제 할 수 있도록 함
        ImageView subCategoryOfFileView = findViewById(R.id.subCategoryOfFileView);
        subCategoryOfFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDeleteCategoryAlertofFileView = new AlertDialog.Builder(FileView.this);
                EditText mDeleteCategoryEditTextofFileView = new EditText(FileView.this);
                mDeleteCategoryAlertofFileView.setMessage("카테고리 삭제");
                mDeleteCategoryAlertofFileView.setView(mDeleteCategoryEditTextofFileView);
                mDeleteCategoryAlertofFileView.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //카테고리 배열에 정보를 불러오고
                        //삭제할 카테고리 이름의 정보를 받아 일치하면 삭제
                        String mDeleteCategoty = mDeleteCategoryEditTextofFileView.getText().toString();
                        ArrayList<String> mGetCategory = new ArrayList<>();
                        mGetCategory = getStringArrayPref(mContext, sharedPreferenceKey);
                        for (int i = 0; i < mGetCategory.size(); i++) {
                            if (mDeleteCategoty.equals(mGetCategory.get(i))) {
                                if ((mGetCategory.get(i)).equals("기본 카테고리")) {
                                    Toast.makeText(getApplicationContext(), "기본 카테고리는 삭제 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                mGetCategory.remove(i);
                                Toast.makeText(getApplicationContext(), "카테고리 삭제 성공!!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            if (i == (mGetCategory.size() - 1)) {
                                Toast.makeText(getApplicationContext(), "카테고리가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //해당 삭제 정보를 sharedpreference에 저장
                        setStringArrayPref(mContext, sharedPreferenceKey, mGetCategory);
                        ArrayAdapter<String> mCategoryArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                R.layout.support_simple_spinner_dropdown_item, mGetCategory);
                        mCategoryArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                        mCategorySpinner.setAdapter(mCategoryArrayAdapter);

                    }
                });
                mDeleteCategoryAlertofFileView.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog mDeleteCategoryAlertDialogofFileView = mDeleteCategoryAlertofFileView.create();
                mDeleteCategoryAlertDialogofFileView.show();
            }
        });


        //bottomnavigationview의 아이콘을 선택 했을때 기능 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    //삭제
                    case R.id.delete_tab: {
                        int size=mVariable.size();
                        for (int pos=0;pos<size;pos++){
                            if(mVariable.get(pos).getChecked()==1){
                                setDiretoryEmpty(context.getFilesDir().toString(),mVariable.get(pos).getmFilePath());
                            }
                        }
                        mModifyFlag = 0;
                        handleVisible(mModifyFlag);
                        mAdapter.notifyDataSetChanged();

                        //notifycation이 안먹혀서 임시방편으로 화면 초기화
                        Intent mRestartIntent = getIntent();
                        finish();
                        startActivity(mRestartIntent);

                        return true;
                    }

                    //하단 바 내리기
                    case R.id.close_tab: {
                        mModifyFlag = 0;
                        handleVisible(mModifyFlag);
                        mAdapter.notifyDataSetChanged();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        //전체선택
        check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checked;
                int count = mAdapter.getItemCount();
                if (check_all.isChecked()) checked=1;
                else checked=0;
                for (int pos=0;pos<count;pos++){
                    mVariable.get(pos).setChecked(checked);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    //뒤로가기버튼 활성화
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //하단 바 표시 여부 modify_flag=0 Gone, modify_flag=1 Visible
    public void handleBottomNavVisible(int modify_flag) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (modify_flag == 1) bottomNavigationView.setVisibility(View.VISIBLE);
        else bottomNavigationView.setVisibility(View.GONE);
    }

    //하단 바 표시 여부 어댑터에 전달
    public void handleCheckBoxVisible(int n) {
        mAdapter.checkBoxVisibility(n);
    }

    //전체선택 표시 여부
    public void handleCheckedAllVisible(int modify_flag) {
        CheckBox check_all = findViewById(R.id.check_all);
        if(modify_flag==0) check_all.setVisibility(View.GONE);
        else check_all.setVisibility(View.VISIBLE);
    }

    public void handleVisible(int modify_flag) {
        handleCheckBoxVisible(modify_flag);
        handleCheckedAllVisible(modify_flag);
        handleBottomNavVisible(modify_flag);
    }

    //파일 검색 icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_file_menu, menu);
        MenuItem item = menu.findItem(R.id.search_file_icon);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("파일 검색");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    fileterFile(newText);
                } else {
                    fileterFile("");
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void fileterCategoty(String text) {
        ArrayList<GetSet> mFilteredList = new ArrayList<>();
        for (GetSet item : mVariable) {
            if (item.getmCategory().toLowerCase().contains(text)) {
                mFilteredList.add(item);
            }
        }
        mAdapter.filterList(mFilteredList);
    }

    private void fileterFile(String text) {
        ArrayList<GetSet> mFilteredList = new ArrayList<>();
        for (GetSet item : mVariable) {
            if (item.getName().toLowerCase().contains(text)) {
                mFilteredList.add(item);
            }
        }
        mAdapter.filterList(mFilteredList);
    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
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

    public int setDiretoryEmpty(String highPath,String path){
        File dir = new File(path);
        if(dir.exists()) {
            if((path.equals(highPath+"generatefid.lock")) ||
                    (path.equals(highPath+"PersistedInstallation.W0RFRkFVTFRd+MToyMzc3MTEzNDM4MzM6YW5kcm9pZDo2NDdhYjM4Yzc2YzE4MjFlNTRiZWM4.json"))){
                return 0;
            }else{
                File image = new File(path + "/" + "image.jpg");
                File text = new File(path + "/" + "TTStext.txt");
                text.delete();
                image.delete();
                dir.delete();
                return 1;
            }
        }
        return -1;
    }
}
