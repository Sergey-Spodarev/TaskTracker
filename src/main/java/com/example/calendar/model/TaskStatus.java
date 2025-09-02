package com.example.calendar.model;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    DONE;

    public static boolean canTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        return switch (oldStatus){
            case TODO -> newStatus == IN_PROGRESS;
            case IN_PROGRESS -> newStatus == REVIEW || newStatus == DONE;
            case REVIEW -> newStatus == IN_PROGRESS || newStatus == DONE;
            case DONE -> false;
        };
    }
}
