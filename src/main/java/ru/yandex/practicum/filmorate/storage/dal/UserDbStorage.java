package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
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
    private final RowMapper<Film> filmMapper;

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
        // Проверяем: существует ли обратная заявка
        String checkReverseSql = "SELECT status_id FROM friends WHERE requester_id = ? AND addressee_id = ?";
        List<Integer> reverseStatuses = jdbc.queryForList(checkReverseSql, Integer.class, addresseeId, requesterId);

        boolean reverseExists = !reverseStatuses.isEmpty();
        boolean reverseIsUnconfirmed = reverseExists && reverseStatuses.get(0) == StatusFriendship.UNCONFIRMED.getId();

        String checkDirectSql = "SELECT COUNT(*) FROM friends WHERE requester_id = ? AND addressee_id = ?";
        Integer directCount = jdbc.queryForObject(checkDirectSql, Integer.class, requesterId, addresseeId);

        boolean directExists = directCount != null && directCount > 0;

        if (reverseIsUnconfirmed) {
            if (!directExists) {
                // Добавляем прямую запись
                String insertConfirmed = "INSERT INTO friends (requester_id, addressee_id, status_id) VALUES (?, ?, ?)";
                jdbc.update(insertConfirmed, requesterId, addresseeId, StatusFriendship.CONFIRMED.getId());
            } else {
                // Обновляем прямую запись
                String updateDirect = "UPDATE friends SET status_id = ? WHERE requester_id = ? AND addressee_id = ?";
                jdbc.update(updateDirect, StatusFriendship.CONFIRMED.getId(), requesterId, addresseeId);
            }

            // Обновляем обратную запись
            String updateReverse = "UPDATE friends SET status_id = ? WHERE requester_id = ? AND addressee_id = ?";
            jdbc.update(updateReverse, StatusFriendship.CONFIRMED.getId(), addresseeId, requesterId);

            return new Friendship(requesterId, addresseeId, StatusFriendship.CONFIRMED);
        }

        if (!directExists) {
            String insertUnconfirmed = "INSERT INTO friends (requester_id, addressee_id, status_id) VALUES (?, ?, ?)";
            jdbc.update(insertUnconfirmed, requesterId, addresseeId, StatusFriendship.UNCONFIRMED.getId());
            return new Friendship(requesterId, addresseeId, StatusFriendship.UNCONFIRMED);
        }

        return null; // Уже существует или не требует действий
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

    public Set<Integer> getLikesByUserId(int userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return new HashSet<>(jdbc.queryForList(sql, Integer.class, userId));
    }

    public Map<Integer, Set<Integer>> getAllLikes() {
        String sql = "SELECT * FROM likes";
        return jdbc.query(sql, rs -> {
            Map<Integer, Set<Integer>> usersLikes = new HashMap<>();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int filmId = rs.getInt("film_id");
                usersLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
            }
            return usersLikes;
        });
    }

    public List<Film> getFilmsByIds(Set<Integer> mostSimilarUserLikes) {
        String ids = String.join(",", Collections.nCopies(mostSimilarUserLikes.size(), "?"));

        try {
            String sql = "SELECT * FROM films WHERE film_id IN (" + ids + ")";
            return jdbc.query(sql, filmMapper, mostSimilarUserLikes.toArray());
        } catch (DataAccessException e) {
            log.error("Ошибка при получении фильмов по списку id: {}", mostSimilarUserLikes, e);
            throw new RuntimeException("Не удалось получить рекомендации для пользователя", e);
        }

    }
}
