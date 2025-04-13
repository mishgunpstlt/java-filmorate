package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.RelationshipException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enumModels.StatusFriendship;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        setNameIfEmpty(user);
        user.setId(getNextId());
        userStorage.addUser(user);
        log.info("Добавлен новый объект в коллекцию(users): {}", user);
        return user;
    }

    public User updateUser(User newUser) {
        User oldUser = getExistsUser(newUser.getId());
        setNameIfEmpty(newUser);
        userStorage.updateUser(newUser);
        log.info("Изменен объект в коллекции(users), теперь новый объект: {}", newUser);
        return newUser;
    }

    public Collection<User> getUsers() {
        log.info("Получены объекты коллекции(users): {}", userStorage.getUsers());
        return userStorage.getUsers();
    }

    private User getExistsUser(int userId) {
        User user = userStorage.findUserById(userId);
        if (user != null) {
            log.info("Пользователь с id={} найден", userId);
            return user;
        } else {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public User getUserById(int id) {
        return getExistsUser(id);
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка добавить себя же в друзья id={}", userId);
            throw new RelationshipException("Попытка добавить себя же в друзья id=" + userId);
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add(new Friendship(userId, friendId, StatusFriendship.CONFIRMED));
        friend.getFriends().add(new Friendship(friendId, userId, StatusFriendship.CONFIRMED));
        log.info("Пользователь с id={} стал другом пользователя с id={}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка удалить себя же из друзья id={}", userId);
            throw new RelationshipException("Попытка удалить себя же из друзьей id=" + userId);
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().removeIf(f -> f.getAddresseeId() == friendId);
        friend.getFriends().removeIf(f -> f.getAddresseeId() == userId);
        log.info("Пользователь с id={} удалил пользователя с id={} из списка друзей", userId, friendId);
    }

    public Set<Integer> findMutualFriends(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        Set<Integer> userFriendIds = user.getFriends().stream()
                .map(Friendship::getAddresseeId)
                .collect(Collectors.toSet());

        Set<Integer> friendFriendIds = friend.getFriends().stream()
                .map(Friendship::getAddresseeId)
                .collect(Collectors.toSet());

        userFriendIds.retainAll(friendFriendIds);
        log.info("Общие друзья между пользователями с id={} и id={}: {}", userId, friendId, userFriendIds);
        return userFriendIds;
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private int getNextId() {
        int currentMaxId = userStorage.getUsers()
                .stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
