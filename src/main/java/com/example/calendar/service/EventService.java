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
        return eventRepository.findAllByEmail(user.getEmail());
    }

    public Event saveEvent(EventDTO eventDTO) {
        User user = getCurrentUser();
        Event event = copy(eventDTO);
        event.setUser(user);
        return eventRepository.save(event);
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

    public Event copy(EventDTO eventDTO) {
        User user = getCurrentUser();
        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setStart(eventDTO.getStart());
        event.setEnd(eventDTO.getEnd());
        event.setUser(user);
        return eventRepository.save(event);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        String email;

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));
    }
}
