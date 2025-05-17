package com.example.calendar.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarEventDto {
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;
}
