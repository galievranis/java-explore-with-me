package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.dto.CommentDto;
import ru.practicum.comments.model.dto.NewCommentDto;
import ru.practicum.comments.model.dto.UpdateCommentRequest;
import ru.practicum.comments.model.entity.Comment;
import ru.practicum.comments.model.entity.CommentDislike;
import ru.practicum.comments.model.entity.CommentLike;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.comments.repository.DislikeRepository;
import ru.practicum.comments.repository.LikeRepository;
import ru.practicum.events.model.entity.Event;
import ru.practicum.events.service.EventService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationRequestException;
import ru.practicum.users.model.entity.User;
import ru.practicum.users.service.UserService;
import ru.practicum.util.pageable.OffsetPageRequest;
import ru.practicum.util.validation.SizeValidator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final EventService eventService;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final LikeRepository likesRepository;
    private final DislikeRepository dislikeRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userService.getUserModelById(userId);
        Event event = eventService.getEventModelById(eventId);

        Comment comment = CommentMapper.toComment(newCommentDto);

        comment.setEvent(event);
        comment.setAuthor(user);

        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        commentDto.setLikesCount(likesRepository.countAllByCommentId(comment.getId()));
        commentDto.setDislikesCount(dislikeRepository.countAllByCommentId(comment.getId()));

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void addLikeToComment(Long userId, Long commentId) {
        Comment comment = getCommentModelById(commentId);
        User user = userService.getUserModelById(userId);

        if (comment.getAuthor().equals(user)) {
            throw new ConflictException("Comment author can't like his own comment");
        }

        if (likesRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            throw new ConflictException("User already liked this comment");
        }

        if (dislikeRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            deleteDislike(userId, commentId);
        }

        CommentLike commentLike = CommentLike.builder()
                .commentId(commentId)
                .userId(user.getId())
                .createdOn(LocalDateTime.now())
                .build();

        likesRepository.save(commentLike);
    }

    @Override
    @Transactional
    public void addDislikeToComment(Long userId, Long commentId) {
        Comment comment = getCommentModelById(commentId);
        User user = userService.getUserModelById(userId);

        if (comment.getAuthor().equals(user)) {
            throw new ConflictException("Comment author can't dislike his own comment");
        }

        if (dislikeRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            throw new ConflictException("User already disliked this comment");
        }

        if (likesRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            deleteLike(userId, commentId);
        }

        CommentDislike commentDislike = CommentDislike.builder()
                .commentId(commentId)
                .userId(user.getId())
                .createdOn(LocalDateTime.now())
                .build();

        dislikeRepository.save(commentDislike);
    }

    @Override
    @Transactional
    public void deleteLike(Long userId, Long commentId) {
        commentExists(commentId);
        userService.userExists(userId);
        CommentLike like = getLikeByCommentIdAndUserId(commentId, userId);

        log.info("Removing like from comment with id={}", commentId);
        likesRepository.deleteById(like.getId());
    }

    @Override
    @Transactional
    public void deleteDislike(Long userId, Long commentId) {
        commentExists(commentId);
        userService.userExists(userId);
        CommentDislike dislike = getDislikeByCommentIdAndUserId(commentId, userId);

        log.info("Removing dislike from comment with id={}", commentId);
        dislikeRepository.deleteById(dislike.getId());
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateCommentRequest) {
        Comment comment = getCommentModelById(commentId);
        User user = userService.getUserModelById(userId);

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Only comment author can edit comment");
        }

        log.info("Updating comment with body={}", updateCommentRequest.toString());
        if (updateCommentRequest.getCommentText() != null && !updateCommentRequest.getCommentText().isBlank()) {
            comment.setCommentText(updateCommentRequest.getCommentText());
        }

        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        commentDto.setLikesCount(likesRepository.countAllByCommentId(commentId));
        commentDto.setDislikesCount(dislikeRepository.countAllByCommentId(commentId));

        return commentDto;
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = getCommentModelById(commentId);

        log.info("Getting comment with id={}", commentId);
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        commentDto.setLikesCount(likesRepository.countAllByCommentId(commentId));
        commentDto.setDislikesCount(dislikeRepository.countAllByCommentId(commentId));
        return commentDto;
    }

    @Override
    public List<CommentDto> getAllCommentsByEventId(Long eventId, Integer from, Integer size) {
        SizeValidator.validateSize(size);
        Pageable pageable = OffsetPageRequest.of(from, size);
        eventService.eventExists(eventId);

        log.info("Getting all comments by event with id={}", eventId);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);
        List<CommentDto> commentDtos = CommentMapper.toCommentDto(comments);

        // Getting all comments ids
        List<Long> commentIds = commentDtos.stream()
                .map(CommentDto::getId)
                .collect(Collectors.toList());

        // Getting all likes for comment
        Map<Long, List<CommentLike>> commentLikesMap = likesRepository.findAllByCommentIdIn(commentIds)
                .stream()
                .collect(Collectors.groupingBy(CommentLike::getCommentId));

        // Getting all dislikes for comment
        Map<Long, List<CommentDislike>> commentDislikesMap = dislikeRepository.findAllByCommentIdIn(commentIds)
                .stream()
                .collect(Collectors.groupingBy(CommentDislike::getCommentId));

        // Setting likes and dislikes count to comments
        for (CommentDto commentDto : commentDtos) {
            Long commentId = commentDto.getId();

            List<CommentLike> commentLikes = commentLikesMap.getOrDefault(commentId, Collections.emptyList());
            Integer likesCount = commentLikes.size();

            List<CommentDislike> commentDislikes = commentDislikesMap.getOrDefault(commentId, Collections.emptyList());
            Integer dislikesCount = commentDislikes.size();

            commentDto.setLikesCount(likesCount);
            commentDto.setDislikesCount(dislikesCount);
        }

        return commentDtos;
    }

    @Override
    public Long getCommentsCountByEventId(Long eventId) {
        eventService.eventExists(eventId);

        log.info("Getting comments count by event with id={}", eventId);
        return commentRepository.countByEventId(eventId);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = getCommentModelById(commentId);
        User user = userService.getUserModelById(userId);

        if (!comment.getAuthor().equals(user)) {
            throw new ValidationRequestException("Only author can delete his own comment.");
        }

        log.info("Deleting comment with id={} by user with id={}", commentId, userId);
        likesRepository.deleteAllByCommentId(commentId);
        dislikeRepository.deleteAllByCommentId(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        commentExists(commentId);

        log.info("Deleting comment with id={}", commentId);

        likesRepository.deleteAllByCommentId(commentId);
        dislikeRepository.deleteAllByCommentId(commentId);
        commentRepository.deleteById(commentId);
    }

    private Comment getCommentModelById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Comment", id));
    }

    private void commentExists(Long id) {
        log.info("Checking that comment with id={} exists", id);

        if (!commentRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
    }

    private CommentLike getLikeByCommentIdAndUserId(Long commentId, Long userId) {
        return likesRepository.getByCommentIdAndUserId(commentId, userId).orElseThrow(() ->
                new NotFoundException("Like not found."));
    }

    private CommentDislike getDislikeByCommentIdAndUserId(Long commentId, Long userId) {
        return dislikeRepository.getByCommentIdAndUserId(commentId, userId).orElseThrow(() ->
                new NotFoundException("Dislike not found."));
    }
}
