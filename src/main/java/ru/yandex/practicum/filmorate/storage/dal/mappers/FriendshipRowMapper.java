package ru.yandex.practicum.filmorate.storage.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.enumModels.StatusFriendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Friendship(
                resultSet.getInt("requester_id"),
                resultSet.getInt("addressee_id"),
                StatusFriendship.fromId(resultSet.getInt("status_id"))
        );
    }
}
