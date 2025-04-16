package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.enumModels.StatusFriendship;

@Data
@AllArgsConstructor
public class FriendshipDto {
    private int addresseeId;
    private StatusFriendship friendship;

    public static FriendshipDto from(Friendship friendship) {
        return new FriendshipDto(
                friendship.getAddresseeId(),
                friendship.getStatus()
        );
    }
}
