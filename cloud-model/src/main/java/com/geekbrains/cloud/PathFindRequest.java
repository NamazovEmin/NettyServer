package com.geekbrains.cloud;

public class PathFindRequest implements CloudMessage{
   private String fileName;

    public PathFindRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
