package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.enumModels.EventType;
import ru.yandex.practicum.filmorate.model.enumModels.Operation;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
public class FeedTest {
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    @Test
    public void shouldAddAndGetFeed() {
        UserDbStorage.addFeed(jdbc, 1, 1, EventType.LIKE, Operation.ADD);
        UserDbStorage.addFeed(jdbc, 1, 2, EventType.FRIEND, Operation.ADD);
        assertFalse(userDbStorage.getFeed(1).isEmpty());
    }
}
