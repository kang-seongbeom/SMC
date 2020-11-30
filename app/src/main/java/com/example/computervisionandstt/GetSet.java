package com.example.computervisionandstt;

public class GetSet {
    //파일 저장을 위한 getset
    String fileName,Contents;
    //recyclerview를 위한 getset
    String name,date;
    private String mCategory;

    private int mChecked;

    public GetSet(){
    }

    public GetSet(String category,String name,String date){
        this.mCategory=category;
        this.name=name;
        this.date=date;
        this.mChecked = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContents() {
        return Contents;
    }

    public void setContents(String contents) {
        Contents = contents;
    }

    public String getmCategory() {
        return mCategory;
    }

    public void setmCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public int getChecked() { return mChecked; }

    public void setChecked(int checked) { this.mChecked = checked; }
}
