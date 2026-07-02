package com.Opsfusionn.StreamForge.model;

public enum VideoStatus {
    UPLOADED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED;

    public boolean canTransitionTo(VideoStatus newStatus) {
         switch (this) {
            
            case UPLOADED:
                return newStatus == PENDING;

            case PENDING:
                return newStatus == PROCESSING;

            case PROCESSING:
                return newStatus == COMPLETED ||
                    newStatus == FAILED;

            case COMPLETED:
                return false;

            case FAILED:
                return false;

            default:
                return false;
        }
    }
}
