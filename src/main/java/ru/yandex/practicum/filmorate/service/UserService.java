package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.RelationshipException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@Slf4j
public class UserService {

    private final UserDbStorage userDbStorage;

    public UserService(UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    public User addUser(User user) {
        setNameIfEmpty(user);
        User createUser = userDbStorage.addUser(user);
        log.info("Добавлен новый объект в таблицу users: {}", createUser);
        return createUser;
    }

    public User updateUser(User newUser) {
        getExistsUser(newUser.getId());
        setNameIfEmpty(newUser);
        User updatedUser = userDbStorage.updateUser(newUser);
        log.info("Изменен объект в таблице users, теперь новый объект: {}", updatedUser);
        return updatedUser;
    }

    public Collection<User> getUsers() {
        log.info("Получены объекты коллекции(users): {}", userDbStorage.getUsers());
        return userDbStorage.getUsers();
    }

    private Optional<User> getExistsUser(int userId) {
        Optional<User> user = userDbStorage.findUserById(userId);
        if (user.isPresent()) {
            log.info("Пользователь с id={} найден", userId);
            return user;
        } else {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public Optional<User> getUserById(int id) {
        return getExistsUser(id);
    }

    public Friendship addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка добавить себя же в друзья id={}", userId);
            throw new RelationshipException("Попытка добавить себя же в друзья id=" + userId);
        }
        getExistsUser(userId);
        getExistsUser(friendId);


        log.info("Пользователь с id={} стал другом пользователя с id={}", userId, friendId);
        return userDbStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Попытка удалить себя же из друзья id={}", userId);
            throw new RelationshipException("Попытка удалить себя же из друзьей id=" + userId);
        }

        getExistsUser(userId);
        getExistsUser(friendId);

        userDbStorage.removeFriend(userId, friendId);
        log.info("Пользователь с id={} удалил пользователя с id={} из списка друзей", userId, friendId);
    }

    public Set<Friendship> getFriends(int userId) {
        getExistsUser(userId);
        return userDbStorage.getFriendsByUserId(userId);
    }

    public List<User> findMutualFriends(int userId, int friendId) {
        getExistsUser(userId);
        getExistsUser(friendId);

        List<User> mutualFriends = userDbStorage.findMutualFriends(userId, friendId);
        log.info("Общие друзья между пользователями с id={} и id={}: {}", userId, friendId, mutualFriends);
        return mutualFriends;
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
