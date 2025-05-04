package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Feed;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedDto {
    int eventId;
    String eventType;
    String operation;
    int userId;
    int entityId;
    long timestamp;

    public static Collection<FeedDto> fromModel(Collection<Feed> feed) {
        return feed.stream().map(FeedDto::fromModel).collect(Collectors.toList());
    }

    public static FeedDto fromModel(Feed feed) {
        FeedDto feedDto = new FeedDto();
        feedDto.setEventId(feed.getEventId());
        feedDto.setEventType(feed.getEventType().toString());
        feedDto.setOperation(feed.getOperation().toString());
        feedDto.setUserId(feed.getUserId());
        feedDto.setEntityId(feed.getEntityId());

        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset zoneOffset = systemZone.getRules().getOffset(Instant.now());
        feedDto.setTimestamp(feed.getTimestamp().toEpochSecond(zoneOffset) * 1000);

        return feedDto;
    }
}