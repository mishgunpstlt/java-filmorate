package ru.yandex.practicum.filmorate.model.enumModels;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum Genre {
    COMEDY,
    DRAMA,
    CARTOON,
    THRILLER,
    DOCUMENTARY,
    ACTION;


    public static Genre fromId(int id) {
        return switch (id) {
            case 1 -> COMEDY;
            case 2 -> DRAMA;
            case 3 -> CARTOON;
            case 4 -> THRILLER;
            case 5 -> DOCUMENTARY;
            case 6 -> ACTION;

            default -> throw new NotFoundException("Неизвестный id Genre: " + id);
        };
    }

    public int getId() {
        return switch (this) {
            case COMEDY -> 1;
            case DRAMA -> 2;
            case CARTOON -> 3;
            case THRILLER -> 4;
            case DOCUMENTARY -> 5;
            case ACTION -> 6;
        };
    }

    public String getFormattedName() {
        return switch (this) {
            case COMEDY -> "Комедия";
            case DRAMA -> "Драма";
            case CARTOON -> "Мультфильм";
            case THRILLER -> "Триллер";
            case DOCUMENTARY -> "Документальный";
            case ACTION -> "Боевик";
        };
    }
}
