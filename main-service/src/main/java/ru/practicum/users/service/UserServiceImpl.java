package ru.practicum.users.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.ObjectNotFoundException;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
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
    public void deleteById(Long id) {
        log.info("Removing user with id={}", id);

        userExistsById(id);
        userRepository.deleteById(id);
    }

    @Override
    public void userExistsById(Long id) {
        log.info("Checking that user with id={} exists", id);

        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException("User", id);
        }
    }

    @Override
    public User getUserById(Long id) {
        log.info("Getting user with id={}", id);

        return userRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("User", id));
    }
}
