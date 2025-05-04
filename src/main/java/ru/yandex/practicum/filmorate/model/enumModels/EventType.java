package ru.yandex.practicum.filmorate.model.enumModels;

public enum EventType {
    LIKE,
    REVIEW,
    FRIEND;

    public int getId() {
        return switch (this) {
            case LIKE -> 1;
            case REVIEW -> 2;
            case FRIEND -> 3;
        };
    }
}
