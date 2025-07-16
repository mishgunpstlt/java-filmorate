package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enumModels.EventType;
import ru.yandex.practicum.filmorate.model.enumModels.Operation;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feed {
    int eventId;
    EventType eventType;
    Operation operation;
    int userId;
    int entityId;
    LocalDateTime timestamp;
}
