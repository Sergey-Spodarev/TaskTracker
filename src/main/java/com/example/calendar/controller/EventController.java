package com.example.calendar.controller;

import com.example.calendar.DTO.EventDTO;
import com.example.calendar.model.Event;
import com.example.calendar.service.EventService;
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
    public void addEvent(@RequestBody EventDTO eventDTO) {
        eventService.saveEvent(eventDTO);
    }

    @GetMapping("/get")
    public List<Event> getEvents() {
        return eventService.getCurrentUserEvents();
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
