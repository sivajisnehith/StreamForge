package com.Opsfusionn.StreamForge.dto;

public class UploadResponse {
    private String fileName;
    private String message;

    public UploadResponse(String filename, String message) {
        this.fileName = filename;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

}
