package com.example.calendar.controller;

import com.example.calendar.DTO.CalendarEventDto;
import com.example.calendar.DTO.EventDTO;
import com.example.calendar.model.Event;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addEvent(@RequestBody EventDTO eventDTO) {
        eventService.saveEvent(eventDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    public List<CalendarEventDto> getEvents() {
        return eventService.getCurrentUserEvents().stream()
                .map(event -> {
                    CalendarEventDto dto = new CalendarEventDto();
                    dto.setId(event.getId().toString());
                    dto.setTitle(event.getTitle());
                    dto.setStart(event.getStartTime());
                    dto.setEnd(event.getEndTime());
                    dto.setAllDay(event.isAllDay());
                    return dto;
                })
                .toList();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestBody EventDTO eventDTO) {
        eventService.saveEvent(eventDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
