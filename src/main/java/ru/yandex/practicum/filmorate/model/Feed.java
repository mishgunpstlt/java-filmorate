package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feed {
    int eventId;
    String eventType;
    String operation;
    int userId;
    int entityId;
    LocalDateTime timestamp;
}
