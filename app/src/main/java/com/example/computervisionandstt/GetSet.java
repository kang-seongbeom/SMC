package com.example.computervisionandstt;

public class GetSet {
    //파일 저장을 위한 getset
    String fileName,Contents;
    //recyclerview를 위한 getset
    String name,date;

    public GetSet(){

    }
    public GetSet(String name,String date){
        this.name=name;
        this.date=date;
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
}
