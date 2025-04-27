package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.dal.dto.FilmDto;
import ru.yandex.practicum.filmorate.storage.dal.dto.FriendshipDto;
import ru.yandex.practicum.filmorate.storage.dal.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid UserDto dto) {
        return UserDto.fromModel(userService.addUser(UserDto.toModel(dto)));
    }

    @PutMapping
    public UserDto updateUser(@RequestBody @Valid UserDto dto) {
        return UserDto.fromModel(userService.updateUser(UserDto.toModel(dto)));
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers().stream()
                .map(UserDto::fromModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable int id) {
        return UserDto.fromModel(userService.getUserById(id).get());
    }

    @PutMapping("/{id}/friends/{friendId}")
    public FriendshipDto addFriend(@PathVariable int id, @PathVariable int friendId) {
        Friendship friendship = userService.addFriend(id, friendId);
        return FriendshipDto.from(friendship);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable int id) {
        return userService.getFriends(id).stream()
                .map(friendship -> {
                    int friendId = (friendship.getRequesterId() == id)
                            ? friendship.getAddresseeId()
                            : friendship.getRequesterId();
                    return userService.getUserById(friendId);
                })
                .map((Optional<User> user) -> UserDto.fromModel(user.get()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.findMutualFriends(id, otherId).stream()
                .map(UserDto::fromModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable int id) {
        return userService.getRecommendations(id).stream()
                .map(FilmDto::toDto)
                .collect(Collectors.toList());
    }
}
