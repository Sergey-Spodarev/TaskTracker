package com.example.calendar.controller;

import com.example.calendar.DTO.CalendarEventDto;
import com.example.calendar.DTO.EventDTO;
import com.example.calendar.model.Event;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.service.EventService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<EventDTO> addEvent(@RequestBody EventDTO eventDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.saveEvent(eventDTO));
    }

    @GetMapping("/get")
    public List<EventDTO> getEvents() {
        return eventService.getCurrentUserEvents();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestBody EventDTO eventDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.updateEvent(eventDTO));
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
