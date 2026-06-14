package com.kepo.service;

import com.kepo.model.Event;
import com.kepo.repository.EventRepository;

import java.util.List;

public class EventService {

    private final EventRepository eventRepo;
    private final UserService userService;

    public EventService(EventRepository eventRepo, UserService userService) {
        this.eventRepo = eventRepo;
        this.userService = userService;
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public Event getEventById(int id) {
        return eventRepo.findById(id);
    }

    public boolean saveEvent(Event ev) {
        boolean res = eventRepo.save(ev);
        if (res && userService.getCurrentUser() != null) {
            String act = ev.getEventId() > 0 ? "UPDATE_EVENT" : "CREATE_EVENT";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Event: " + ev.getName());
        }
        return res;
    }

    public boolean deleteEvent(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_EVENT", "ID: " + id);
        }
        return eventRepo.delete(id);
    }
}
