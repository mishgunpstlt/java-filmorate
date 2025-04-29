package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class Film {
    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не должна быть пустой")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    private Set<Integer> likes = new HashSet<>();
    @NotNull(message = "У фильма не может не быть жанра")
    private Set<Genre> genres = new HashSet<>();
    @NotNull(message = "У фильма не может не быть МРА")
    private Mpa mpa;

    private Set<Director> directors = new HashSet<>();

    @AssertTrue(message = "Дата релиза фильма должна быть не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
