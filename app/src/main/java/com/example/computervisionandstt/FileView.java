package com.example.computervisionandstt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;

public class FileView extends AppCompatActivity  {
    private ArrayList<GetSet> mArrayList;
    private CustomAdapter mAdapter;
    private ArrayList<String> filesNameList = new ArrayList<>();
    private ArrayList<String> filesDateList = new ArrayList<>();
    private ArrayList<GetSet> mVariable = new ArrayList<>();
    private File[] mFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_list);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mArrayList = new ArrayList<>();
        mAdapter = new CustomAdapter(mArrayList);
        mRecyclerView.setAdapter(mAdapter);

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
                Intent intent = new Intent(getApplicationContext(), ImageAndTextView.class);
                intent.putExtra("filesNameList", filesNameList.get(position));
                intent.putExtra("filesDateList", filesDateList.get(position));
                intent.putExtra("paths", mFiles[position].getPath());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        Context context=getApplicationContext();
        String path =  context.getFilesDir()+ "/";
        File directory = new File(path);
        mFiles = directory.listFiles();

        for (int i = 0; i < mFiles.length; i++) {
            String name = mFiles[i].getName();
            String[] result = name.split("#");
            filesNameList.add(result[0]);
            filesDateList.add(result[1]);
            mVariable.add(new GetSet("날짜:" + filesDateList.get(i), "파일이름 : " + filesNameList.get(i)));
            mArrayList.add(mVariable.get(i));
        }
    }

    //뒤로가기버튼 활성화
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //파일 검색 icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_file_menu,menu);
        MenuItem item=menu.findItem(R.id.search_file_icon);
        SearchView searchView= (SearchView) item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return super.onCreateOptionsMenu(menu);
    }



    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private FileView.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final FileView.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }
}
