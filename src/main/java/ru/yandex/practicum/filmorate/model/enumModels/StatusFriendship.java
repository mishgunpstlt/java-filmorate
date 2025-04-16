package ru.yandex.practicum.filmorate.model.enumModels;

public enum StatusFriendship {
    CONFIRMED,
    UNCONFIRMED;

    public static StatusFriendship fromId(int id) {
        return switch (id) {
            case 1 -> CONFIRMED;
            case 2 -> UNCONFIRMED;
            default -> throw new IllegalArgumentException("Неизвестный id StatusFriendship: " + id);
        };
    }

    public int getId() {
        return switch (this) {
            case CONFIRMED -> 1;
            case UNCONFIRMED -> 2;
        };
    }
}
