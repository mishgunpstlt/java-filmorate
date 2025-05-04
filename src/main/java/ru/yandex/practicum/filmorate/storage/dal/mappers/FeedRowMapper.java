package ru.yandex.practicum.filmorate.storage.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enumModels.EventType;
import ru.yandex.practicum.filmorate.model.enumModels.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEntityId(resultSet.getInt("entity_id"));
        feed.setOperation(Operation.valueOf(resultSet.getString("operation")));
        feed.setEventId(resultSet.getInt("event_id"));
        feed.setUserId(resultSet.getInt("user_id"));
        feed.setEventType(EventType.valueOf(resultSet.getString("type")));
        feed.setTimestamp(resultSet.getTimestamp("event_time").toLocalDateTime());

        return feed;
    }
}
