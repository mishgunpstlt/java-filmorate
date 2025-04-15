package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enumModels.StatusFriendship;

@Data
@AllArgsConstructor
public class Friendship {
    private int requesterId;
    private int addresseeId;
    private StatusFriendship status;
}
