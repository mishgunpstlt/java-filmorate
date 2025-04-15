package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Friendship;

@Data
@AllArgsConstructor
public class FriendshipDto {
    private int requesterId;
    private Integer addresseeId;
    private String status;

    public static FriendshipDto from(Friendship friendship) {
        return new FriendshipDto(
                friendship.getRequesterId(),
                friendship.getAddresseeId(),
                friendship.getStatus().name()
        );
    }
}
