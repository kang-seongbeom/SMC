package com.example.computervisionandstt;


import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder>{
    private ArrayList<GetSet> mList;
    private int mModifyFlag=0; //하단바 표시여부에 따라 체크박스 표시 flag

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView mName;
        protected TextView mDate;
        protected CheckBox mCheckBox;

        public CustomViewHolder(View view) {
            super(view);
            this.mName = (TextView) view.findViewById(R.id.R_name);
            this.mDate = (TextView) view.findViewById(R.id.R_date);
            this.mCheckBox=(CheckBox) view.findViewById(R.id.file_checkbox);
        }
    }

    public CustomAdapter(){
        mList=new ArrayList<>();
    }

    public CustomAdapter(ArrayList<GetSet> list) {
        this.mList = list;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_list, viewGroup, false);
        CustomViewHolder mViewHolder = new CustomViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        viewholder.mName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        viewholder.mDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.mName.setGravity(Gravity.CENTER);
        viewholder.mDate.setGravity(Gravity.CENTER);
        viewholder.mName.setText(mList.get(position).getName());
        viewholder.mDate.setText(mList.get(position).getDate());

        //항목 클릭 시 체크, 배경색 바꿈
        if (mList.get(position).getChecked()==1) {
            viewholder.mCheckBox.setChecked(true);

        }
        else {
            viewholder.mCheckBox.setChecked(false);

        }

        //하단바 표시 flag에 따라 checkBox 표시
        if (mModifyFlag==0) {
            viewholder.mCheckBox.setVisibility(View.GONE);

        }
        else {
            viewholder.mCheckBox.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
    //modify_flag 외부 제어 함수
    public void checkBoxVisibility(int modify_flag){ this.mModifyFlag = modify_flag; }


    //클릭 & 해제 배경색 전환
    public void setItemBackground(@NonNull CustomViewHolder viewholder, String colorString){
        viewholder.mCheckBox.setBackgroundColor(Color.parseColor(colorString));
        viewholder.mName.setBackgroundColor(Color.parseColor(colorString));
        viewholder.mDate.setBackgroundColor(Color.parseColor(colorString));
    }

    public void filterList(ArrayList<GetSet> filteredList){
        mList=filteredList;
        notifyDataSetChanged();
    }
}
