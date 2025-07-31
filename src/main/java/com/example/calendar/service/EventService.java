package com.example.calendar.service;

import com.example.calendar.DTO.EventDTO;
import com.example.calendar.model.Event;
import com.example.calendar.model.User;
import com.example.calendar.repository.EventRepository;
import com.example.calendar.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<Event> getCurrentUserEvents(){
        User user = getCurrentUser();
        return eventRepository.findAllByUserEmail(user.getEmail());
    }

    public EventDTO saveEvent(EventDTO eventDTO) {
        User user = getCurrentUser();

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setStartTime(eventDTO.getStart());
        event.setEndTime(eventDTO.getEnd());
        event.setUser(user);

        System.out.println("Saving event: " + event);
        return convertEventToDTO(eventRepository.save(event));
    }

    public EventDTO updateEvent(EventDTO eventDTO) {
        Event event = eventRepository.findById(eventDTO.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Event not found"));
        event.setTitle(eventDTO.getTitle());
        event.setStartTime(eventDTO.getStart());
        event.setEndTime(eventDTO.getEnd());
        event.setAllDay(eventDTO.isAllDay());
        return convertEventToDTO(eventRepository.save(event));
    }

    private EventDTO convertEventToDTO(Event event) {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(event.getId());
        eventDTO.setTitle(event.getTitle());
        eventDTO.setStart(event.getStartTime());
        eventDTO.setEnd(event.getEndTime());
        eventDTO.setAllDay(event.isAllDay());
        return eventDTO;
    }

    public void deleteEvent(Long id) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Событие с ID " + id + " не найдено"));
        if (!event.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Вы не можете удалить чужое событие");//надо будет переделать на AccessDeniedException, но пока там ошибка
        }
        eventRepository.deleteById(id);
    }



    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        String name;
        if (authentication.getPrincipal() instanceof UserDetails userDetails){
            name = userDetails.getUsername();
        } else {
            name = authentication.getName();
        }

        Optional<User> userOptional = userRepository.findByUserName(name);
        return userOptional.
                orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем " + name + " не найден"));
    }
}
