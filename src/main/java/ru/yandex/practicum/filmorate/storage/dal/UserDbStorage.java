package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enumModels.StatusFriendship;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.FriendshipRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;


@RequiredArgsConstructor
@Component
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) " +
                "VALUES(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Integer generatedId = keyHolder.getKeyAs(Integer.class);
        if (generatedId == null) {
            throw new RuntimeException("Не удалось сохранить пользователя — id не сгенерирован");
        }
        user.setId(generatedId);

        return user;
    }

    @Override
    public User updateUser(User newUser) {
        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
        jdbc.update(sql, newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(),
                newUser.getId());

        return newUser;
    }

    @Override
    public Collection<User> getUsers() {
        String sql = "SELECT * FROM users";
        List<User> results = jdbc.query(sql, mapper);
        for (User u : results) {
            u.setFriends(getFriendsByUserId(u.getId()));
        }
        return results;
    }

    @Override
    public Optional<User> findUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User result = jdbc.queryForObject(sql, mapper, id);
            result.setFriends(getFriendsByUserId(id));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Set<Friendship> getFriendsByUserId(int userId) {
        String sql = "SELECT * FROM friends WHERE requester_id = ?";
        // Получаем список друзей, где статус подтвержден
        return new HashSet<>(jdbc.query(sql, new FriendshipRowMapper(), userId));
    }

    public Friendship addFriend(int requesterId, int addresseeId) {
        // Проверка: есть ли уже такая заявка
        String sqlCheck = "SELECT status_id FROM friends WHERE requester_id = ? AND addressee_id = ?";
        List<Integer> statuses = jdbc.queryForList(sqlCheck, Integer.class, requesterId, addresseeId);

        if (statuses.isEmpty()) {
            // Если заявки нет, то создаем новую
            String insertUnconfirmed = "INSERT INTO friends (requester_id, addressee_id, status_id) VALUES (?, ?, ?)";
            jdbc.update(insertUnconfirmed, requesterId, addresseeId, StatusFriendship.UNCONFIRMED.getId());
            return new Friendship(requesterId, addresseeId, StatusFriendship.UNCONFIRMED);
        } else if (statuses.get(0) == StatusFriendship.UNCONFIRMED.getId()) {
            // Если заявка уже есть, обновляем ее статус на CONFIRMED
            String updateConfirmed = "UPDATE friends SET status_id = ? WHERE requester_id = ? OR addressee_id = ?";
            jdbc.update(updateConfirmed, StatusFriendship.CONFIRMED.getId(), requesterId, addresseeId);
            return new Friendship(requesterId, addresseeId, StatusFriendship.CONFIRMED);
        }

        return null;
    }


    public void removeFriend(int requesterId, int addresseeId) {
        String sql = "DELETE FROM friends WHERE requester_id = ? AND addressee_id = ?";
        jdbc.update(sql, requesterId, addresseeId);

        String sqlCheck = "SELECT status_id FROM friends WHERE requester_id = ? AND addressee_id = ?";
        List<Integer> statuses = jdbc.queryForList(sqlCheck, Integer.class, addresseeId, requesterId);
        if (!statuses.isEmpty() && StatusFriendship.fromId(statuses.get(0)) == StatusFriendship.CONFIRMED) {
            String sqlUpdate = "UPDATE friendships SET status_id = ? WHERE requester_id = ? AND addressee_id = ?";
            jdbc.update(sqlUpdate, StatusFriendship.UNCONFIRMED.getId(), addresseeId, requesterId);
        }
    }

    public List<User> findMutualFriends(int userId, int otherUserId) {
        String sql = """
                    SELECT u.* FROM users u
                    JOIN friends f1 ON u.user_id = f1.addressee_id
                    JOIN friends f2 ON u.user_id = f2.addressee_id
                    WHERE f1.requester_id = ? AND f2.requester_id = ?
                """;
        return jdbc.query(sql, mapper, userId, otherUserId);
    }
}
