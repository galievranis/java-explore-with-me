package ru.practicum.events.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.model.dto.*;
import ru.practicum.events.service.EventService;
import ru.practicum.requests.model.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
public class EventControllerPrivate {

    private final EventService eventService;

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEventsByUserId(@PathVariable Long userId,
                                                    @Valid @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                    @Valid @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET at '/users/{}/events' to get all events by user with id={}", userId, userId);
        return eventService.getAllEventsByUserId(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("POST at '/users/{}/events' to create new event with body={} " +
                "from user with id={}", userId, newEventDto.toString(), userId);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        log.info("GET at '/users/{}/events/{}' to get event by id={}", userId, eventId, eventId);
        return eventService.getEventById(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("PATCH at '/users/{}/events/{}' to update event with body={}",
                userId, eventId, updateEventUserRequest.toString());
        return eventService.updateEventByUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@Valid @Positive @PathVariable Long userId,
                                                     @Valid @Positive @PathVariable Long eventId) {
        log.info("GET at '/users/{}/events/{}/requests' to get requests by event with id={}", userId, eventId, eventId);
        return eventService.getRequests(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@Valid @Positive @PathVariable Long userId,
                                                              @Valid @Positive @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("PATCH at '/users/{}/events/{}/requests' to update request with body={}",
                userId, eventId, eventRequestStatusUpdateRequest.toString());
        return eventService.updateRequestStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
