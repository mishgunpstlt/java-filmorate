package ru.yandex.practicum.filmorate.model.enumModels;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum MPA {
    G("У фильма нет возрастных ограничений"),
    PG("Детям рекомендуется смотреть фильм с родителями"),
    PG13("Детям до 13 лет просмотр не желателен"),
    R("Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC17("Лицам до 18 лет просмотр запрещён");

    private final String description;

    MPA(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static MPA fromId(int id) {
        return switch (id) {
            case 1 -> G;
            case 2 -> PG;
            case 3 -> PG13;
            case 4 -> R;
            case 5 -> NC17;
            default -> throw new NotFoundException("Неизвестный id MPA: " + id);
        };
    }
    public int getId() {
        return switch (this) {
            case G -> 1;
            case PG -> 2;
            case PG13 -> 3;
            case R -> 4;
            case NC17 -> 5;
        };
    }

    public String getFormattedName() {
        return switch (this) {
            case G -> "G";
            case PG -> "PG";
            case PG13 -> "PG-13";
            case R -> "R";
            case NC17 -> "NC-17";
        };
    }
}
