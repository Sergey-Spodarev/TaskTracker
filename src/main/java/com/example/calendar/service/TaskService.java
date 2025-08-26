package com.example.calendar.service;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.Task;
import com.example.calendar.model.User;
import com.example.calendar.repository.TaskRepository;
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
public class TaskService {
    private final TaskRepository eventRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    //todo переделать чтобы не было постоянных запросов в БД при переходе на новый месяц, возможно сделать запрос на все сабытия и на фронте просто добавлять, сюда кстати можно будет прикрутить noSQL
    public List<TaskDTO> getCurrentUserEvents(){//может это переделать под noSQL ибо мы берём все задачи
        User user = getCurrentUser();
        return eventRepository.findAllByUserEmail(user.getEmail()).stream()
                .map(this::convertEventToDTO)
                .toList();
    }

    public TaskDTO saveEvent(TaskDTO eventDTO) {
        User user = getCurrentUser();

        Task event = new Task();
        event.setTitle(eventDTO.getTitle());
        event.setStartTime(eventDTO.getStart());
        event.setEndTime(eventDTO.getEnd());
        event.setUser(user);

        System.out.println("Saving event: " + event);
        return convertEventToDTO(eventRepository.save(event));
    }

    public TaskDTO updateEvent(TaskDTO eventDTO) {
        Task event = eventRepository.findById(eventDTO.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Event not found"));
        event.setTitle(eventDTO.getTitle());
        event.setStartTime(eventDTO.getStart());
        event.setEndTime(eventDTO.getEnd());
        event.setAllDay(eventDTO.isAllDay());
        return convertEventToDTO(eventRepository.save(event));
    }

    private TaskDTO convertEventToDTO(Task event) {
        TaskDTO eventDTO = new TaskDTO();
        eventDTO.setId(event.getId());
        eventDTO.setTitle(event.getTitle());
        eventDTO.setStart(event.getStartTime());
        eventDTO.setEnd(event.getEndTime());
        eventDTO.setAllDay(event.isAllDay());
        return eventDTO;
    }

    public void deleteEvent(Long id) {
        User user = getCurrentUser();
        Task event = eventRepository.findById(id)
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
