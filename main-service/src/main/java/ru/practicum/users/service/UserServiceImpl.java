package ru.practicum.users.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.dto.NewUserRequest;
import ru.practicum.users.model.dto.UserDto;
import ru.practicum.users.model.entity.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.util.pageable.OffsetPageRequest;
import ru.practicum.util.validation.SizeValidator;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Creating user with body={}", newUserRequest);

        User user = UserMapper.toUser(newUserRequest);
        userRepository.save(user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Long> ids, Integer from, Integer size) {
        log.info("Getting users with ids={}", ids);

        SizeValidator.validateSize(size);
        Pageable pageable = OffsetPageRequest.of(from, size);

        List<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).toList();
        } else {
            users = userRepository.findUsersByIdIn(ids, pageable);
        }

        return UserMapper.toUserDto(users);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Removing user with id={}", id);

        userExists(id);
        userRepository.deleteById(id);
    }

    @Override
    public void userExists(Long id) {
        log.info("Checking that user with id={} exists", id);

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
    }

    @Override
    public User getUserModelById(Long id) {
        log.info("Getting user model by id={}", id);

        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("User", id));
    }
}
