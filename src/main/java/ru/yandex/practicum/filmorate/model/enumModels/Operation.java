package ru.yandex.practicum.filmorate.model.enumModels;

public enum Operation {
    REMOVE,
    ADD,
    UPDATE;

    public int getId() {
        return switch (this) {
            case REMOVE -> 1;
            case ADD -> 2;
            case UPDATE -> 3;
        };
    }
}
