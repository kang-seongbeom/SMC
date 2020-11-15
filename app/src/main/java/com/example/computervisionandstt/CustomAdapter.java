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

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView mName;
        protected TextView mDate;

        public CustomViewHolder(View view) {
            super(view);
            this.mName = (TextView) view.findViewById(R.id.R_date);
            this.mDate = (TextView) view.findViewById(R.id.R_name);
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

        viewholder.mName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.mDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        viewholder.mName.setGravity(Gravity.CENTER);
        viewholder.mDate.setGravity(Gravity.CENTER);
        viewholder.mName.setText(mList.get(position).getName());
        viewholder.mDate.setText(mList.get(position).getDate());

    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
    //modify_flag 외부 제어 함수


    //클릭 & 해제 배경색 전환
    public void setItemBackground(@NonNull CustomViewHolder viewholder, String colorString){
        viewholder.mName.setBackgroundColor(Color.parseColor(colorString));
        viewholder.mDate.setBackgroundColor(Color.parseColor(colorString));
    }
}
