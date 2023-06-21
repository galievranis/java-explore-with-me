package ru.practicum.requests.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.model.entity.Event;
import ru.practicum.events.service.EventService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.requests.mapper.ParticipationRequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.ParticipationRequestDto;
import ru.practicum.requests.repository.ParticipationRequestRepository;
import ru.practicum.users.model.entity.User;
import ru.practicum.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.enums.EventState.PUBLISHED;
import static ru.practicum.enums.RequestStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@AllArgsConstructor(onConstructor_ = {@Lazy})
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userService.getUserModelById(userId);
        Event event = eventService.getEventModelById(eventId);

        checkBeforeCreate(event, userId);

        log.info("Creating request from user with id={} to event with id={}", userId, eventId);
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(CONFIRMED);
        } else {
            participationRequest.setStatus(PENDING);
        }

        return ParticipationRequestMapper.toRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.userExists(userId);

        log.info("Cancel request with id={}", requestId);
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request", requestId));

        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request", requestId);
        }

        participationRequest.setStatus(CANCELED);
        return ParticipationRequestMapper.toRequestDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUserId(Long userId) {
        userService.userExists(userId);

        log.info("Getting all requests by user with id={}", userId);
        List<ParticipationRequest> requests = participationRequestRepository.findAllByRequesterId(userId);
        return ParticipationRequestMapper.toRequestDto(requests);
    }

    private void checkBeforeCreate(Event event, Long userId) {
        if (participationRequestRepository.findByRequesterIdAndEventId(userId, event.getId()) != null) {
            throw new ConflictException("You can't add a repeat request.");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The initiator of the event cannot add a request to participate in his event");
        }

        if (event.getState() != PUBLISHED) {
            throw new ConflictException("You can't participate in an unpublished event");
        }

        Long confirmedCount = participationRequestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Reach participant limit.");
        }
    }
}
