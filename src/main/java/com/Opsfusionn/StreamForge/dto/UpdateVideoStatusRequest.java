package com.Opsfusionn.StreamForge.dto;

import com.Opsfusionn.StreamForge.model.VideoStatus;

public class UpdateVideoStatusRequest {
    private VideoStatus status;

    public UpdateVideoStatusRequest() {
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public VideoStatus getStatus() {
        return status;
    }

}
