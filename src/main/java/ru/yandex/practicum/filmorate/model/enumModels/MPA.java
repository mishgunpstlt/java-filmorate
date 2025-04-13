package ru.yandex.practicum.filmorate.model.enumModels;

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
}
